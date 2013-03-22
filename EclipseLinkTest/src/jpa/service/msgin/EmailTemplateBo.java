package jpa.service.msgin;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.NoResultException;

import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.VariableType;
import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.exception.TemplateNotFoundException;
import jpa.model.EmailAddress;
import jpa.model.EmailTemplate;
import jpa.model.EmailVariable;
import jpa.model.MailingList;
import jpa.model.SenderData;
import jpa.model.message.TemplateData;
import jpa.model.message.TemplateDataPK;
import jpa.service.EmailAddressService;
import jpa.service.EmailTemplateService;
import jpa.service.EmailVariableService;
import jpa.service.MailingListService;
import jpa.service.SenderDataService;
import jpa.service.external.VariableResolver;
import jpa.service.message.TemplateDataService;
import jpa.service.msgout.RenderBo;
import jpa.util.EmailAddrUtil;
import jpa.util.SpringUtil;
import jpa.variable.RenderUtil;
import jpa.variable.RenderVariableVo;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("emailTemplateBo")
@Transactional(propagation=Propagation.REQUIRED)
public class EmailTemplateBo {
	static final Logger logger = Logger.getLogger(EmailTemplateBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator", "\n");

	@Autowired
	private EmailTemplateService emailTemplateDao;
	@Autowired
	private EmailVariableService emailVariableDao;
	@Autowired
	private RenderBo renderBo;
	@Autowired
	private MailingListService mailingListDao;
	@Autowired
	private EmailAddressService emailAddrDao;
	@Autowired
	private TemplateDataService templateDataDao;
	@Autowired
	private SenderDataService senderService;
	
	public static void main(String[] args) {
		EmailTemplateBo bo = (EmailTemplateBo) SpringUtil.getAppContext().getBean("emailTemplateBo");
		SpringUtil.beginTransaction();
		try {
			bo.processMain();
			SpringUtil.commitTransaction();
		}
		catch (Exception e) {
			logger.error("Exception", e);
			SpringUtil.rollbackTransaction();
		}
		finally {
		}
	}
	
	void processMain() {
		try {
			String text = renderBo.renderTemplateById("testTemplate", null, null);
			System.out.println(text);
			SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
			TemplateDataPK pk = new TemplateDataPK(sender, "testTemplate", null);
			TemplateData bodyVo = templateDataDao.getByBestMatch(pk);
			if (bodyVo == null) {
				throw new DataValidationException("BodyTemplate not found for testTemplate");
			}
			else if (isDebugEnabled) {
				logger.debug("Template to render:" + LF + bodyVo.getBodyTemplate());
			}
			List<String> variables = RenderUtil.retrieveVariableNames(bodyVo.getBodyTemplate());
			System.out.println("Variables: " + variables);
			
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("BroadcastMsgId","3");
			TemplateRenderVo renderVo = renderEmailTemplate("jsmith@test.com",vars, "SampleNewsletter1");
			System.out.println(renderVo);
			
			System.out.println(renderEmailVariable("UserProfileURL", Integer.valueOf(1)));
			
			String checkText = "Dear ${SubscriberAddress}," + LF + LF + 
			"This is a sample text newsletter message for a traditional mailing list." + LF +
			"With a traditional mailing list, people who want to subscribe to the list " + LF +
			"must send an email from their account to the mailing list address with " + LF +
			"\"subscribe\" in the email subject." + LF + LF + 
			"Unsubscribing from a traditional mailing list is just as easy; simply send " + LF +
			"an email to the mailing list address with \"unsubscribe\" in subject." + LF + LF +
			"Date sent: ${CurrentDate}" + LF + LF +
			"BroadcastMsgId: ${BroadcastMsgId}, ListId: ${MailingListId}" + LF + LF +
			"Contact Email: ${ContactEmailAddress}" + LF + LF +
			"To see our promotions, copy and paste the following link in your browser:" + LF +
			"${WebSiteUrl}/SamplePromoPage.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}" + LF +
			"${FooterWithUnsubAddr}";
			checkVariableLoop(checkText);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method first retrieves variable names from the input text and save
	 * them into a list. It then loop through the list and for each name in the
	 * list, it checks the variable's value to make sure there is no loops.<br/>
	 * 
	 * This method should be called before an email template is saved to the
	 * database.
	 * 
	 * @param text -
	 *            template text
	 * @throws DataValidationException
	 */
	public void checkVariableLoop(String text) throws DataValidationException {
		List<String> varNames = RenderUtil.retrieveVariableNames(text);
		for (String loopName : varNames) {
			try {
				EmailVariable vo = emailVariableDao.getByVariableName(loopName);
				RenderUtil.checkVariableLoop(vo.getDefaultValue(), loopName);
			}
			catch (NoResultException e) {}
		}
	}
	
	/**
	 * For each variable name on the input list, retrieve its value by executing
	 * its SQL query or process class if one is defined, return the default
	 * value otherwise.
	 * 
	 * @param variables -
	 *            list of variable names
	 * @param addrId -
	 *            email address id
	 * @return a map of rendered variables.
	 */
	Map<String, RenderVariableVo> renderEmailVariables(List<String> variables,
			int addrId) {
		HashMap<String, RenderVariableVo> vars = new HashMap<String, RenderVariableVo>();
		for (String name : variables) {
			if (RenderUtil.isListVariable(name)) {
				continue;
			}
			EmailVariable vo =null;
			try {
				vo= emailVariableDao.getByVariableName(name);
			}	
			catch (NoResultException e) {
				logger.info("renderEmailVariables() - EmailVariable record not found, "
						+ "variable name: " + name);
				continue;
			}
			String query = vo.getVariableQuery();
			String proc = vo.getVariableProcName();
			String value = null;
			if (StringUtils.isNotBlank(query)) {
				try {
					value = emailVariableDao.getByQuery(query, addrId);
				}
				catch (Exception e) {
					logger.error("Exception caught for: " + query, e);
				}
			}
			else if (StringUtils.isNotBlank(proc)) {
				try {
					Object obj = Class.forName(proc).newInstance();
					if (obj instanceof VariableResolver) {
						value = ((VariableResolver)obj).process(addrId);
					}
					else {
						logger.error("Variable class is not a VariableResolver.");
					}
				}
				catch (Exception e) {
					logger.error("Exception caught for: " + proc, e);
				}
			}
			// use default if the query or procedure returned no value 
			if (value == null) {
				value = vo.getDefaultValue();
			}
			logger.info("renderEmailVariables() - name=" + name + ", value=" + value);
			RenderVariableVo var = new RenderVariableVo(name, value, null, VariableType.TEXT,
					CodeType.YES_CODE.getValue(), Boolean.FALSE);
			vars.put(name, var);
		}
		return vars;
	}
	
	/* experimental */
	String renderEmailVariable(String emailVariableName, Integer sbsrId) throws DataValidationException {
		String renderedValue = "";
		EmailVariable vo = emailVariableDao.getByVariableName(emailVariableName);
		HashMap<String, RenderVariableVo> vars = new HashMap<String, RenderVariableVo>();
		if (sbsrId != null) {
			RenderVariableVo var = new RenderVariableVo(
					"SubscriberAddressId",
					sbsrId.toString(),
					null,
					VariableType.TEXT,
					CodeType.YES_CODE.getValue(),
					Boolean.FALSE);
			vars.put("SubscriberAddressId", var);
		}
		if (vo != null) {
			try {
				renderedValue = renderBo.renderTemplateText(vo.getDefaultValue(), null, vars);
			}
			catch (Exception e) {
				System.out.println("loadSbsrDaos.jsp - renderEmailVariable: " + e.toString());
			}
		}
		return renderedValue;
	}
	
	/**
	 * This method renders an email template using provided inputs. It retrieves
	 * a template text using provided template id, and renders the template
	 * using provided variables. It renders customer variables using the
	 * provided TO email address, and uses the list address from the template as
	 * its FROM address.
	 * 
	 * @param toAddr -
	 *            TO address
	 * @param variables -
	 *            list of variables with rendered values
	 * @param templateId -
	 *            template id
	 * @return A TemplateRenderVo instance
	 * @throws DataValidationException
	 * @throws TemplateNotFoundException
	 * @throws TemplateException 
	 */
	public TemplateRenderVo renderEmailTemplate(String toAddr, Map<String, String> variables,
			String templateId) throws DataValidationException, TemplateNotFoundException,
			TemplateException {
		return renderEmailTemplate(toAddr, variables, templateId, null);
	}
	
	/**
	 * This method renders an email template using provided inputs. It retrieves
	 * a template text using provided template id, and renders the template
	 * using provided variables. It renders customer variables using the
	 * provided TO email address, and uses the list address from the template as
	 * its FROM address. If the listIdOverride is provided, it'll use its list
	 * address as FROM address instead.
	 * 
	 * @param toAddr -
	 *            TO address
	 * @param variables -
	 *            list of variables with rendered values
	 * @param templateId -
	 *            template id
	 * @param listIdOverride -
	 *            use this list address as FROM address if provided.
	 * @return A TemplateRenderVo instance
	 * @throws DataValidationException
	 * @throws TemplateNotFoundException
	 * @throws TemplateException 
	 */
	public TemplateRenderVo renderEmailTemplate(String toAddr, Map<String, String> variables,
			String templateId, String listIdOverride) throws DataValidationException,
			TemplateNotFoundException, TemplateException {
		if (templateId == null) {
			throw new DataValidationException("Input templateId is null.");
		}
		validateToAddress(toAddr);
		EmailTemplate tmpltVo = null;
		try {
			tmpltVo = emailTemplateDao.getByTemplateId(templateId);
		}
		catch (NoResultException e) {
			throw new TemplateNotFoundException("Could not find Template by Id: " + templateId);
		}
		MailingList listVo = null;
		if (StringUtils.isNotBlank(listIdOverride)) {
			// try the list id from input parameters first
			try {
				listVo = mailingListDao.getByListId(listIdOverride);
			}
			catch (NoResultException e) {
				logger.warn("renderEmailTemplate() - Failed to find List by override list Id: "
								+ listIdOverride);
			}
		}
		if (listVo == null) {
			// use the list id from template
			try {
				listVo = mailingListDao.getByListId(tmpltVo.getMailingList().getListId());
			}
			catch (NoResultException e) {
				throw new DataValidationException("Could not find Mailing List by Id: "
						+ tmpltVo.getMailingList().getListId());
			}
		}
		TemplateRenderVo renderVo = new TemplateRenderVo();
		renderVo.setToAddr(toAddr);
		renderVo.setSenderId(listVo.getSenderData().getSenderId());
		renderVo.setEmailTemplate(tmpltVo);
		renderVo.setMailingList(listVo);
		// retrieve variable names from body template
		List<String> varNames = RenderUtil.retrieveVariableNames(tmpltVo.getBodyText());
		if (isDebugEnabled) {
			logger.debug("renderEmailTemplate() - Body Variable names: " + varNames);
		}
		// retrieve variable names from subject template
		String subjText = tmpltVo.getSubject() == null ? "" : tmpltVo.getSubject();
		List<String> subjVarNames = RenderUtil.retrieveVariableNames(subjText);
		if (!subjVarNames.isEmpty()) {
			varNames.addAll(subjVarNames);
			if (isDebugEnabled) {
				logger.debug("renderEmailTemplate() - Subject Variable names: " + subjVarNames);
			}
		}
		EmailAddress addrVo = emailAddrDao.findSertAddress(toAddr);
		// render email variables using TO emailAddrId
		Map<String, RenderVariableVo> vars = renderEmailVariables(varNames, addrVo.getRowId());
		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (String key : keys) {
				RenderVariableVo var = new RenderVariableVo(key, variables.get(key), null,
						VariableType.TEXT, CodeType.YES_CODE.getValue(), Boolean.FALSE);
				vars.put(key, var);
			}
		}
		// include mailing list variables
		vars.putAll(MailingListUtil.renderListVariables(listVo, toAddr, addrVo.getRowId()));
		try {
			// now render the templates
			String senderId = listVo.getSenderData().getSenderId();
			String body = renderBo.renderTemplateText(tmpltVo.getBodyText(), senderId, vars);
			String subj = renderBo.renderTemplateText(tmpltVo.getSubject(), senderId, vars);
			renderVo.setSubject(subj);
			renderVo.setBody(body);
		}
		catch (ParseException e) {
			throw new DataValidationException("ParseException caught", e);
		}
		if (vars.containsKey(EmailAddrType.CC_ADDR.getValue())) {
			// set CC if it was passed as an input variable
			RenderVariableVo cc = vars.get(EmailAddrType.CC_ADDR.getValue());
			if (cc != null && VariableType.TEXT.equals(cc.getVariableType())
					&& cc.getVariableValue() != null) {
				try {
					validateFromAddress((String) cc.getVariableValue());
					renderVo.setCcAddr((String) cc.getVariableValue());
				}
				catch (Exception e) {
					logger.error("renderEmailTemplate() - Failed to parse CC address: "
							+ cc.getVariableValue() + LF + e.getMessage());
				}
			}
		}
		if (vars.containsKey(EmailAddrType.BCC_ADDR.getValue())) {
			// set BCC if it was passed as an input variable
			RenderVariableVo bcc = vars.get(EmailAddrType.BCC_ADDR.getValue());
			if (bcc != null && VariableType.TEXT.equals(bcc.getVariableType())
					&& bcc.getVariableValue() != null) {
				try {
					validateFromAddress((String) bcc.getVariableValue());
					renderVo.setBccAddr((String) bcc.getVariableValue());
				}
				catch (Exception e) {
					logger.error("renderEmailTemplate() - Failed to parse BCC address: "
							+ bcc.getVariableValue() + LF + e.getMessage());
				}
			}
		}
		validateFromAddress(listVo.getListEmailAddr());
		renderVo.setFromAddr(listVo.getListEmailAddr());
		return renderVo;
	}
	
	private void validateToAddress(String toAddr) throws DataValidationException {
		if (toAddr == null) {
			throw new DataValidationException("Input toAddr is null.");
		}
		if (!EmailAddrUtil.isRemoteEmailAddress(toAddr)) {
			throw new DataValidationException("Input toAddr is invalid: " + toAddr);
		}
		try {
			InternetAddress.parse(toAddr);
		}
		catch (AddressException e) {
			throw new DataValidationException("Input toAddr is invalid: " + toAddr, e);
		}
	}
	
	private void validateFromAddress(String fromAddr) throws DataValidationException {
		try {
			InternetAddress.parse(fromAddr);
		}
		catch (AddressException e) {
			throw new DataValidationException("Invalid FROM address found from list: " + fromAddr);
		}
	}
	
	/**
	 * This method renders an email template using provided inputs. It renders
	 * the message subject and message body using provided variables. It renders
	 * customer variables using the provided TO email address. The FROM address
	 * is retrieved from the mailing list that is retrieved using the provided
	 * list id.
	 * 
	 * @param toAddr -
	 *            TO address
	 * @param variables -
	 *            list of variables with rendered values
	 * @param subj -
	 *            message subject
	 * @param body -
	 *            message body
	 * @param listId -
	 *            mailing list id this email associated to
	 * @return A TemplateRenderVo instance
	 * @throws DataValidationException
	 * @throws TemplateException 
	 */
	public TemplateRenderVo renderEmailText(String toAddr, Map<String, String> variables,
			String subj, String body, String listId) throws DataValidationException,
			TemplateException {
		return renderEmailText(toAddr, variables, subj, body, listId, null);
	}
	
	/**
	 * This method renders an email template using provided inputs. It renders
	 * the message subject and message body using provided variables. It renders
	 * customer variables using the provided TO email address. The FROM address
	 * is retrieved from the mailing list that is retrieved using the provided
	 * list id.<br/>
	 * 
	 * This method is intended for BroadcastBo where a same message body and
	 * subject are used again and again. The BroadcastBo could scan the message
	 * body and subject once for variable names, and pass them as one of the
	 * inputs.
	 * 
	 * @param toAddr -
	 *            TO address
	 * @param variables -
	 *            list of variables with rendered values
	 * @param subj -
	 *            message subject
	 * @param body -
	 *            message body
	 * @param listId -
	 *            mailing list id this email associated to
	 * @param variableNames -
	 *            list of variable names retrieved from subject and body
	 * @return A TemplateRenderVo instance
	 * @throws DataValidationException
	 * @throws TemplateException 
	 */
	public TemplateRenderVo renderEmailText(String toAddr, Map<String, String> variables,
			String subj, String body, String listId, List<String> variableNames)
			throws DataValidationException, TemplateException {
		// first check input TO address
		validateToAddress(toAddr);
		MailingList listVo = null;
		try {
			listVo = mailingListDao.getByListId(listId);
		}
		catch (NoResultException e) {
			throw new DataValidationException("Mailing List " + listId + " not found.");
		}
		String _from = listVo.getListEmailAddr();
		String dispName = listVo.getDisplayName();
		if (StringUtils.isNotBlank(dispName)) {
			_from = dispName + "<" + _from + ">";
		}
		validateFromAddress(_from); // us list address as FROM
		TemplateRenderVo renderVo = new TemplateRenderVo();
		renderVo.setToAddr(toAddr);
		renderVo.setFromAddr(_from);
		renderVo.setSenderId(listVo.getSenderData().getSenderId());
		renderVo.setMailingList(listVo);
		List<String> varNames = null;
		if (variableNames == null) {
			// retrieve variable names from message body
			varNames = RenderUtil.retrieveVariableNames(body);
			if (isDebugEnabled)
				logger.debug("Body Variable names: " + varNames);
			// retrieve variable names from message subject
			String subject = subj == null ? "" : subj;
			List<String> subjVarNames = RenderUtil.retrieveVariableNames(subject);
			if (!subjVarNames.isEmpty()) {
				varNames.addAll(subjVarNames);
				if (isDebugEnabled)
					logger.debug("Subject Variable names: " + subjVarNames);
			}
		}
		else { // use variable names from input
			varNames = variableNames;
		}
		EmailAddress addrVo = emailAddrDao.findSertAddress(toAddr);
		// retrieve variable values by variable name and email address id
		Map<String, RenderVariableVo> vars = renderEmailVariables(varNames,
				addrVo.getRowId());
		// include render variables from input data
		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (String key : keys) {
				RenderVariableVo var = new RenderVariableVo(key, variables.get(key), null,
						VariableType.TEXT, CodeType.YES_CODE.getValue(), Boolean.FALSE);
				vars.put(key, var);
			}
		}
		// include mailing list variables
		vars.putAll(MailingListUtil.renderListVariables(listVo, addrVo.getAddress(), addrVo.getRowId()));
		try {
			String bodyText = renderBo.renderTemplateText(body, listVo.getSenderData().getSenderId(), vars);
			String subjText = renderBo.renderTemplateText(subj, listVo.getSenderData().getSenderId(), vars);
			renderVo.setSubject(subjText);
			renderVo.setBody(bodyText);
		}
		catch (ParseException e) {
			logger.error("Failed to render message body", e);
			throw new DataValidationException("ParseException caught: " + e.toString());
		}
		return renderVo;
	}
	

}

package com.es.bo.sender;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.external.AbstractResolver;
import com.es.bo.mlist.MailingListUtil;
import com.es.bo.render.RenderUtil;
import com.es.bo.render.RenderVariable;
import com.es.core.util.EmailAddrUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.address.EmailTemplateDao;
import com.es.dao.address.EmailVariableDao;
import com.es.dao.address.MailingListDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.EmailAddressType;
import com.es.data.constant.VariableType;
import com.es.exception.DataValidationException;
import com.es.exception.TemplateException;
import com.es.exception.TemplateNotFoundException;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.address.EmailTemplateVo;
import com.es.vo.address.EmailVariableVo;
import com.es.vo.address.MailingListVo;

@Component("emailRenderBo")
@Transactional(propagation=Propagation.REQUIRED)
public class EmailRenderBo implements java.io.Serializable {
	private static final long serialVersionUID = -8878231823581557690L;
	static final Logger logger = Logger.getLogger(EmailRenderBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator", "\n");

	@Autowired
	private EmailTemplateDao emailTemplateDao;
	@Autowired
	private EmailVariableDao emailVariableDao;
	@Autowired
	private RenderBo renderBo;
	@Autowired
	private MailingListDao mailingListDao;
	@Autowired
	private EmailAddressDao emailAddrDao;
	
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
			EmailVariableVo vo = emailVariableDao.getByVariableName(loopName);
			if (vo != null) {
				RenderUtil.checkVariableLoop(vo.getDefaultValue(), loopName);
			}
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
	Map<String, RenderVariable> renderEmailVariables(List<String> variables,
			long addrId) {
		Map<String, RenderVariable> vars = new HashMap<String, RenderVariable>();
		for (String name : variables) {
			if (RenderUtil.isListVariable(name)) {
				continue;
			}
			EmailVariableVo vo= emailVariableDao.getByVariableName(name);
			if (vo == null) {
				logger.info("renderEmailVariables() - EmailVariable record not found, "
						+ "variable name: " + name);
				continue;
			}
			String query = vo.getVariableQuery();
			String proc = vo.getVariableProc();
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
					if (obj instanceof AbstractResolver) {
						value = ((AbstractResolver)obj).process(addrId);
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
			RenderVariable var = new RenderVariable(name, value, null, VariableType.TEXT,
					CodeType.YES_CODE.getValue(), Boolean.FALSE);
			vars.put(name, var);
		}
		return vars;
	}
	
	/* experimental */
	public String renderEmailVariable(String emailVariableName, Integer sbsrId) throws DataValidationException {
		String renderedValue = "";
		EmailVariableVo vo = emailVariableDao.getByVariableName(emailVariableName);
		Map<String, RenderVariable> vars = new HashMap<String, RenderVariable>();
		if (sbsrId != null) {
			RenderVariable var = new RenderVariable(
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
				logger.info("loadSbsrDaos.jsp - renderEmailVariable: " + e.toString());
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
	 * @return A EmailRenderDo instance
	 * @throws DataValidationException
	 * @throws TemplateNotFoundException
	 * @throws TemplateException 
	 */
	public EmailRenderDo renderEmailTemplate(String toAddr, Map<String, String> variables,
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
	 * @return A EmailRenderDo instance
	 * @throws DataValidationException
	 * @throws TemplateNotFoundException
	 * @throws TemplateException 
	 */
	public EmailRenderDo renderEmailTemplate(String toAddr, Map<String, String> variables,
			String templateId, String listIdOverride) throws DataValidationException,
			TemplateNotFoundException, TemplateException {
		if (templateId == null) {
			throw new DataValidationException("Input templateId is null.");
		}
		validateToAddress(toAddr);
		EmailTemplateVo tmpltVo = emailTemplateDao.getByTemplateId(templateId);
		if (tmpltVo == null) {
			throw new TemplateNotFoundException("Could not find Template by Id: " + templateId);
		}
		MailingListVo listVo = null;
		if (StringUtils.isNotBlank(listIdOverride)) {
			// try the list id from input parameters first
			listVo = mailingListDao.getByListId(listIdOverride);
			if (listVo == null) {
				logger.warn("renderEmailTemplate() - Failed to find List by override list Id: "
								+ listIdOverride);
			}
		}
		if (listVo == null) {
			// use the list id from template
			listVo = mailingListDao.getByListId(tmpltVo.getListId());
			if (listVo == null) {
				throw new DataValidationException("Could not find Mailing List by Id: "
						+ tmpltVo.getListId());
			}
		}
		EmailRenderDo renderVo = new EmailRenderDo();
		renderVo.setToAddr(toAddr);
		renderVo.setSenderId(listVo.getSenderId());
		renderVo.setEmailTemplateVo(tmpltVo);
		renderVo.setMailingListVo(listVo);
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
		EmailAddressVo addrVo = emailAddrDao.findSertAddress(toAddr);
		// render email variables using TO emailAddrId
		Map<String, RenderVariable> vars = renderEmailVariables(varNames, addrVo.getEmailAddrId());
		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (String key : keys) {
				RenderVariable var = new RenderVariable(key, variables.get(key), null,
						VariableType.TEXT, CodeType.YES_CODE.getValue(), Boolean.FALSE);
				vars.put(key, var);
			}
		}
		// include mailing list variables
		vars.putAll(MailingListUtil.buildRenderVariables(listVo, toAddr, addrVo.getEmailAddrId()));
		try {
			// now render the templates
			String senderId = listVo.getSenderId();
			String body = renderBo.renderTemplateText(tmpltVo.getBodyText(), senderId, vars);
			String subj = renderBo.renderTemplateText(tmpltVo.getSubject(), senderId, vars);
			renderVo.setSubject(subj);
			renderVo.setBody(body);
		}
		catch (ParseException e) {
			throw new DataValidationException("ParseException caught", e);
		}
		if (vars.containsKey(EmailAddressType.CC_ADDR.getValue())) {
			// set CC if it was passed as an input variable
			RenderVariable cc = vars.get(EmailAddressType.CC_ADDR.getValue());
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
		if (vars.containsKey(EmailAddressType.BCC_ADDR.getValue())) {
			// set BCC if it was passed as an input variable
			RenderVariable bcc = vars.get(EmailAddressType.BCC_ADDR.getValue());
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
		validateFromAddress(listVo.getEmailAddr());
		renderVo.setFromAddr(listVo.getEmailAddr());
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
	 * @return A EmailRenderDo instance
	 * @throws DataValidationException
	 * @throws TemplateException 
	 */
	public EmailRenderDo renderEmailText(String toAddr, Map<String, String> variables,
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
	 * @return A EmailRenderDo instance
	 * @throws DataValidationException
	 * @throws TemplateException 
	 */
	public EmailRenderDo renderEmailText(String toAddr, Map<String, String> variables,
			String subj, String body, String listId, List<String> variableNames)
			throws DataValidationException, TemplateException {
		// first check input TO address
		validateToAddress(toAddr);
		MailingListVo listVo = mailingListDao.getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Mailing List " + listId + " not found.");
		}
		String _from = listVo.getEmailAddr();
		String dispName = listVo.getDisplayName();
		if (StringUtils.isNotBlank(dispName)) {
			_from = dispName + "<" + _from + ">";
		}
		validateFromAddress(_from); // us list address as FROM
		EmailRenderDo renderVo = new EmailRenderDo();
		renderVo.setToAddr(toAddr);
		renderVo.setFromAddr(_from);
		renderVo.setSenderId(listVo.getSenderId());
		renderVo.setMailingListVo(listVo);
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
		EmailAddressVo addrVo = emailAddrDao.findSertAddress(toAddr);
		// retrieve variable values by variable name and email address id
		Map<String, RenderVariable> vars = renderEmailVariables(varNames,
				addrVo.getEmailAddrId());
		// include render variables from input data
		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (String key : keys) {
				RenderVariable var = new RenderVariable(key, variables.get(key), null,
						VariableType.TEXT, CodeType.YES_CODE.getValue(), Boolean.FALSE);
				vars.put(key, var);
			}
		}
		// include mailing list variables
		vars.putAll(MailingListUtil.buildRenderVariables(listVo, addrVo.getEmailAddr(), addrVo.getEmailAddrId()));
		try {
			String bodyText = renderBo.renderTemplateText(body, listVo.getSenderId(), vars);
			String subjText = renderBo.renderTemplateText(subj, listVo.getSenderId(), vars);
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
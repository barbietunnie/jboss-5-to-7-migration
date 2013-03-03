package jpa.service.msgout;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.constant.CarrierCode;
import jpa.constant.CodeType;
import jpa.constant.EmailAddrType;
import jpa.constant.VariableName;
import jpa.constant.VariableType;
import jpa.constant.XHeaderName;
import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.message.BodypartBean;
import jpa.message.MessageBean;
import jpa.message.MsgHeader;
import jpa.model.ClientVariable;
import jpa.model.GlobalVariable;
import jpa.model.message.MessageSource;
import jpa.model.message.TemplateData;
import jpa.model.message.TemplateVariable;
import jpa.service.ClientVariableService;
import jpa.service.EmailAddressService;
import jpa.service.GlobalVariableService;
import jpa.service.message.MessageSourceService;
import jpa.service.message.TemplateDataService;
import jpa.service.message.TemplateVariableService;
import jpa.variable.ErrorVariableVo;
import jpa.variable.RenderVariableVo;
import jpa.variable.Renderer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("renderBo")
@Transactional(propagation=Propagation.REQUIRED)
public class RenderBo {
	static final Logger logger = Logger.getLogger(RenderBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator","\n");
	
	private final Renderer render = Renderer.getInstance();
	
	@Autowired
	private MessageSourceService msgSourceDao;
	@Autowired
	private TemplateDataService templateDao;
	@Autowired
	private ClientVariableService clientVariableDao;
	@Autowired
	private GlobalVariableService globalVariableDao;
	@Autowired
	private TemplateVariableService templateVariableDao;
	@Autowired
	private EmailAddressService emailAddrDao;
	
	public RenderResponse getRenderedEmail(RenderRequest req)
			throws ParseException, AddressException, TemplateException {
		logger.info("in getRenderedEmail(RenderRequest)...");
		if (req == null)
			throw new IllegalArgumentException("RenderRequest is null");
		if (req.startTime==null)
			req.startTime = new Timestamp(new java.util.Date().getTime());

		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariableVos(req, rsp);
		buildRenderedBody(req, rsp);
		buildRenderedSubj(req, rsp);
		buildRenderedAttachments(req, rsp);
		buildRenderedAddrs(req, rsp);
		buildRenderedMisc(req, rsp);
		buildRenderedXHdrs(req, rsp); // to be executed last

		return rsp;
	}
	
	private RenderResponse initRenderResponse(RenderRequest req) throws DataValidationException {
		MessageSource vo = (MessageSource) msgSourceDao.getByMsgSourceId(req.msgSourceId);
		if (vo == null) {
			throw new DataValidationException("MsgSource record not found for " + req.msgSourceId);
		}
		RenderResponse rsp = new RenderResponse(
				vo,
				req.clientId,
				req.startTime,
				new HashMap<String, RenderVariableVo>(),
				new HashMap<String, ErrorVariableVo>(),
				new MessageBean()
				);
		return rsp;
	}
	
	public RenderResponse getRenderedBody(RenderRequest req) throws ParseException, TemplateException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariableVos(req, rsp);
		buildRenderedBody(req, rsp);

		return rsp;
	}
	
	public RenderResponse getRenderedMisc(RenderRequest req) {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariableVos(req, rsp);
		buildRenderedMisc(req, rsp);

		return rsp;
	}
	
	public RenderResponse getRenderedSubj(RenderRequest req)
			throws ParseException, TemplateException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariableVos(req, rsp);
		buildRenderedSubj(req, rsp);

		return rsp;
	}

	public RenderResponse getRenderedAddrs(RenderRequest req) throws AddressException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariableVos(req, rsp);
		buildRenderedAddrs(req, rsp);

		return rsp;
	}

	public RenderResponse getRenderedXHdrs(RenderRequest req) {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariableVos(req, rsp);
		buildRenderedXHdrs(req, rsp);

		return rsp;
	}
	
	private void buildRenderedBody(RenderRequest req, RenderResponse rsp)
			throws ParseException, TemplateException {
		if (isDebugEnabled)
			logger.debug("in buildRenderedBody()...");
		MessageSource srcVo = rsp.msgSourceVo;
		
		String bodyTemplate = null;
		String contentType = null;
		// body template may come from variables
		if (rsp.variableFinal.containsKey(VariableName.BODY_TEMPLATE)
				&& CodeType.YES_CODE.getValue().equalsIgnoreCase(srcVo.getAllowOverride())) {
			RenderVariableVo var = (RenderVariableVo) rsp.variableFinal.get(VariableName.BODY_TEMPLATE);
			if (VariableType.TEXT.equals(var.getVariableType())) {
				bodyTemplate = (String) var.getVariableValue();
				contentType = var.getVariableFormat() == null ? "text/plain" : var
						.getVariableFormat();
			}
		}
		
		if (bodyTemplate == null) {
			TemplateData tmpltVo = templateDao.getByBestMatch(srcVo.getTemplateData().getTemplateDataPK());
			if (tmpltVo == null) {
				throw new DataValidationException("BodyTemplate not found for: "
						+ srcVo.getTemplateData().getTemplateDataPK());
			}
			bodyTemplate = tmpltVo.getBodyTemplate();
			contentType = tmpltVo.getContentType();
		}
		
		String body = render(bodyTemplate, rsp.variableFinal, rsp.variableErrors);
		MessageBean mBean = rsp.messageBean;
		mBean.setContentType(contentType);
		mBean.setBody(body);
	}
	
	private void buildRenderedSubj(RenderRequest req, RenderResponse rsp)
			throws ParseException, TemplateException {
		logger.info("in buildRenderedSubj()...");
		MessageSource srcVo = rsp.msgSourceVo;

		String subjTemplate = null;
		// subject template may come from variables
		if (rsp.variableFinal.containsKey(VariableName.SUBJECT_TEMPLATE)
				&& CodeType.YES_CODE.getValue().equalsIgnoreCase(srcVo.getAllowOverride())) {
			RenderVariableVo var = (RenderVariableVo) rsp.variableFinal.get(VariableName.SUBJECT_TEMPLATE);
			if (VariableType.TEXT.equals(var.getVariableType())) {
				subjTemplate = (String) var.getVariableValue();
			}
		}

		if (subjTemplate == null) {
			TemplateData tmpltVo = templateDao.getByBestMatch(srcVo.getTemplateData().getTemplateDataPK());
			if (tmpltVo == null) {
				throw new DataValidationException("SubjTemplate not found for: "
						+ srcVo.getTemplateData().getTemplateDataPK());
			}
			subjTemplate = tmpltVo.getSubjectTemplate();
		}
		
		String subj = render(subjTemplate, rsp.variableFinal, rsp.variableErrors);
		MessageBean mBean = rsp.messageBean;
		mBean.setSubject(subj);
	}

	private void buildRenderedAttachments(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedAttachments()...");
		Map<String, RenderVariableVo> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;
		Collection<RenderVariableVo> c = varbls.values();
		for (Iterator<RenderVariableVo> it = c.iterator(); it.hasNext();) {
			RenderVariableVo r = it.next();
			if (VariableType.LOB.equals(r.getVariableType()) && r.getVariableValue() != null) {
				BodypartBean attNode = new BodypartBean();
				if (r.getVariableFormat() != null && r.getVariableFormat().indexOf(";") > 0
						&& r.getVariableFormat().indexOf("name=") > 0) {
					attNode.setContentType(r.getVariableFormat());
				}
				else {
					attNode.setContentType(r.getVariableFormat() + "; name=\""
							+ r.getVariableName() + "\"");
				}
				attNode.setDisposition(Part.ATTACHMENT);
				// not necessary, for consistency?
				attNode.setDescription(r.getVariableName());
				attNode.setValue(r.getVariableValue());
				mBean.put(attNode);
			}
		}
	}
	
	private String render(String templateText, Map<String, RenderVariableVo> varbls,
			Map<String, ErrorVariableVo> errors) throws TemplateException, ParseException {
		return render.render(templateText, varbls, errors);
	}
	
	private void buildRenderedAddrs(RenderRequest req, RenderResponse rsp) throws AddressException {
		logger.info("in buildRenderedAddrs()...");
		Map<String, RenderVariableVo> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;

		// variableValue could be type of: String/Address
		Collection<RenderVariableVo> c = varbls.values();
		for (Iterator<RenderVariableVo> it=c.iterator(); it.hasNext();) {
			RenderVariableVo r = it.next();
			if (VariableType.ADDRESS.equals(r.getVariableType()) && r.getVariableValue() != null) {
				if (EmailAddrType.FROM_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setFrom(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof InternetAddress) {
						mBean.setFrom(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (EmailAddrType.REPLYTO_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setReplyto(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof Address) {
						mBean.setReplyto(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (EmailAddrType.TO_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setTo(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof Address) {
						mBean.setTo(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (EmailAddrType.CC_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setCc(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof Address) {
						mBean.setCc(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (EmailAddrType.BCC_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setBcc(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof Address) {
						mBean.setBcc(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
			}
		}
	}
	
	private void buildRenderedMisc(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedMisc()...");
		MessageSource src = rsp.msgSourceVo;
		Map<String, RenderVariableVo> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;

		Collection<RenderVariableVo> c = varbls.values();
		for (Iterator<RenderVariableVo> it=c.iterator(); it.hasNext();) {
			RenderVariableVo r = it.next();
			if (r.getVariableValue() != null && VariableType.TEXT.equals(r.getVariableType())) {
				if (VariableName.PRIORITY.equals(r.getVariableName())) {
					String[] s = { (String) r.getVariableValue() };
					mBean.setPriority(s);
				}
				else if (VariableName.RULE_NAME.equals(r.getVariableName()))
					mBean.setRuleName((String)r.getVariableValue());
				else if (VariableName.CARRIER_CODE.equals(r.getVariableName()))
					mBean.setCarrierCode(CarrierCode.getByValue((String)r.getVariableValue()));
				else if (VariableName.MAILBOX_HOST.equals(r.getVariableName()))
					mBean.setMailboxHost((String)r.getVariableValue());
				else if (VariableName.MAILBOX_HOST.equals(r.getVariableName()))
					mBean.setMailboxHost((String)r.getVariableValue());
				else if (VariableName.MAILBOX_NAME.equals(r.getVariableName()))
					mBean.setMailboxName((String)r.getVariableValue());
				else if (VariableName.MAILBOX_USER.equals(r.getVariableName()))
					mBean.setMailboxUser((String)r.getVariableValue());
				else if (VariableName.FOLDER_NAME.equals(r.getVariableName()))
					mBean.setFolderName((String)r.getVariableValue());
				else if (VariableName.CLIENT_ID.equals(r.getVariableName()))
					mBean.setClientId((String)r.getVariableValue());
				else if (VariableName.CUSTOMER_ID.equals(r.getVariableName()))
					mBean.setCustId((String)r.getVariableValue());
				else if (VariableName.TO_PLAIN_TEXT.equals(r.getVariableName()))
					mBean.setToPlainText(CodeType.YES_CODE.getValue().equals((String)r.getVariableValue()));
			}
			else if (r.getVariableValue() != null && VariableType.NUMERIC.equals(r.getVariableType())) {
				if (VariableName.MSG_REF_ID.equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof Integer)
						mBean.setMsgRefId((Integer) r.getVariableValue());
					else if (r.getVariableValue() instanceof String)
						mBean.setMsgRefId(Integer.valueOf((String) r.getVariableValue()));
				}
			}
			else if (VariableType.DATETIME.equals(r.getVariableType())) {
				if (VariableName.SEND_DATE.equals(r.getVariableName())) {
					if (r.getVariableValue() == null) {
						mBean.setSendDate(new java.util.Date());
					}
					else {
						SimpleDateFormat fmt = new SimpleDateFormat(RenderVariableVo.DEFAULT_DATETIME_FORMAT);
						if (r.getVariableFormat()!=null) {
							fmt.applyPattern(r.getVariableFormat());
						}
						if (r.getVariableValue() instanceof String) {
							try {
								java.util.Date date = fmt.parse((String) r.getVariableValue());
								mBean.setSendDate(date);
							}
							catch (ParseException e) {
								logger.error("ParseException caught", e);
								mBean.setSendDate(new java.util.Date());
							}
						}
						else if (r.getVariableValue() instanceof java.util.Date) {
							mBean.setSendDate((java.util.Date) r.getVariableValue());
						}
					}
				}
			}
		}
		// make sure CarrierCode is populated
		if (mBean.getCarrierCode() == null) {
			mBean.setCarrierCode(CarrierCode.getByValue(rsp.msgSourceVo.getCarrierCode()));
		}

		if (src.isExcludingIdToken()) {
			mBean.setEmBedEmailId(Boolean.valueOf(false));
		}

		if (src.isSaveMsgStream())
			mBean.setSaveMsgStream(true);
		else
			mBean.setSaveMsgStream(false);
	}
	
	/*
	 * If MessageBean's ClientId field is not valued, and X-Client_id header is
	 * found and valued, populate MessageBean's ClientId field with the value
	 * from X-Client_id header. <br> 
	 */
	private void buildRenderedXHdrs(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedXHdrs()...");
		// MessageSource src = rsp.msgSourceVo;
		Map<String, RenderVariableVo> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;

		List<MsgHeader> headers = new ArrayList<MsgHeader>();

		Collection<RenderVariableVo> c = varbls.values();
		for (Iterator<RenderVariableVo> it=c.iterator(); it.hasNext();) {
			RenderVariableVo r = it.next();
			if (VariableType.X_HEADER.equals(r.getVariableType()) && r.getVariableValue() != null) {
				MsgHeader msgHeader = new MsgHeader();
				msgHeader.setName(r.getVariableName());
				msgHeader.setValue((String) r.getVariableValue());
				headers.add(msgHeader);
				// set ClientId for MessageBean
				if (XHeaderName.CLIENT_ID.getValue().equals(r.getVariableName())) {
					if (StringUtils.isBlank(mBean.getClientId()))
						mBean.setClientId((String) r.getVariableValue());
				}
				else if (XHeaderName.CUSTOMER_ID.getValue().equals(r.getVariableName())) {
					if (StringUtils.isBlank(mBean.getCustId()))
						mBean.setCustId((String) r.getVariableValue());
				}
			}
		}
		mBean.setHeaders(headers);
	}
	
	private void buildRenderVariableVos(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderVariableVos()...");
		
		MessageSource msgSourceVo = msgSourceDao.getByMsgSourceId(req.msgSourceId);
		rsp.msgSourceVo = msgSourceVo;
		
		// retrieve variables
		Collection<GlobalVariable> globalVariables = globalVariableDao.getCurrent();
		Collection<ClientVariable> clientVariables = clientVariableDao.getCurrentByClientId(
				req.clientId);
		Collection<TemplateVariable> templateVariables = msgSourceVo.getTemplateVariableList();
		
		// convert variables into Map
		Map<String, RenderVariableVo> g_ht = GlobalVariablesToHashMap(globalVariables);
		Map<String, RenderVariableVo> c_ht = ClientVariablesToHashMap(clientVariables);
		Map<String, RenderVariableVo> t_ht = TemplateVariablesToHashMap(templateVariables);
		
		// variables from req and MsgSource table
		Map<String, RenderVariableVo> s_ht = new HashMap<String, RenderVariableVo>();
		RenderVariableVo vreq = new RenderVariableVo(
				VariableName.CLIENT_ID.getValue(),
				req.clientId,
				null,
				VariableType.TEXT, 
				CodeType.YES_CODE.getValue(),
				Boolean.TRUE);
		s_ht.put(vreq.getVariableName(), vreq);
		
		vreq = new RenderVariableVo(
			EmailAddrType.FROM_ADDR.getValue(),
			msgSourceVo.getFromAddress(),
			null,
			VariableType.ADDRESS, 
			CodeType.YES_CODE.getValue(),
			Boolean.TRUE);
		s_ht.put(vreq.getVariableName(), vreq);
		
		if (msgSourceVo.getReplyToAddress()!=null) {
			vreq = new RenderVariableVo(
				EmailAddrType.REPLYTO_ADDR.getValue(),
				msgSourceVo.getReplyToAddress(),
				null,
				VariableType.ADDRESS, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
			s_ht.put(vreq.getVariableName(), vreq);
		}
		
		// get Runtime variables
		Map<String, RenderVariableVo> r_ht = req.variableOverrides;
		if (r_ht==null) r_ht = new HashMap<String, RenderVariableVo>();
		
		// error hash table
		Map<String, ErrorVariableVo> err_ht = new HashMap<String, ErrorVariableVo>();
		
		// merge variable hash tables
		mergeHashMaps(s_ht, g_ht, err_ht);
		mergeHashMaps(c_ht, g_ht, err_ht);
		mergeHashMaps(t_ht, g_ht, err_ht);
		mergeHashMaps(r_ht, g_ht, err_ht);
		
		verifyHashMap(g_ht, err_ht);
		
		rsp.variableFinal.putAll(g_ht);
		rsp.variableErrors.putAll(err_ht);
	}
	
	private void mergeHashMaps(Map<String, RenderVariableVo> from,
			Map<String, RenderVariableVo> to, Map<String, ErrorVariableVo> error) {
		Set<String> keys = from.keySet();
		for (Iterator<String> it=keys.iterator(); it.hasNext();) {
			String name = it.next();
			if (to.get(name) != null) {
				RenderVariableVo req = (RenderVariableVo) to.get(name);
				if (CodeType.YES_CODE.getValue().equalsIgnoreCase(req.getAllowOverride())
						|| CodeType.MANDATORY_CODE.getValue().equalsIgnoreCase(req.getAllowOverride())) {
					to.put(name, from.get(name));
				}
				else {
					RenderVariableVo r = (RenderVariableVo) from.get(name);
					ErrorVariableVo err = new ErrorVariableVo(
							r.getVariableName(), 
							r.getVariableValue(), 
							"Variable Override is not allowed.");
					error.put(name, err);
				}
			}
			else {
				to.put(name, from.get(name));
			}
		}
	}
	
	private void verifyHashMap(Map<String, RenderVariableVo> ht,
			Map<String, ErrorVariableVo> error) {
		Set<String> keys = ht.keySet();
		for (Iterator<String> it=keys.iterator(); it.hasNext();) {
			String name = it.next();
			RenderVariableVo req = (RenderVariableVo) ht.get(name);
			if (CodeType.MANDATORY_CODE.getValue().equalsIgnoreCase(req.getAllowOverride())) {
				ErrorVariableVo err = new ErrorVariableVo(
						req.getVariableName(), 
						req.getVariableValue(), 
						"Variable Override is mandatory.");
				error.put(name, err);
			}
		}
	}
	
	private HashMap<String, RenderVariableVo> GlobalVariablesToHashMap(Collection<GlobalVariable> c) {
		HashMap<String, RenderVariableVo> ht = new HashMap<String, RenderVariableVo>();
		for (Iterator<GlobalVariable> it = c.iterator(); it.hasNext();) {
			GlobalVariable req = it.next();
			RenderVariableVo r = new RenderVariableVo(
				req.getGlobalVariablePK().getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.valueOf(req.getVariableType()), 
				req.getAllowOverride(), 
				req.isRequired()
				);
			ht.put(req.getGlobalVariablePK().getVariableName(), r);
		}
		return ht;
	}
	
	private HashMap<String, RenderVariableVo> ClientVariablesToHashMap(Collection<ClientVariable> c) {
		HashMap<String, RenderVariableVo> ht = new HashMap<String, RenderVariableVo>();
		for (Iterator<ClientVariable> it = c.iterator(); it.hasNext();) {
			ClientVariable req = it.next();
			RenderVariableVo r = new RenderVariableVo(
				req.getClientVariablePK().getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.valueOf(req.getVariableType()), 
				req.getAllowOverride(), 
				req.isRequired()
				);
			ht.put(req.getClientVariablePK().getVariableName(), r);
		}
		return ht;
	}
	
	private HashMap<String, RenderVariableVo> TemplateVariablesToHashMap(
			Collection<TemplateVariable> c) {
		HashMap<String, RenderVariableVo> ht = new HashMap<String, RenderVariableVo>();
		for (Iterator<TemplateVariable> it = c.iterator(); it.hasNext();) {
			TemplateVariable req = it.next();
			RenderVariableVo r = new RenderVariableVo(
				req.getTemplateVariablePK().getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.valueOf(req.getVariableType()), 
				req.getAllowOverride(), 
				req.isRequired()
				);
			ht.put(req.getTemplateVariablePK().getVariableName(), r);
		}
		return ht;
	}
}

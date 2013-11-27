package com.es.bo.render;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.es.dao.address.EmailAddressDao;
import com.es.dao.sender.GlobalVariableDao;
import com.es.dao.sender.SenderVariableDao;
import com.es.dao.template.MsgSourceDao;
import com.es.dao.template.TemplateDataDao;
import com.es.dao.template.TemplateVariableDao;
import com.es.data.constant.CarrierCode;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.EmailAddressType;
import com.es.data.constant.VariableName;
import com.es.data.constant.VariableType;
import com.es.data.constant.XHeaderName;
import com.es.exception.DataValidationException;
import com.es.msgbean.BodypartBean;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MsgHeader;
import com.es.vo.template.GlobalVariableVo;
import com.es.vo.template.MsgSourceVo;
import com.es.vo.template.SenderVariableVo;
import com.es.vo.template.TemplateDataVo;
import com.es.vo.template.TemplateVariableVo;

@Component("renderBo")
public class RenderBo {
	static final Logger logger = Logger.getLogger(RenderBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator","\n");
	
	private final Renderer render = Renderer.getInstance();
	
	@Autowired
	private MsgSourceDao msgSourceDao;
	@Autowired
	private TemplateDataDao templateDataDao;
	@Autowired
	private SenderVariableDao senderVariableDao;
	@Autowired
	private GlobalVariableDao globalVariableDao;
	@Autowired
	private TemplateVariableDao templateVariableDao;
	@Autowired
	private EmailAddressDao emailAddressDao;
	
	public RenderResponse getRenderedEmail(RenderRequest req) throws DataValidationException,
			ParseException, AddressException {
		logger.info("in getRenderedEmail(RenderRequest)...");
		if (req == null) {
			throw new IllegalArgumentException("RenderRequest is null");
		}
		if (req.startTime==null) {
			req.startTime = new Timestamp(new java.util.Date().getTime());
		}
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedBody(req, rsp);
		buildRenderedSubj(req, rsp);
		buildRenderedAttachments(req, rsp);
		buildRenderedAddrs(req, rsp);
		buildRenderedMisc(req, rsp);
		buildRenderedXHdrs(req, rsp); // to be executed last

		return rsp;
	}
	
	private RenderResponse initRenderResponse(RenderRequest req) throws DataValidationException {
		MsgSourceVo vo = msgSourceDao.getByPrimaryKey(req.msgSourceId);
		if (vo == null) {
			throw new DataValidationException("MsgSource record not found for " + req.msgSourceId);
		}
		RenderResponse rsp = new RenderResponse(
				vo,
				req.senderId,
				req.startTime,
				new HashMap<String, RenderVariable>(),
				new HashMap<String, RenderVariable>(),
				new MessageBean()
				);
		return rsp;
	}
	
	public RenderResponse getRenderedBody(RenderRequest req) throws DataValidationException,
			ParseException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedBody(req, rsp);

		return rsp;
	}
	
	public RenderResponse getRenderedMisc(RenderRequest req) throws DataValidationException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedMisc(req, rsp);

		return rsp;
	}
	
	public RenderResponse getRenderedSubj(RenderRequest req) throws DataValidationException,
			ParseException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedSubj(req, rsp);

		return rsp;
	}

	public RenderResponse getRenderedAddrs(RenderRequest req) throws DataValidationException,
			AddressException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedAddrs(req, rsp);

		return rsp;
	}

	public RenderResponse getRenderedXHdrs(RenderRequest req) throws DataValidationException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedXHdrs(req, rsp);

		return rsp;
	}
	
	private void buildRenderedBody(RenderRequest req, RenderResponse rsp)
			throws DataValidationException, ParseException {
		if (isDebugEnabled)
			logger.debug("in buildRenderedBody()...");
		MsgSourceVo srcVo = rsp.msgSourceVo;
		
		String bodyTemplate = null;
		String contentType = null;
		// body template may come from variables
		if (rsp.variableFinal.containsKey(VariableName.BODY_TEMPLATE.getValue())
				&& CodeType.YES_CODE.getValue().equalsIgnoreCase(srcVo.getAllowOverride())) {
			RenderVariable var = rsp.variableFinal.get(VariableName.BODY_TEMPLATE.getValue());
			if (VariableType.TEXT.equals(var.getVariableType())) {
				bodyTemplate = (String) var.getVariableValue();
				contentType = var.getVariableFormat() == null ? "text/plain" : var.getVariableFormat();
			}
		}
		
		if (bodyTemplate == null) {
			TemplateDataVo tmpltVo = getTemplateDataDao().getByBestMatch(srcVo.getTemplateDataId(),
					req.senderId, req.startTime);
			if (tmpltVo == null) {
				throw new DataValidationException("BodyTemplate not found for: "
						+ srcVo.getTemplateDataId() + "/" + req.senderId + "/" + req.startTime);
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
			throws DataValidationException, ParseException {
		logger.info("in buildRenderedSubj()...");
		MsgSourceVo srcVo = rsp.msgSourceVo;

		String subjTemplate = null;
		// subject template may come from variables
		if (rsp.variableFinal.containsKey(VariableName.SUBJECT_TEMPLATE.getValue())
				&& CodeType.YES_CODE.getValue().equalsIgnoreCase(srcVo.getAllowOverride())) {
			RenderVariable var = rsp.variableFinal.get(VariableName.SUBJECT_TEMPLATE.getValue());
			if (VariableType.TEXT.equals(var.getVariableType())) {
				subjTemplate = (String) var.getVariableValue();
			}
		}

		if (subjTemplate == null) {
			TemplateDataVo tmpltVo = getTemplateDataDao().getByBestMatch(srcVo.getTemplateDataId(),
					req.senderId, req.startTime);
			if (tmpltVo == null) {
				throw new DataValidationException("SubjTemplate not found for: "
						+ srcVo.getTemplateDataId() + "/" + req.senderId + "/" + req.startTime);
			}
			subjTemplate = tmpltVo.getSubjTemplate();
		}
		
		String subj = render(subjTemplate, rsp.variableFinal, rsp.variableErrors);
		MessageBean mBean = rsp.messageBean;
		mBean.setSubject(subj);
	}

	private void buildRenderedAttachments(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedAttachments()...");
		Map<String, RenderVariable> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;
		Collection<RenderVariable> c = varbls.values();
		for (Iterator<RenderVariable> it = c.iterator(); it.hasNext();) {
			RenderVariable r = it.next();
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
	
	private String render(String templateText, Map<String, RenderVariable> varbls,
			Map<String, RenderVariable> errors) throws DataValidationException, ParseException {
		return render.render(templateText, varbls, errors);
	}
	
	private void buildRenderedAddrs(RenderRequest req, RenderResponse rsp) throws AddressException {
		logger.info("in buildRenderedAddrs()...");
		Map<String, RenderVariable> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;

		// variableValue could be type of: String/Address
		Collection<RenderVariable> c = varbls.values();
		for (Iterator<RenderVariable> it=c.iterator(); it.hasNext();) {
			RenderVariable r = it.next();
			if (VariableType.ADDRESS.equals(r.getVariableType()) && r.getVariableValue() != null) {
				if (EmailAddressType.FROM_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setFrom(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof InternetAddress) {
						mBean.setFrom(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (EmailAddressType.REPLYTO_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setReplyto(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof Address) {
						mBean.setReplyto(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (EmailAddressType.TO_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setTo(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof Address) {
						mBean.setTo(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (EmailAddressType.CC_ADDR.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof String)
						mBean.setCc(InternetAddress.parse((String) r.getVariableValue()));
					else if (r.getVariableValue() instanceof Address) {
						mBean.setCc(InternetAddress.parse(((Address)r.getVariableValue()).toString()));
					}
				}
				else if (EmailAddressType.BCC_ADDR.getValue().equals(r.getVariableName())) {
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
		MsgSourceVo src = rsp.msgSourceVo;
		Map<String, RenderVariable> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;

		Collection<RenderVariable> c = varbls.values();
		for (Iterator<RenderVariable> it=c.iterator(); it.hasNext();) {
			RenderVariable r = it.next();
			if (r.getVariableValue() != null && VariableType.TEXT.equals(r.getVariableType())) {
				if (VariableName.PRIORITY.getValue().equals(r.getVariableName())) {
					String[] s = { (String) r.getVariableValue() };
					mBean.setPriority(s);
				}
				else if (VariableName.RULE_NAME.getValue().equals(r.getVariableName()))
					mBean.setRuleName((String)r.getVariableValue());
				else if (VariableName.CARRIER_CODE.getValue().equals(r.getVariableName()))
					mBean.setCarrierCode(CarrierCode.getByValue((String)r.getVariableValue()));
				else if (VariableName.MAILBOX_HOST.getValue().equals(r.getVariableName()))
					mBean.setMailboxHost((String)r.getVariableValue());
				else if (VariableName.MAILBOX_HOST.getValue().equals(r.getVariableName()))
					mBean.setMailboxHost((String)r.getVariableValue());
				else if (VariableName.MAILBOX_NAME.getValue().equals(r.getVariableName()))
					mBean.setMailboxName((String)r.getVariableValue());
				else if (VariableName.MAILBOX_USER.getValue().equals(r.getVariableName()))
					mBean.setMailboxUser((String)r.getVariableValue());
				else if (VariableName.FOLDER_NAME.getValue().equals(r.getVariableName()))
					mBean.setFolderName((String)r.getVariableValue());
				else if (VariableName.SENDER_ID.getValue().equals(r.getVariableName()))
					mBean.setSenderId((String)r.getVariableValue());
				else if (VariableName.SUBSCRIBER_ID.getValue().equals(r.getVariableName()))
					mBean.setSubrId((String)r.getVariableValue());
				else if (VariableName.TO_PLAIN_TEXT.getValue().equals(r.getVariableName()))
					mBean.setToPlainText(CodeType.YES_CODE.getValue().equals((String)r.getVariableValue()));
			}
			else if (r.getVariableValue() != null && VariableType.NUMERIC.equals(r.getVariableType())) {
				if (VariableName.MSG_REF_ID.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() instanceof Long)
						mBean.setMsgRefId(((Long) r.getVariableValue()).intValue());
					else if (r.getVariableValue() instanceof String)
						mBean.setMsgRefId(Integer.valueOf((String) r.getVariableValue()));
				}
			}
			else if (VariableType.DATETIME.equals(r.getVariableType())) {
				if (VariableName.SEND_DATE.getValue().equals(r.getVariableName())) {
					if (r.getVariableValue() == null) {
						mBean.setSendDate(new java.util.Date());
					}
					else {
						SimpleDateFormat fmt = new SimpleDateFormat(Constants.DEFAULT_DATETIME_FORMAT);
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

		if (CodeType.YES_CODE.getValue().equalsIgnoreCase(src.getExcludingIdToken())) {
			mBean.setEmBedEmailId(Boolean.valueOf(false));
		}

		if (CodeType.YES_CODE.getValue().equalsIgnoreCase(src.getSaveMsgStream())) {
			mBean.setSaveMsgStream(true);
		}
		else {
			mBean.setSaveMsgStream(false);
		}
	}
	
	/*
	 * If MessageBean's SenderId field is not valued, and X-Sender_id header is
	 * found and valued, populate MessageBean's SenderId field with the value
	 * from X-Sender_id header. <br> 
	 */
	private void buildRenderedXHdrs(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedXHdrs()...");
		// MsgSourceVo src = rsp.msgSourceVo;
		Map<String, RenderVariable> varbls = rsp.variableFinal;
		MessageBean mBean = rsp.messageBean;

		List<MsgHeader> headers = new ArrayList<MsgHeader>();

		Collection<RenderVariable> c = varbls.values();
		for (Iterator<RenderVariable> it=c.iterator(); it.hasNext();) {
			RenderVariable r = it.next();
			if (VariableType.X_HEADER.equals(r.getVariableType()) && r.getVariableValue() != null) {
				MsgHeader msgHeader = new MsgHeader();
				msgHeader.setName(r.getVariableName());
				msgHeader.setValue((String) r.getVariableValue());
				headers.add(msgHeader);
				// set SenderId for MessageBean
				if (XHeaderName.SENDER_ID.getValue().equals(r.getVariableName())) {
					if (StringUtils.isEmpty(mBean.getSenderId())) {
						mBean.setSenderId((String) r.getVariableValue());
					}
				}
				else if (XHeaderName.SUBSCRIBER_ID.getValue().equals(r.getVariableName())) {
					if (StringUtils.isEmpty(mBean.getSubrId())) {
						mBean.setSubrId((String) r.getVariableValue());
					}
				}
			}
		}
		mBean.setHeaders(headers);
	}
	
	private void buildRenderVariables(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderVariables()...");
		
		MsgSourceVo msgSourceVo = msgSourceDao.getByPrimaryKey(req.msgSourceId);
		rsp.msgSourceVo = msgSourceVo;
		
		// retrieve variables
		Collection<GlobalVariableVo> globalVariables = getGlobalVariableDao().getCurrent();
		Collection<SenderVariableVo> senderVariables = getSenderVariableDao().getCurrentBySenderId(
				req.senderId);
		Collection<TemplateVariableVo> templateVariables = getTemplateVariableDao()
				.getCurrentByTemplateId(msgSourceVo.getTemplateVariableId(), req.senderId);
		
		// convert variables into Map
		Map<String, RenderVariable> g_ht = GlobalVariablesToMap(globalVariables);
		Map<String, RenderVariable> c_ht = SenderVariablesToMap(senderVariables);
		Map<String, RenderVariable> t_ht = TemplateVariablesToMap(templateVariables);
		
		// variables from req and MsgSource table
		Map<String, RenderVariable> s_ht = new HashMap<String, RenderVariable>();
		RenderVariable vreq = new RenderVariable(
				VariableName.SENDER_ID.getValue(),
				req.senderId,
				null,
				VariableType.TEXT, 
				CodeType.YES_CODE.getValue(),
				CodeType.YES_CODE.getValue(),
				null);
		s_ht.put(vreq.getVariableName(), vreq);
		
		vreq = new RenderVariable(
			EmailAddressType.FROM_ADDR.getValue(),
			emailAddressDao.getByAddrId(msgSourceVo.getFromAddrId()).getEmailAddr(),
			null,
			VariableType.ADDRESS, 
			CodeType.YES_CODE.getValue(),
			CodeType.YES_CODE.getValue(),
			null);
		s_ht.put(vreq.getVariableName(), vreq);
		
		if (msgSourceVo.getReplyToAddrId()!=null) {
			vreq = new RenderVariable(
				EmailAddressType.REPLYTO_ADDR.getValue(),
				emailAddressDao.getByAddrId(msgSourceVo.getReplyToAddrId()).getEmailAddr(),
				null,
				VariableType.ADDRESS, 
				CodeType.YES_CODE.getValue(),
				CodeType.NO_CODE.getValue(),
				null);
			s_ht.put(vreq.getVariableName(), vreq);
		}
		
		// get Runtime variables
		Map<String, RenderVariable> r_ht = req.variableOverrides;
		if (r_ht==null) r_ht = new HashMap<String, RenderVariable>();
		
		// error hash table
		Map<String, RenderVariable> err_ht = new HashMap<String, RenderVariable>();
		
		// merge variable hash tables
		mergeMaps(s_ht, g_ht, err_ht);
		mergeMaps(c_ht, g_ht, err_ht);
		mergeMaps(t_ht, g_ht, err_ht);
		mergeMaps(r_ht, g_ht, err_ht);
		
		verifyMap(g_ht, err_ht);
		
		rsp.variableFinal.putAll(g_ht);
		rsp.variableErrors.putAll(err_ht);
	}
	
	private void mergeMaps(Map<String, RenderVariable> from,
			Map<String, RenderVariable> to, Map<String, RenderVariable> error) {
		Set<String> keys = from.keySet();
		for (Iterator<String> it=keys.iterator(); it.hasNext();) {
			String name = it.next();
			if (to.get(name) != null) {
				RenderVariable req = (RenderVariable) to.get(name);
				if (CodeType.YES_CODE.getValue().equals(req.getAllowOverride())
						|| CodeType.MANDATORY_CODE.getValue().equals(req.getAllowOverride())) {
					to.put(name, from.get(name));
				}
				else {
					RenderVariable r = from.get(name);
					r.setErrorMsg("Variable Override is not allowed.");
					error.put(name, r);
				}
			}
			else {
				to.put(name, from.get(name));
			}
		}
	}
	
	private void verifyMap(Map<String, RenderVariable> ht,
			Map<String, RenderVariable> error) {
		Set<String> keys = ht.keySet();
		for (Iterator<String> it=keys.iterator(); it.hasNext();) {
			String name = it.next();
			RenderVariable req = (RenderVariable) ht.get(name);
			if (CodeType.MANDATORY_CODE.getValue().equals(req.getAllowOverride())) {
				req.setErrorMsg("Variable Override is mandatory.");
				error.put(name, req);
			}
		}
	}
	
	private Map<String, RenderVariable> GlobalVariablesToMap(Collection<GlobalVariableVo> c) {
		Map<String, RenderVariable> ht = new HashMap<String, RenderVariable>();
		for (Iterator<GlobalVariableVo> it = c.iterator(); it.hasNext();) {
			GlobalVariableVo req = it.next();
			RenderVariable r = new RenderVariable(
				req.getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.getByValue(req.getVariableType()), 
				req.getAllowOverride(), 
				req.getRequired(),
				""
				);
			ht.put(req.getVariableName(), r);
		}
		return ht;
	}
	
	private Map<String, RenderVariable> SenderVariablesToMap(Collection<SenderVariableVo> c) {
		Map<String, RenderVariable> ht = new HashMap<String, RenderVariable>();
		for (Iterator<SenderVariableVo> it = c.iterator(); it.hasNext();) {
			SenderVariableVo req = it.next();
			RenderVariable r = new RenderVariable(
				req.getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.getByValue(req.getVariableType()), 
				req.getAllowOverride(), 
				req.getRequired(),
				""
				);
			ht.put(req.getVariableName(), r);
		}
		return ht;
	}
	
	private Map<String, RenderVariable> TemplateVariablesToMap(
			Collection<TemplateVariableVo> c) {
		Map<String, RenderVariable> ht = new HashMap<String, RenderVariable>();
		for (Iterator<TemplateVariableVo> it = c.iterator(); it.hasNext();) {
			TemplateVariableVo req = it.next();
			RenderVariable r = new RenderVariable(
				req.getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.getByValue(req.getVariableType()), 
				req.getAllowOverride(), 
				req.getRequired(),
				""
				);
			ht.put(req.getVariableName(), r);
		}
		return ht;
	}

	public MsgSourceDao getMsgSourceDao() {
		return msgSourceDao;
	}

	public TemplateDataDao getTemplateDataDao() {
		return templateDataDao;
	}

	public SenderVariableDao getSenderVariableDao() {
		return senderVariableDao;
	}

	public GlobalVariableDao getGlobalVariableDao() {
		return globalVariableDao;
	}

	public TemplateVariableDao getTemplateVariableDao() {
		return templateVariableDao;
	}

	public EmailAddressDao getEmailAddressDao() {
		return emailAddressDao;
	}

}

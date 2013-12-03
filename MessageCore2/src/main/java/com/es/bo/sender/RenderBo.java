package com.es.bo.sender;

import java.math.BigDecimal;
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.outbox.MsgOutboxBo;
import com.es.bo.render.ErrorVariable;
import com.es.bo.render.RenderRequest;
import com.es.bo.render.RenderResponse;
import com.es.bo.render.RenderVariable;
import com.es.bo.render.Renderer;
import com.es.core.util.HtmlUtil;
import com.es.core.util.SpringUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.outbox.MsgRenderedDao;
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
import com.es.exception.TemplateException;
import com.es.msgbean.BodypartBean;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MsgHeader;
import com.es.vo.outbox.MsgRenderedVo;
import com.es.vo.template.GlobalVariableVo;
import com.es.vo.template.MsgSourceVo;
import com.es.vo.template.SenderVariableVo;
import com.es.vo.template.TemplateDataVo;
import com.es.vo.template.TemplateVariableVo;

@Component("renderBo")
@Transactional(propagation=Propagation.REQUIRED)
public class RenderBo implements java.io.Serializable {
	private static final long serialVersionUID = -8967835234168609528L;
	static final Logger logger = Logger.getLogger(RenderBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator","\n");
	
	private final Renderer render = Renderer.getInstance();
	
	@Autowired
	private MsgSourceDao msgSourceDao;
	@Autowired
	private TemplateDataDao templateDao;
	@Autowired
	private SenderVariableDao senderVariableDao;
	@Autowired
	private GlobalVariableDao globalVariableDao;
	@Autowired
	private TemplateVariableDao templateVariableDao;
	@Autowired
	private EmailAddressDao emailAddrDao;
	
	public static void main(String[] args) {
		RenderBo bo = SpringUtil.getAppContext().getBean(RenderBo.class);
		MsgOutboxBo outboxBo = SpringUtil.getAppContext().getBean(MsgOutboxBo.class);
		MsgRenderedDao rndrDao = SpringUtil.getAppContext().getBean(MsgRenderedDao.class);
		SpringUtil.beginTransaction();
		try {
			MsgRenderedVo mr = null;
			try {
				mr = rndrDao.getFirstRecord();
			}
			catch (EmptyResultDataAccessException e) {
				throw new IllegalStateException("Msg_Rendered table is empty.");
			}
			RenderRequest req = outboxBo.getRenderRequestByPK(mr.getRowId());
			RenderVariable vo = new RenderVariable(
					EmailAddressType.TO_ADDR.getValue(),
					"testto@localhost",
					VariableType.ADDRESS);
			req.getVariableOverrides().put(vo.getVariableName(), vo);
			RenderResponse rsp = bo.getRenderedEmail(req);
			logger.info(req);
			logger.info(rsp);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			SpringUtil.commitTransaction();
		}
	}
	
	public RenderResponse getRenderedEmail(RenderRequest req)
			throws ParseException, AddressException, TemplateException {
		logger.info("in getRenderedEmail(RenderRequest)...");
		if (req == null) {
			throw new IllegalArgumentException("RenderRequest is null");
		}
		if (req.getStartTime()==null) {
			req.setStartTime(new Timestamp(System.currentTimeMillis()));
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
		MsgSourceVo vo = null;
		try {
			vo = msgSourceDao.getByPrimaryKey(req.getMsgSourceId());
		}
		catch (EmptyResultDataAccessException e) {
			throw new DataValidationException("Msg_Source record not found for " + req.getMsgSourceId());
		}
		RenderResponse rsp = new RenderResponse(
				vo,
				req.getSenderId(),
				req.getStartTime(),
				new HashMap<String, RenderVariable>(),
				new HashMap<String, ErrorVariable>(),
				new MessageBean()
				);
		return rsp;
	}
	
	public RenderResponse getRenderedBody(RenderRequest req) throws ParseException, TemplateException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedBody(req, rsp);

		return rsp;
	}
	
	public RenderResponse getRenderedMisc(RenderRequest req) {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedMisc(req, rsp);

		return rsp;
	}
	
	public RenderResponse getRenderedSubj(RenderRequest req)
			throws ParseException, TemplateException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedSubj(req, rsp);

		return rsp;
	}

	public RenderResponse getRenderedAddrs(RenderRequest req) throws AddressException {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedAddrs(req, rsp);

		return rsp;
	}

	public RenderResponse getRenderedXHdrs(RenderRequest req) {
		RenderResponse rsp = initRenderResponse(req);
		buildRenderVariables(req, rsp);
		buildRenderedXHdrs(req, rsp);

		return rsp;
	}
	
	private void buildRenderedBody(RenderRequest req, RenderResponse rsp)
			throws ParseException, TemplateException {
		if (isDebugEnabled)
			logger.debug("in buildRenderedBody()...");
		MsgSourceVo srcVo = rsp.getMsgSourceVo();
		
		String bodyTemplate = null;
		String contentType = null;
		// body template may come from variables
		if (rsp.getVariableFinal().containsKey(VariableName.BODY_TEMPLATE.getValue())
				&& CodeType.YES_CODE.getValue().equalsIgnoreCase(srcVo.getAllowOverride())) {
			RenderVariable var = rsp.getVariableFinal().get(VariableName.BODY_TEMPLATE.getValue());
			if (VariableType.TEXT.equals(var.getVariableType())) {
				bodyTemplate = (String) var.getVariableValue();
				contentType = (var.getVariableFormat() == null ? (HtmlUtil.isHTML(bodyTemplate) ? "text/html" : "text/plain")
						: var.getVariableFormat());
			}
		}
		
		if (bodyTemplate == null) {
			TemplateDataVo tmpltVo = templateDao.getByBestMatch(srcVo.getTemplateDataId(), req.getSenderId(), req.getStartTime());
			if (tmpltVo == null) {
				throw new DataValidationException("BodyTemplate not found for: "
						+ srcVo.getTemplateDataId() + "/" + req.getSenderId() + "/" + req.getStartTime());
			}
			bodyTemplate = tmpltVo.getBodyTemplate();
			contentType = tmpltVo.getContentType();
		}
		
		String body = render(bodyTemplate, rsp.getVariableFinal(), rsp.getVariableErrors());
		MessageBean mBean = rsp.getMessageBean();
		mBean.setContentType(contentType);
		mBean.setBody(body);
	}
	
	private void buildRenderedSubj(RenderRequest req, RenderResponse rsp)
			throws ParseException, TemplateException {
		logger.info("in buildRenderedSubj()...");
		MsgSourceVo srcVo = rsp.getMsgSourceVo();

		String subjTemplate = null;
		// subject template may come from variables
		if (rsp.getVariableFinal().containsKey(VariableName.SUBJECT_TEMPLATE.getValue())
				&& CodeType.YES_CODE.getValue().equalsIgnoreCase(srcVo.getAllowOverride())) {
			RenderVariable var = rsp.getVariableFinal().get(VariableName.SUBJECT_TEMPLATE.getValue());
			if (VariableType.TEXT.equals(var.getVariableType())) {
				subjTemplate = (String) var.getVariableValue();
			}
		}

		if (subjTemplate == null) {
			TemplateDataVo tmpltVo = templateDao.getByBestMatch(srcVo.getTemplateDataId(), req.getSenderId(), req.getStartTime());
			if (tmpltVo == null) {
				throw new DataValidationException("SubjTemplate not found for: "
						+ srcVo.getTemplateDataId() + "/" + req.getSenderId() + "/" + req.getStartTime());
			}
			subjTemplate = tmpltVo.getSubjTemplate();
		}
		
		String subj = render(subjTemplate, rsp.getVariableFinal(), rsp.getVariableErrors());
		MessageBean mBean = rsp.getMessageBean();
		mBean.setSubject(subj);
	}

	private void buildRenderedAttachments(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedAttachments()...");
		Map<String, RenderVariable> varbls = rsp.getVariableFinal();
		MessageBean mBean = rsp.getMessageBean();
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
			Map<String, ErrorVariable> errors) throws TemplateException, ParseException {
		return render.render(templateText, varbls, errors);
	}
	
	private void buildRenderedAddrs(RenderRequest req, RenderResponse rsp) throws AddressException {
		logger.info("in buildRenderedAddrs()...");
		Map<String, RenderVariable> varbls = rsp.getVariableFinal();
		MessageBean mBean = rsp.getMessageBean();

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
		MsgSourceVo src = rsp.getMsgSourceVo();
		Map<String, RenderVariable> varbls = rsp.getVariableFinal();
		MessageBean mBean = rsp.getMessageBean();

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
					if (r.getVariableValue() instanceof BigDecimal)
						mBean.setMsgRefId(((BigDecimal) r.getVariableValue()).longValue());
					else if (r.getVariableValue() instanceof String)
						mBean.setMsgRefId(Long.valueOf((String) r.getVariableValue()));
				}
			}
			else if (VariableType.DATETIME.equals(r.getVariableType())) {
				if (VariableName.SEND_DATE.equals(r.getVariableName())) {
					if (r.getVariableValue() == null) {
						mBean.setSendDate(new java.util.Date());
					}
					else {
						SimpleDateFormat fmt = new SimpleDateFormat(RenderVariable.DEFAULT_DATETIME_FORMAT);
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
			mBean.setCarrierCode(CarrierCode.getByValue(rsp.getMsgSourceVo().getCarrierCode()));
		}

		if (CodeType.YES_CODE.getValue().equals(src.getExcludingIdToken())) {
			mBean.setEmBedEmailId(Boolean.valueOf(false));
		}

		if (CodeType.YES_CODE.getValue().equals(src.getSaveMsgStream()))
			mBean.setSaveMsgStream(true);
		else
			mBean.setSaveMsgStream(false);
	}
	
	/*
	 * If MessageBean's SenderId field is not valued, and X-Sender_id header is
	 * found and valued, populate MessageBean's SenderId field with the value
	 * from X-Sender_id header. <br> 
	 */
	private void buildRenderedXHdrs(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderedXHdrs()...");
		// MsgSourceVo src = rsp.msgSourceVo;
		Map<String, RenderVariable> varbls = rsp.getVariableFinal();
		MessageBean mBean = rsp.getMessageBean();
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
					if (StringUtils.isBlank(mBean.getSenderId())) {
						mBean.setSenderId((String) r.getVariableValue());
					}
				}
				else if (XHeaderName.SUBSCRIBER_ID.getValue().equals(r.getVariableName())) {
					if (StringUtils.isBlank(mBean.getSubrId())) {
						mBean.setSubrId((String) r.getVariableValue());
					}
				}
			}
		}
		mBean.setHeaders(headers);
	}
	
	private void buildRenderVariables(RenderRequest req, RenderResponse rsp) {
		logger.info("in buildRenderVariables()...");
		
		MsgSourceVo msgSourceVo = msgSourceDao.getByPrimaryKey(req.getMsgSourceId());
		rsp.setMsgSourceVo(msgSourceVo);
		
		// retrieve variables
		Collection<GlobalVariableVo> globalVariables = globalVariableDao.getCurrent();
		Collection<SenderVariableVo> senderVariables = senderVariableDao.getCurrentBySenderId(
				req.getSenderId());
		Collection<TemplateVariableVo> templateVariables = templateVariableDao.getCurrentByTemplateId(
				msgSourceVo.getTemplateVariableId(), req.getSenderId());
		
		// convert variables into Map
		Map<String, RenderVariable> g_ht = globalVariablesToMap(globalVariables);
		Map<String, RenderVariable> c_ht = senderVariablesToMap(senderVariables);
		Map<String, RenderVariable> t_ht = templateVariablesToMap(templateVariables);
		
		// variables from req and MsgSource table
		Map<String, RenderVariable> s_ht = new HashMap<String, RenderVariable>();
		RenderVariable vreq = new RenderVariable(
				VariableName.SENDER_ID.getValue(),
				req.getSenderId(),
				null,
				VariableType.TEXT, 
				CodeType.YES_CODE.getValue(),
				Boolean.TRUE);
		s_ht.put(vreq.getVariableName(), vreq);
		
		vreq = new RenderVariable(
			EmailAddressType.FROM_ADDR.getValue(),
			emailAddrDao.getByAddrId(msgSourceVo.getFromAddrId()).getEmailAddr(),
			null,
			VariableType.ADDRESS, 
			CodeType.YES_CODE.getValue(),
			Boolean.TRUE);
		s_ht.put(vreq.getVariableName(), vreq);
		
		if (msgSourceVo.getReplyToAddrId()!=null) {
			vreq = new RenderVariable(
				EmailAddressType.REPLYTO_ADDR.getValue(),
				emailAddrDao.getByAddrId(msgSourceVo.getReplyToAddrId()).getEmailAddr(),
				null,
				VariableType.ADDRESS, 
				CodeType.YES_CODE.getValue(),
				Boolean.FALSE);
			s_ht.put(vreq.getVariableName(), vreq);
		}
		
		// get Runtime variables
		Map<String, RenderVariable> r_ht = req.getVariableOverrides();
		if (r_ht==null) {
			r_ht = new HashMap<String, RenderVariable>();
		}
		
		// error hash table
		Map<String, ErrorVariable> err_ht = new HashMap<String, ErrorVariable>();
		
		// merge variable tables
		mergeVariableMaps(s_ht, g_ht, err_ht);
		mergeVariableMaps(c_ht, g_ht, err_ht);
		mergeVariableMaps(t_ht, g_ht, err_ht);
		verifyVariableMap(g_ht, r_ht, err_ht);
		mergeVariableMaps(r_ht, g_ht, err_ht);
		
		rsp.getVariableFinal().putAll(g_ht);
		rsp.getVariableErrors().putAll(err_ht);
	}
	
	private void mergeVariableMaps(Map<String, RenderVariable> from,
			Map<String, RenderVariable> to, Map<String, ErrorVariable> error) {
		Set<String> keys = from.keySet();
		for (Iterator<String> it=keys.iterator(); it.hasNext();) {
			String name = it.next();
			if (to.get(name) != null) {
				RenderVariable req = (RenderVariable) to.get(name);
				if (CodeType.YES_CODE.getValue().equalsIgnoreCase(req.getAllowOverride())
						|| CodeType.MANDATORY_CODE.getValue().equalsIgnoreCase(req.getAllowOverride())) {
					to.put(name, from.get(name));
				}
				else {
					RenderVariable r = (RenderVariable) from.get(name);
					ErrorVariable err = new ErrorVariable(
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
	
	private void verifyVariableMap(Map<String, RenderVariable> gt,
			Map<String, RenderVariable> rt, Map<String, ErrorVariable> error) {
		Set<String> keys = gt.keySet();
		for (Iterator<String> it=keys.iterator(); it.hasNext();) {
			String name = it.next();
			RenderVariable req = (RenderVariable) gt.get(name);
			if (CodeType.MANDATORY_CODE.getValue().equalsIgnoreCase(req.getAllowOverride())) {
				if (!rt.containsKey(name)) {
					ErrorVariable err = new ErrorVariable(
							req.getVariableName(),
							req.getVariableValue(),
							"Variable Override is mandatory.");
					error.put(name, err);
				}
			}
		}
	}
	
	private Map<String, RenderVariable> globalVariablesToMap(Collection<GlobalVariableVo> c) {
		Map<String, RenderVariable> ht = new HashMap<String, RenderVariable>();
		for (GlobalVariableVo req : c) {
			RenderVariable r = new RenderVariable(
				req.getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.getByValue(req.getVariableType()), 
				req.getAllowOverride(), 
				CodeType.YES_CODE.equals(req.getRequired())
				);
			ht.put(req.getVariableName(), r);
		}
		return ht;
	}
	
	private Map<String, RenderVariable> senderVariablesToMap(Collection<SenderVariableVo> c) {
		Map<String, RenderVariable> ht = new HashMap<String, RenderVariable>();
		for (SenderVariableVo req : c) {
			RenderVariable r = new RenderVariable(
				req.getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.getByValue(req.getVariableType()), 
				req.getAllowOverride(), 
				CodeType.YES_CODE.equals(req.getRequired())
				);
			ht.put(req.getVariableName(), r);
		}
		return ht;
	}
	
	private Map<String, RenderVariable> templateVariablesToMap(Collection<TemplateVariableVo> c) {
		Map<String, RenderVariable> ht = new HashMap<String, RenderVariable>();
		for (TemplateVariableVo req : c) {
			RenderVariable r = new RenderVariable(
				req.getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				VariableType.getByValue(req.getVariableType()), 
				req.getAllowOverride(), 
				CodeType.YES_CODE.equals(req.getRequired())
				);
			ht.put(req.getVariableName(), r);
		}
		return ht;
	}

	public static Map<String, RenderVariable> renderVariablesToMap(Collection<RenderVariable> c) {
		Map<String, RenderVariable> ht = new HashMap<String, RenderVariable>();
		for (RenderVariable req : c) {
			RenderVariable r = new RenderVariable(
				req.getVariableName(),
				req.getVariableValue(), 
				req.getVariableFormat(), 
				req.getVariableType(), 
				CodeType.YES_CODE.getValue(), 
				Boolean.FALSE
				);
			ht.put(req.getVariableName(), r);
		}
		return ht;
	}
	
	
	/**
	 * render a template by templateId and senderId.
	 * 
	 * @param templateId -
	 *            template id
	 * @param senderId -
	 *            sender id
	 * @param variables -
	 *            variables
	 * @return rendered text
	 * @throws DataValidationException
	 * @throws ParseException
	 * @throws TemplateException 
	 */
	public String renderTemplateById(String templateId, String senderId,
			Map<String, RenderVariable> variables) throws DataValidationException,
			ParseException, TemplateException {
		if (StringUtils.isBlank(senderId)) {
			senderId = Constants.DEFAULT_SENDER_ID;
		}
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		TemplateDataVo tmpltVo = templateDao.getByBestMatch(senderId,templateId,startTime);
		if (tmpltVo == null) {
			throw new DataValidationException("TemplateData not found by: " + templateId + "/"
					+ senderId + "/" + startTime);
		}
		if (isDebugEnabled) {
			logger.debug("Template to render:" + LF + tmpltVo.getBodyTemplate());
		}
		
		Map<String, RenderVariable> map = new HashMap<String, RenderVariable>();

		List<TemplateVariableVo> tmpltList = templateVariableDao.getByTemplateId(templateId);
		for (Iterator<TemplateVariableVo> it = tmpltList.iterator(); it.hasNext();) {
			TemplateVariableVo vo = it.next();
			RenderVariable var = new RenderVariable(
					vo.getVariableName(),
					vo.getVariableValue(),
					vo.getVariableFormat(),
					VariableType.getByValue(vo.getVariableType()),
					vo.getAllowOverride(),
					CodeType.YES_CODE.equals(vo.getRequired()));
			if (map.containsKey(vo.getVariableName())) {
				RenderVariable v2 = map.get(vo.getVariableName());
				if (CodeType.YES_CODE.getValue().equalsIgnoreCase(v2.getAllowOverride())) {
					map.put(vo.getVariableName(), var);
				}
			}
			else {
				map.put(vo.getVariableName(), var);
			}
		}
		
		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (Iterator<String> it=keys.iterator(); it.hasNext(); ) {
				String key = it.next();
				if (map.containsKey(key)) {
					RenderVariable v2 = map.get(key);
					if (CodeType.YES_CODE.getValue().equalsIgnoreCase(v2.getAllowOverride())) {
						map.put(key, variables.get(key));
					}
				}
				else {
					map.put(key, variables.get(key));
				}
			}
		}
		
		String text = renderTemplateText(tmpltVo.getBodyTemplate(), senderId, map);
		return text;
	}

	/**
	 * render a template by template text and client id.
	 * 
	 * @param templateText -
	 *            template text
	 * @param senderId -
	 *            client id
	 * @param variables -
	 *            variables
	 * @return rendered text
	 * @throws DataValidationException
	 * @throws ParseException
	 * @throws TemplateException 
	 */
	public String renderTemplateText(String templateText, String senderId,
			Map<String, RenderVariable> variables) throws
			ParseException, TemplateException {
		if (templateText == null || templateText.trim().length() == 0) {
			return templateText;
		}
		if (StringUtils.isBlank(senderId)) {
			senderId = Constants.DEFAULT_SENDER_ID;
		}
		
		Map<String, RenderVariable> map = new HashMap<String, RenderVariable>();

		List<GlobalVariableVo> globalList = globalVariableDao.getCurrent();
		for (Iterator<GlobalVariableVo> it = globalList.iterator(); it.hasNext();) {
			GlobalVariableVo vo = it.next();
			RenderVariable var = new RenderVariable(
					vo.getVariableName(),
					vo.getVariableValue(),
					vo.getVariableFormat(),
					VariableType.getByValue(vo.getVariableType()),
					vo.getAllowOverride(),
					CodeType.YES_CODE.equals(vo.getRequired()));
			map.put(vo.getVariableName(), var);
		}

		List<SenderVariableVo> clientList = null;
		if (senderId != null) {
			clientList = senderVariableDao.getCurrentBySenderId(senderId);
			for (Iterator<SenderVariableVo> it = clientList.iterator(); it.hasNext();) {
				SenderVariableVo vo = it.next();
				RenderVariable var = new RenderVariable(
						vo.getVariableName(),
						vo.getVariableValue(),
						vo.getVariableFormat(),
						VariableType.getByValue(vo.getVariableType()),
						vo.getAllowOverride(),
						CodeType.YES_CODE.equals(vo.getRequired()));
				if (map.containsKey(vo.getVariableName())) {
					RenderVariable v2 = map.get(vo.getVariableName());
					if (CodeType.YES_CODE.getValue().equalsIgnoreCase(v2.getAllowOverride())) {
						map.put(vo.getVariableName(), var);
					}
				}
				else {
					map.put(vo.getVariableName(), var);
				}
			}
		}

		if (variables != null) {
			Set<String> keys = variables.keySet();
			for (Iterator<String> it=keys.iterator(); it.hasNext(); ) {
				String key = it.next();
				if (map.containsKey(key)) {
					RenderVariable v2 = map.get(key);
					if (CodeType.YES_CODE.getValue().equalsIgnoreCase(v2.getAllowOverride())) {
						map.put(key, variables.get(key));
					}
				}
				else {
					map.put(key, variables.get(key));
				}
			}
		}
		
		Map<String, ErrorVariable> errors = new HashMap<String, ErrorVariable>();
		String text = Renderer.getInstance().render(templateText, map, errors);
		return text;
	}
	
}

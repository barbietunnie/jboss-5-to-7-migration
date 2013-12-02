package com.es.bo.outbox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.es.bo.render.RenderRequest;
import com.es.bo.render.RenderResponse;
import com.es.bo.render.RenderVariable;
import com.es.bo.sender.RenderBo;
import com.es.core.util.SpringUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.inbox.MsgAddressDao;
import com.es.dao.inbox.MsgAttachmentDao;
import com.es.dao.inbox.MsgHeaderDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.dao.outbox.MsgRenderedDao;
import com.es.dao.outbox.RenderAttachmentDao;
import com.es.dao.outbox.RenderObjectDao;
import com.es.dao.outbox.RenderVariableDao;
import com.es.data.constant.CarrierCode;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.VariableType;
import com.es.data.preload.RuleNameEnum;
import com.es.exception.DataValidationException;
import com.es.exception.TemplateException;
import com.es.msgbean.MessageBean;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.outbox.MsgRenderedVo;
import com.es.vo.outbox.RenderAttachmentVo;
import com.es.vo.outbox.RenderObjectVo;
import com.es.vo.outbox.RenderVariableVo;

@Component("msgOutboxBo")
public class MsgOutboxBo {
	static final Logger logger = Logger.getLogger(MsgOutboxBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	static final String LF = System.getProperty("line.separator","\n");
	
	@Autowired
	private RenderBo renderBo;
	@Autowired
	private MsgRenderedDao msgRenderedDao;
	@Autowired
	private RenderAttachmentDao renderAttachmentDao;
	@Autowired
	private RenderVariableDao renderVariableDao;
	@Autowired
	private RenderObjectDao renderObjectDao;
	@Autowired
	private MsgInboxDao msgInboxDao;
	@Autowired
	private MsgAttachmentDao attachmentDao;
	@Autowired
	private MsgHeaderDao msgHeaderDao;
	@Autowired
	private MsgAddressDao msgAddressDao;
	@Autowired
	private EmailAddressDao emailAddressDao;
	
	public MsgOutboxBo() {
	}
	
	public static void main(String[] args) {
		MsgOutboxBo msgOutboxBo = SpringUtil.getAppContext().getBean(MsgOutboxBo.class);
		MsgRenderedDao renderedDao = SpringUtil.getAppContext().getBean(MsgRenderedDao.class);
		long renderId = 2L;
		try {
			MsgRenderedVo renderedVo = renderedDao.getByPrimaryKey(renderId);
			if (renderedVo == null) {
				renderId = renderedDao.getLastRecord().getRenderId();
			}
			MessageBean bean = msgOutboxBo.getMessageByPK(renderId);
			System.out.println("MessageBean retrieved:\n" + bean);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * save Message Source Id's and RenderVariables into MsgRendered tables
	 * 
	 * @param rsp -
	 *            RenderResponse
	 * @return renderId of the record inserted
	 * @throws DataValidationException 
	 * @throws IOException 
	 */
	public long saveRenderData(RenderResponse rsp) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering saveRenderData()...");
		}
		if (rsp == null) {
			throw new DataValidationException("Input object is null");
		}
		if (rsp.getMessageBean() == null) {
			throw new DataValidationException("Input MessageBean object is null");
		}
		if (rsp.getMsgSourceVo() == null) {
			throw new DataValidationException("Input MsgSourceVo object is null");
		}
		
		MessageBean msgBean = rsp.getMessageBean();
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		
		//
		// save MsgRendered record
		//
		MsgRenderedVo msgVo = new MsgRenderedVo();
		
		msgVo.setMsgSourceId(rsp.getMsgSourceVo().getMsgSourceId());
		msgVo.setTemplateId(rsp.getMsgSourceVo().getTemplateDataId());
		msgVo.setStartTime(rsp.getStartTime());
		msgVo.setSenderId(msgBean.getSenderId());
		msgVo.setSubrId(msgBean.getSubrId());
		msgVo.setPurgeAfter(rsp.getMsgSourceVo().getPurgeAfter());
		
		msgBean.setPurgeAfter(rsp.getMsgSourceVo().getPurgeAfter());
		msgBean.setRuleName(RuleNameEnum.SEND_MAIL.getValue());
		
		if (msgBean.getCarrierCode() == null) {
			String cc = rsp.getMsgSourceVo().getCarrierCode();
			msgBean.setCarrierCode(CarrierCode.getByValue(cc));
		}
		if (msgBean.getFrom() == null && rsp.getMsgSourceVo().getFromAddrId() != null) {
			EmailAddressVo addrVo = getEmailAddrDao()
					.getByAddrId(rsp.getMsgSourceVo().getFromAddrId());
			if (addrVo !=null) {
				try {
					Address[] from = InternetAddress.parse(addrVo.getEmailAddr());
					msgBean.setFrom(from);
				}
				catch (AddressException e) {
					logger.error("saveRenderData() - AddressException caught for address: "
							+ addrVo.getEmailAddr());
				}
			}
		}
		if (msgBean.getReplyto() == null && rsp.getMsgSourceVo().getReplyToAddrId() != null) {
			EmailAddressVo addrVo = getEmailAddrDao()
					.getByAddrId(rsp.getMsgSourceVo().getReplyToAddrId());
			if (addrVo != null) {
				try {
					Address[] replyto = InternetAddress.parse(addrVo.getEmailAddr());
					msgBean.setReplyto(replyto);
				}
				catch (AddressException e) {
					logger.error("saveRenderData() - AddressException caught for address: "
							+ addrVo.getEmailAddr());
				}
			}
			
		}
		
		if (CodeType.YES_CODE.getValue().equalsIgnoreCase(rsp.getMsgSourceVo().getExcludingIdToken())) {
			// operation moved to MailSender after message is written to database
			msgBean.setEmBedEmailId(Boolean.valueOf(false));
		}
		
		if (CodeType.NO_CODE.getValue().equalsIgnoreCase(rsp.getMsgSourceVo().getSaveMsgStream())) {
			// operation moved to MailSender after mail is sent
			msgBean.setSaveMsgStream(false);
		}
		
		msgVo.setUpdtTime(updtTime);
		msgVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		msgRenderedDao.insert(msgVo);
		msgBean.setRenderId(Long.valueOf(msgVo.getRenderId()));
		
		// save Render Attachments & Render Variables
		Map<?, ?> varbls = rsp.getVariableFinal();
		if (varbls!=null && !varbls.isEmpty()) {
			Collection<?> c = varbls.values();
			int i=0;
			for (Iterator<?> it=c.iterator(); it.hasNext(); ) {
				RenderVariable req = (RenderVariable)it.next();
				if (VariableType.LOB.equals(req.getVariableType())) {
					// save to RenderAttachment
					RenderAttachmentVo renderAttachmentVo = new RenderAttachmentVo();
					renderAttachmentVo.setRenderId(msgVo.getRenderId());
					renderAttachmentVo.setAttchmntName(req.getVariableName());
					renderAttachmentVo.setAttchmntSeq(i++);
					if (req.getVariableFormat() != null && req.getVariableFormat().indexOf(";") > 0
							&& req.getVariableFormat().indexOf("name=") > 0) {
						renderAttachmentVo.setAttchmntType(req.getVariableFormat());
					}
					else {
						renderAttachmentVo.setAttchmntType(req.getVariableFormat() + "; name=\""
								+ req.getVariableName() + "\"");
					}
					renderAttachmentVo.setAttchmntDisp(Part.ATTACHMENT);
					Object value = req.getVariableValue();
					if (req.getVariableValue() instanceof String) {
						renderAttachmentVo.setAttchmntValue(((String)value).getBytes());
					}
					else if (value instanceof byte[]) {
						renderAttachmentVo.setAttchmntValue((byte[])value);
					}
					else {
						throw new DataValidationException("Invalid Attachment Type: "
								+ value.getClass().getName());
					}
					// create a record
					renderAttachmentDao.insert(renderAttachmentVo);
				}
				else if (VariableType.COLLECTION.equals(req.getVariableType())) {
					// save to RenderObject
					RenderObjectVo renderObjectVo = new RenderObjectVo();
					renderObjectVo.setRenderId(msgVo.getRenderId());
					renderObjectVo.setVariableName(req.getVariableName());
					renderObjectVo.setVariableFormat(req.getVariableFormat());
					renderObjectVo.setVariableType(req.getVariableType().getValue());
				    try {
					    ByteArrayOutputStream baos = new ByteArrayOutputStream();
					    ObjectOutputStream oos = new ObjectOutputStream(baos);
					    oos.writeObject(req.getVariableValue());
					    oos.flush();
					    byte[] bytes = baos.toByteArray();
					    oos.close();
					    renderObjectVo.setVariableValue(bytes);
				    }
				    catch (IOException e) {
				    	logger.error("saveRenderData() - IOException caught", e);
				    	throw new DataValidationException(e.toString());
				    }
					// create a record
					renderObjectDao.insert(renderObjectVo);
				}
				else {
					// save to RenderVariable
					RenderVariableVo renderVariableVo = new RenderVariableVo();
					renderVariableVo.setRenderId(msgVo.getRenderId());
					renderVariableVo.setVariableName(req.getVariableName());
					renderVariableVo.setVariableFormat(req.getVariableFormat());
					renderVariableVo.setVariableType(req.getVariableType().getValue());
					if (VariableType.TEXT.equals(req.getVariableType())
							|| VariableType.X_HEADER.equals(req.getVariableType())) {
						renderVariableVo.setVariableValue((String)req.getVariableValue());
					}
					else if (VariableType.ADDRESS.equals(req.getVariableType())) {
						if (req.getVariableValue() instanceof Address) {
							renderVariableVo.setVariableValue(((Address)req.getVariableValue()).toString());
						}
						else if (req.getVariableValue() instanceof String) {
							renderVariableVo.setVariableValue((String)req.getVariableValue());
						}
					}
					else if (VariableType.NUMERIC.equals(req.getVariableType())) {
						if (req.getVariableValue() instanceof Long) {
							renderVariableVo.setVariableValue(((Long)req.getVariableValue()).toString());
						}
						else if (req.getVariableValue() instanceof String) {
							renderVariableVo.setVariableValue((String)req.getVariableValue());
						}
					}
					else if (VariableType.DATETIME.equals(req.getVariableType())) {
						SimpleDateFormat fmt = new SimpleDateFormat(Constants.DEFAULT_DATETIME_FORMAT);
						if (req.getVariableFormat()!=null) {
							fmt = new SimpleDateFormat(req.getVariableFormat());
						}
						if (req.getVariableValue()!=null) {
							if (req.getVariableValue() instanceof String) {
								try {
									java.util.Date date = fmt.parse((String)req.getVariableValue());
									renderVariableVo.setVariableValue(fmt.format(date));
								}
								catch (ParseException e) {
									logger.error("saveRenderData() - Invalid Date Value: "
											+ req.getVariableValue(), e);
									renderVariableVo.setVariableValue((String)req.getVariableValue());
								}
							}
							else if (req.getVariableValue() instanceof java.util.Date) {
								renderVariableVo.setVariableValue(fmt
										.format((java.util.Date) req.getVariableValue()));
							}
						}
					}
					else {
						logger.warn("saveRenderData() - Unrecognized render name/type: "
									+ req.getVariableName() + "/" + req.getVariableType()
									+ ", ignored");
					}
					// create a record
					renderVariableDao.insert(renderVariableVo);
				}
			}
		}
		
		return msgVo.getRenderId();
	}
	
	/**
	 * retrieve a MessageBean by primary key from MsgRendered tables
	 * 
	 * @param renderId -
	 *            render id
	 * @return a MessageBean object
	 * @throws AddressException
	 * @throws ParseException
	 * @throws TemplateException 
	 */
	public MessageBean getMessageByPK(long renderId) throws AddressException,
			ParseException, TemplateException {
		RenderRequest renderRequest = getRenderRequestByPK(renderId);
		if (renderRequest == null) { // should never happen
			throw new DataValidationException("RenderRequest is null for RenderId: " + renderId);
		}
		RenderResponse rsp = renderBo.getRenderedEmail(renderRequest);
		rsp.getMessageBean().setRenderId(Long.valueOf(renderId));
		return rsp.getMessageBean();
	}
	
	/**
	 * retrieve a RenderRequest by primary key from MsgRendered tables
	 * 
	 * @param renderId -
	 *            render id
	 * @return a RenderRequest object
	 * @throws DataValidationException 
	 */
	public RenderRequest getRenderRequestByPK(long renderId) throws DataValidationException {
		// get the messageOB
		MsgRenderedVo msgRenderedVo = msgRenderedDao.getByPrimaryKey(renderId);
		if (msgRenderedVo == null) {
			throw new DataValidationException("MsgRendered record not found for renderId: "
					+ renderId);
		}
		String msgSourceId = msgRenderedVo.getMsgSourceId();
		//MsgSourceVo src = msgSourceDao.getByPrimaryKey(msgSourceId);
		if (msgSourceId == null) {
			throw new DataValidationException("MsgSourceId is null for RenderId: " + renderId);
		}
		
		// populate variableFinal
		Map<String, RenderVariable> varblFinal = new HashMap<String, RenderVariable>();
		List<RenderVariableVo> renderVariables = renderVariableDao.getByRenderId(renderId);
		if (renderVariables != null && !renderVariables.isEmpty()) {
			Iterator<RenderVariableVo> it = renderVariables.iterator();
			while (it.hasNext()) {
				RenderVariableVo varVo = it.next();
				RenderVariable r = new RenderVariable(
						varVo.getVariableName(),
						varVo.getVariableValue(),
						varVo.getVariableFormat(),
						VariableType.getByValue(varVo.getVariableType()), 
						CodeType.YES_CODE.getValue(), // allow override
						Boolean.FALSE // required
						);
				
				varblFinal.put(r.getVariableName(), r);
			}
		}

		// populate renderObjects into variableFinal
		List<RenderObjectVo> renderObjects = renderObjectDao.getByRenderId(renderId);
		if (renderObjects != null && !renderObjects.isEmpty()) {
			Iterator<RenderObjectVo> it = renderObjects.iterator();
			while (it.hasNext()) {
				RenderObjectVo varVo = it.next();
				List<?> value = null;
				try {
					byte[] bytes = varVo.getVariableValue();
					ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
					ObjectInputStream ois = new ObjectInputStream(bais);
					value = (List<?>) ois.readObject();
				}
				catch (Exception e) {
					logger.error("Exception caught", e);
					throw new DataValidationException(e.toString());
				}
				RenderVariable r = new RenderVariable(
						varVo.getVariableName(),
						value,
						varVo.getVariableFormat(),
						VariableType.getByValue(varVo.getVariableType()),
						CodeType.YES_CODE.getValue(), // allow override
						Boolean.FALSE // required
						);
				
				varblFinal.put(r.getVariableName(), r);
			}
		}

		// populate renderAttachments into variableFinal
		List<RenderAttachmentVo> renderAttachments = renderAttachmentDao.getByRenderId(renderId);
		if (renderAttachments != null && !renderAttachments.isEmpty()) {
			for (Iterator<RenderAttachmentVo> it = renderAttachments.iterator(); it.hasNext();) {
				RenderAttachmentVo attVo = it.next();
				Object value = null;
				if (attVo.getAttchmntType().indexOf("text")>=0) {
					value = new String(attVo.getAttchmntValue());
				}
				else {
					value = attVo.getAttchmntValue();
				}
				RenderVariable r = new RenderVariable(
					attVo.getAttchmntName(), 
					value, 
					attVo.getAttchmntType(), // content type as format
					VariableType.LOB, 
					CodeType.YES_CODE.getValue(), 
					Boolean.FALSE 
					);
				varblFinal.put(r.getVariableName(), r);
			}
		}
		
		RenderRequest renderRequest = new RenderRequest(
			msgSourceId,
			msgRenderedVo.getSenderId(),
			msgRenderedVo.getStartTime(),
			varblFinal
			);
		
		return renderRequest;
	}

	public MsgAttachmentDao getAttachmentsDao() {
		return attachmentDao;
	}

	public RenderVariableDao getRenderVariableDao() {
		return renderVariableDao;
	}

	public EmailAddressDao getEmailAddrDao() {
		return emailAddressDao;
	}

	public RenderBo getRenderBo() {
		return renderBo;
	}

	public MsgRenderedDao getMsgRenderedDao() {
		return msgRenderedDao;
	}

	public RenderAttachmentDao getRenderAttachmentDao() {
		return renderAttachmentDao;
	}

	public MsgHeaderDao getMsgHeadersDao() {
		return msgHeaderDao;
	}

	public MsgAddressDao getMsgAddrsDao() {
		return msgAddressDao;
	}

	public MsgInboxDao getMsgInboxDao() {
		return msgInboxDao;
	}

	public RenderObjectDao getRenderObjectDao() {
		return renderObjectDao;
	}
}

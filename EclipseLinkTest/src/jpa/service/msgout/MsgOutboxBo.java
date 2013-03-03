package jpa.service.msgout;

import java.io.IOException;
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
import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.VariableType;
import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.model.ClientData;
import jpa.model.CustomerData;
import jpa.model.EmailAddress;
import jpa.model.message.MessageRendered;
import jpa.model.message.MessageSource;
import jpa.model.message.RenderAttachment;
import jpa.model.message.RenderAttachmentPK;
import jpa.model.message.RenderVariable;
import jpa.model.message.RenderVariablePK;
import jpa.service.ClientDataService;
import jpa.service.CustomerDataService;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageAddressService;
import jpa.service.message.MessageAttachmentService;
import jpa.service.message.MessageHeaderService;
import jpa.service.message.MessageInboxService;
import jpa.service.message.MessageRenderedService;
import jpa.service.message.MessageSourceService;
import jpa.service.message.RenderAttachmentService;
import jpa.service.message.RenderVariableService;
import jpa.util.SpringUtil;
import jpa.variable.RenderVariableVo;
import jpa.variable.TemplateException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("msgOutboxBo")
@Transactional(propagation=Propagation.REQUIRED)
public class MsgOutboxBo {
	static final Logger logger = Logger.getLogger(MsgOutboxBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	static final String LF = System.getProperty("line.separator","\n");
	
	@Autowired
	private RenderBo renderBo;
	@Autowired
	private MessageRenderedService msgRenderedDao;
	@Autowired
	private RenderAttachmentService renderAttachmentDao;
	@Autowired
	private RenderVariableService renderVariableDao;
	@Autowired
	private MessageInboxService msgInboxDao;
	@Autowired
	private MessageAttachmentService attachmentsDao;
	@Autowired
	private MessageHeaderService msgHeadersDao;
	@Autowired
	private MessageAddressService msgAddrsDao;
	@Autowired
	private EmailAddressService emailAddrDao;
	@Autowired
	private ClientDataService clientService;
	@Autowired
	private CustomerDataService custService;
	@Autowired
	private MessageSourceService msgSourceDao;
	
	public MsgOutboxBo() {
	}
	
	public static void main(String[] args) {
		MsgOutboxBo msgOutboxBo = (MsgOutboxBo) SpringUtil.getAppContext().getBean("msgOutboxBo");
		int renderId = 2;
		try {
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
	public int saveRenderData(RenderResponse rsp) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering saveRenderData()...");
		if (rsp == null) {
			throw new DataValidationException("Input object is null");
		}
		if (rsp.getMessageBean() == null) {
			throw new DataValidationException("Input MessageBean object is null");
		}
		if (rsp.getMessageSource() == null) {
			throw new DataValidationException("Input MsgSourceVo object is null");
		}
		
		MessageBean msgBean = rsp.getMessageBean();
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		
		//
		// save MsgRendered record
		//
		MessageRendered msgVo = new MessageRendered();
		
		msgVo.setMessageSourceRowId(rsp.getMessageSource().getRowId());
		msgVo.setMessageTemplateRowId(rsp.getMessageSource().getTemplateData().getRowId());
		msgVo.setStartTime(rsp.getStartTime());
		if (StringUtils.isNotBlank(msgBean.getClientId())) {
			try {
				ClientData client = clientService.getByClientId(msgBean.getClientId());
				msgVo.setClientDataRowId(client.getRowId());
			}
			catch (NoResultException e) {}
		}
		if (StringUtils.isNotBlank(msgBean.getCustId())) {
			try {
				CustomerData cust = custService.getByCustomerId(msgBean.getCustId());
				msgVo.setCustomerDataRowId(cust.getRowId());
			}
			catch (NoResultException e) {}
		}
		msgVo.setPurgeAfter(rsp.getMessageSource().getPurgeAfter());
		
		msgBean.setPurgeAfter(rsp.getMessageSource().getPurgeAfter());
		msgBean.setRuleName(RuleNameEnum.SEND_MAIL.getValue());
		
		if (msgBean.getCarrierCode() == null) {
			msgBean.setCarrierCode(CarrierCode.valueOf(rsp.getMessageSource().getCarrierCode()));
		}
		if (msgBean.getFrom() == null && rsp.getMessageSource().getFromAddress() != null) {
			EmailAddress addrVo = rsp.getMessageSource().getFromAddress();
			try {
				Address[] from = InternetAddress.parse(addrVo.getAddress());
				msgBean.setFrom(from);
			}
			catch (AddressException e) {
				logger.error("saveRenderData() - AddressException caught for address: "
						+ addrVo.getAddress());
			}
		}
		if (msgBean.getReplyto() == null && rsp.getMessageSource().getReplyToAddress() != null) {
			EmailAddress addrVo = rsp.getMessageSource().getReplyToAddress();
			try {
				Address[] replyto = InternetAddress.parse(addrVo.getAddress());
				msgBean.setReplyto(replyto);
			}
			catch (AddressException e) {
				logger.error("saveRenderData() - AddressException caught for address: "
						+ addrVo.getAddress());
			}
		}
		
		if (rsp.getMessageSource().isExcludingIdToken()) {
			// operation moved to MailSender after message is written to database
			msgBean.setEmBedEmailId(Boolean.valueOf(false));
		}
		
		if (!rsp.getMessageSource().isSaveMsgStream()) {
			// operation moved to MailSender after mail is sent
			msgBean.setSaveMsgStream(false);
		}
		
		msgVo.setUpdtTime(updtTime);
		msgVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		msgRenderedDao.insert(msgVo);
		msgBean.setRenderId(Integer.valueOf(msgVo.getRowId()));
		
		// save Render Attachments & Render Variables
		Map<String, RenderVariableVo> varbls = rsp.getVariableFinal();
		if (varbls!=null && !varbls.isEmpty()) {
			Collection<RenderVariableVo> c = varbls.values();
			int i=0;
			for (Iterator<RenderVariableVo> it=c.iterator(); it.hasNext(); ) {
				RenderVariableVo req = it.next();
				if (VariableType.LOB.equals(req.getVariableType())) {
					// save to RenderAttachment
					RenderAttachment renderAttc = new RenderAttachment();
					RenderAttachmentPK pk = new RenderAttachmentPK(msgVo, i++);
					renderAttc.setRenderAttachmentPK(pk);
					renderAttc.setAttachmentName(req.getVariableName());
					if (req.getVariableFormat() != null && req.getVariableFormat().indexOf(";") > 0
							&& req.getVariableFormat().indexOf("name=") > 0) {
						renderAttc.setAttachmentType(req.getVariableFormat());
					}
					else {
						renderAttc.setAttachmentType(req.getVariableFormat() + "; name=\""
								+ req.getVariableName() + "\"");
					}
					renderAttc.setAttachmentDisp(Part.ATTACHMENT);
					Object value = req.getVariableValue();
					if (req.getVariableValue() instanceof String) {
						renderAttc.setAttachmentValue(((String)value).getBytes());
					}
					else if (value instanceof byte[]) {
						renderAttc.setAttachmentValue((byte[])value);
					}
					else {
						throw new DataValidationException("Invalid Attachment Type: "
								+ value.getClass().getName());
					}
					// create a record
					renderAttachmentDao.insert(renderAttc);
				}
//				else if (VariableType.COLLECTION.equals(req.getVariableType())) {
//					// save to RenderObject
//					RenderObjectVo renderObjectVo = new RenderObjectVo();
//					renderObjectVo.setRenderId(msgVo.getRenderId());
//					renderObjectVo.setVariableName(req.getVariableName());
//					renderObjectVo.setVariableFormat(req.getVariableFormat());
//					renderObjectVo.setVariableType(req.getVariableType());
//				    try {
//					    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//					    ObjectOutputStream oos = new ObjectOutputStream(baos);
//					    oos.writeObject(req.getVariableValue());
//					    oos.flush();
//					    byte[] bytes = baos.toByteArray();
//					    oos.close();
//					    renderObjectVo.setVariableValue(bytes);
//				    }
//				    catch (IOException e) {
//				    	logger.error("saveRenderData() - IOException caught", e);
//				    	throw new DataValidationException(e.toString());
//				    }
//					// create a record
//					renderObjectDao.insert(renderObjectVo);
//				}
				else {
					// save to RenderVariable
					RenderVariable renderVar = new RenderVariable();
					RenderVariablePK pk = new RenderVariablePK(msgVo, req.getVariableName());
					renderVar.setRenderVariablePK(pk);
					renderVar.setVariableFormat(req.getVariableFormat());
					renderVar.setVariableType(req.getVariableType().getValue());
					if (VariableType.TEXT.equals(req.getVariableType())
							|| VariableType.X_HEADER.equals(req.getVariableType())) {
						renderVar.setVariableValue((String)req.getVariableValue());
					}
					else if (VariableType.ADDRESS.equals(req.getVariableType())) {
						if (req.getVariableValue() instanceof Address) {
							renderVar.setVariableValue(((Address)req.getVariableValue()).toString());
						}
						else if (req.getVariableValue() instanceof String) {
							renderVar.setVariableValue((String)req.getVariableValue());
						}
					}
					else if (VariableType.NUMERIC.equals(req.getVariableType())) {
						if (req.getVariableValue() instanceof Long) {
							renderVar.setVariableValue(((Long)req.getVariableValue()).toString());
						}
						else if (req.getVariableValue() instanceof String) {
							renderVar.setVariableValue((String)req.getVariableValue());
						}
					}
					else if (VariableType.DATETIME.equals(req.getVariableType())) {
						SimpleDateFormat fmt = new SimpleDateFormat(RenderVariableVo.DEFAULT_DATETIME_FORMAT);
						if (req.getVariableFormat()!=null) {
							fmt = new SimpleDateFormat(req.getVariableFormat());
						}
						if (req.getVariableValue()!=null) {
							if (req.getVariableValue() instanceof String) {
								try {
									java.util.Date date = fmt.parse((String)req.getVariableValue());
									renderVar.setVariableValue(fmt.format(date));
								}
								catch (ParseException e) {
									logger.error("saveRenderData() - Invalid Date Value: "
											+ req.getVariableValue(), e);
									renderVar.setVariableValue((String)req.getVariableValue());
								}
							}
							else if (req.getVariableValue() instanceof java.util.Date) {
								renderVar.setVariableValue(fmt
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
					renderVariableDao.insert(renderVar);
				}
			}
		}
		
		return msgVo.getRowId();
	}
	
	/**
	 * retrieve a MessageBean by primary key from MsgRendered tables
	 * 
	 * @param renderId -
	 *            render id
	 * @return a MessageBean object
	 * @throws AddressException
	 * @throws DataValidationException
	 * @throws ParseException
	 * @throws TemplateException 
	 */
	public MessageBean getMessageByPK(int renderId) throws AddressException,
			ParseException, TemplateException {
		RenderRequest renderRequest = getRenderRequestByPK(renderId);
		if (renderRequest == null) { // should never happen
			throw new DataValidationException("RenderRequest is null for RenderId: " + renderId);
		}
		RenderResponse rsp = renderBo.getRenderedEmail(renderRequest);
		rsp.getMessageBean().setRenderId(Integer.valueOf(renderId));
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
	public RenderRequest getRenderRequestByPK(int renderId) throws DataValidationException {
		// get the messageOB
		MessageRendered msgRenderedVo = null;
		try {
			msgRenderedVo = msgRenderedDao.getByPrimaryKey(renderId);
		}
		catch (NoResultException e){
			throw new DataValidationException("MsgRendered record not found for renderId: "
					+ renderId);
		}
		String msgSourceId = null;
		try {
			int rowId = msgRenderedVo.getMessageSourceRowId();
			MessageSource src = msgSourceDao.getByRowId(rowId);
			msgSourceId = src.getMsgSourceId();
		}
		catch (NoResultException e) {
			throw new DataValidationException("MsgSourceId is null for RenderId: " + renderId);
		}
		String clientId = null;
		try {
			int rowId = msgRenderedVo.getClientDataRowId();
			ClientData client = clientService.getByRowId(rowId);
			clientId = client.getClientId();
		}
		catch (NoResultException e) {}
		// populate variableFinal
		Map<String, RenderVariableVo> varblFinal = new HashMap<String, RenderVariableVo>();
		List<RenderVariable> renderVariables = renderVariableDao.getByRenderId(renderId);
		if (!renderVariables.isEmpty()) {
			Iterator<RenderVariable> it = renderVariables.iterator();
			while (it.hasNext()) {
				RenderVariable varVo = it.next();
				RenderVariableVo r = new RenderVariableVo(
						varVo.getRenderVariablePK().getVariableName(),
						varVo.getVariableValue(),
						varVo.getVariableFormat(),
						VariableType.valueOf(varVo.getVariableType()), 
						CodeType.YES_CODE.getValue(), // allow override
						Boolean.FALSE // required
						);
				
				varblFinal.put(r.getVariableName(), r);
			}
		}

		// populate renderObjects into variableFinal
//		List<RenderObject> renderObjects = renderObjectDao.getByRenderId(renderId);
//		if (!renderObjects.isEmpty()) {
//			Iterator<RenderObject> it = renderObjects.iterator();
//			while (it.hasNext()) {
//				RenderObject varVo = it.next();
//				List<?> value = null;
//				try {
//					byte[] bytes = varVo.getVariableValue();
//					ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//					ObjectInputStream ois = new ObjectInputStream(bais);
//					value = (List<?>) ois.readObject();
//				}
//				catch (Exception e) {
//					logger.error("Exception caught", e);
//					throw new DataValidationException(e.toString());
//				}
//				RenderVariableVo r = new RenderVariableVo(
//						varVo.getRenderVariablePK().getVariableName(),
//						value,
//						varVo.getVariableFormat(),
//						VariableType.valueOf(varVo.getVariableType()), 
//						CodeType.YES_CODE.getValue(), // allow override
//						Boolean.FALSE // required
//						);
//				
//				varblFinal.put(r.getVariableName(), r);
//			}
//		}

		// populate renderAttachments into variableFinal
		List<RenderAttachment> renderAttachments = renderAttachmentDao.getByRenderId(renderId);
		if (!renderAttachments.isEmpty()) {
			for (Iterator<RenderAttachment> it = renderAttachments.iterator(); it.hasNext();) {
				RenderAttachment attVo = it.next();
				Object value = null;
				if (attVo.getAttachmentType().indexOf("text")>=0) {
					value = new String(attVo.getAttachmentValue());
				}
				else {
					value = attVo.getAttachmentValue();
				}
				RenderVariableVo r = new RenderVariableVo(
					attVo.getAttachmentName(), 
					value, 
					attVo.getAttachmentType(), // content type as format
					VariableType.LOB, 
					CodeType.YES_CODE.getValue(), 
					Boolean.FALSE
					);
				varblFinal.put(r.getVariableName(), r);
			}
		}
		
		RenderRequest renderRequest = new RenderRequest(
			msgSourceId,
			clientId,
			msgRenderedVo.getStartTime(),
			varblFinal
			);
		
		return renderRequest;
	}
}

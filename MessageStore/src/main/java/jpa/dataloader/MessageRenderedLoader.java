package jpa.dataloader;

import java.sql.Timestamp;
import java.util.List;

import javax.mail.Part;

import jpa.constant.Constants;
import jpa.constant.VariableName;
import jpa.constant.VariableType;
import jpa.model.SenderData;
import jpa.model.message.MessageRendered;
import jpa.model.message.MessageSource;
import jpa.model.message.RenderAttachment;
import jpa.model.message.RenderAttachmentPK;
import jpa.model.message.RenderVariable;
import jpa.model.message.RenderVariablePK;
import jpa.service.common.SenderDataService;
import jpa.service.message.MessageRenderedService;
import jpa.service.message.MessageSourceService;
import jpa.service.message.RenderAttachmentService;
import jpa.service.message.RenderVariableService;
import jpa.util.SpringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

public class MessageRenderedLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(MessageRenderedLoader.class);
	private MessageRenderedService service;
	private SenderDataService senderService;
	private MessageSourceService sourceService;
	private RenderVariableService variableService;
	private RenderAttachmentService attachmentService;

	public static void main(String[] args) {
		MessageRenderedLoader loader = new MessageRenderedLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = SpringUtil.getAppContext().getBean(MessageRenderedService.class);
		senderService = SpringUtil.getAppContext().getBean(SenderDataService.class);
		sourceService = SpringUtil.getAppContext().getBean(MessageSourceService.class);
		variableService = SpringUtil.getAppContext().getBean(RenderVariableService.class);
		attachmentService = SpringUtil.getAppContext().getBean(RenderAttachmentService.class);
		startTransaction();
		try {
			loadMessageRendered();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadMessageRendered() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		List<MessageSource> srcs = sourceService.getAll();
		MessageSource src1 = srcs.get(0);

		MessageRendered in1 = new MessageRendered();
		in1.setMessageSourceRowId(src1.getRowId());
		in1.setMessageTemplateRowId(src1.getTemplateData().getRowId());
		in1.setStartTime(updtTime);
		in1.setSenderDataRowId(sender.getRowId());
		in1.setSubscriberDataRowId(null);
		in1.setPurgeAfter(null);
		service.insert(in1);
		
		RenderVariable rv1 = new RenderVariable();
		RenderVariablePK rvpk1 = new RenderVariablePK(in1,VariableName.SENDER_ID.getValue());
		rv1.setRenderVariablePK(rvpk1);
		rv1.setVariableType(VariableType.TEXT.getValue());
		rv1.setVariableValue(Constants.DEFAULT_SENDER_ID);
		variableService.insert(rv1);
		
		RenderVariable rv2 = new RenderVariable();
		try {
			BeanUtils.copyProperties(rv2, rv1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		RenderVariablePK rvpk2 = new RenderVariablePK(in1,"CurrentDate");
		rv2.setRenderVariablePK(rvpk2);
		rv2.setVariableType(VariableType.DATETIME.getValue());
		rv2.setVariableFormat("yyyy-MM-dd");
		rv2.setVariableValue(null);
		variableService.insert(rv2);

		RenderAttachment ra1 = new RenderAttachment();
		RenderAttachmentPK rapk1 = new RenderAttachmentPK(in1,1);
		ra1.setRenderAttachmentPK(rapk1);
		ra1.setAttachmentType("text/plain; charset=\"iso-8859-1\"; name=\"attachment1.txt\"");
		ra1.setAttachmentName("attachment1.txt");
		ra1.setAttachmentDisp(Part.INLINE);
		ra1.setAttachmentValue(loadFromSamples("jndi.bin"));
		attachmentService.insert(ra1);

		RenderAttachment ra2 = new RenderAttachment();
		RenderAttachmentPK rapk2 = new RenderAttachmentPK(in1,2);
		ra2.setRenderAttachmentPK(rapk2);
		ra2.setAttachmentType("image/gif; name=one.gif");
		ra2.setAttachmentName("one.gif");
		ra2.setAttachmentDisp(Part.ATTACHMENT);
		ra2.setAttachmentValue(loadFromSamples("one.gif"));
		attachmentService.insert(ra2);

		logger.info("EntityManager persisted the record.");
	}
	
}


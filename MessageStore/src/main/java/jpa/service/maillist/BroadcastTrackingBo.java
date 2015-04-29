package jpa.service.maillist;

import javax.persistence.NoResultException;

import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;
import jpa.model.EmailAddress;
import jpa.model.Subscription;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SubscriptionService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("broadcastTrackingBo")
@Transactional(propagation=Propagation.REQUIRED)
public class BroadcastTrackingBo implements java.io.Serializable {
	private static final long serialVersionUID = 3992085332692245886L;
	
	static final Logger logger = Logger.getLogger(BroadcastTrackingBo.class);

	@Autowired
	private BroadcastMessageService bcstMsgService;
	@Autowired
	private BroadcastTrackingService trackingService;
	@Autowired
	private SubscriptionService subService;
	@Autowired
	private EmailAddressService emailService;
	
	public Subscription removeFromList(int bcstMsgRowId, int emailAddrRowId) {
		try {
			EmailAddress ea = emailService.getByRowId(emailAddrRowId);
			try {
				BroadcastMessage bm = bcstMsgService.getByRowId(bcstMsgRowId);
				bcstMsgService.updateUnsubscribeCount(bcstMsgRowId);
				Subscription sub = subService.unsubscribe(ea.getAddress(), bm.getMailingList().getListId());
				return sub;
			}
			catch (NoResultException e2) {
				logger.info("BroadcastMessage record not found by RowId (" + bcstMsgRowId + "), ignored.");
			}
		}
		catch (NoResultException e) {
			logger.info("EmailAddress record not found by RowId (" + emailAddrRowId + "), ignored.");
		}
		return null;
	}

	public Subscription removeFromList(int bcstTrkRowId) {
		try {
			BroadcastTracking bt = trackingService.getByRowId(bcstTrkRowId);
			EmailAddress ea = bt.getEmailAddress();
			BroadcastMessage bm  = bt.getBroadcastMessage();
			return removeFromList(bm.getRowId(), ea.getRowId());
		}
		catch (NoResultException e) {
			logger.info("BroadcastTracking record not found by RowId (" + bcstTrkRowId + "), ignored.");
		}
		return null;
	}

}

package jpa.service.task;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.StatusId;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.model.EmailAddress;
import jpa.service.EmailAddressService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("activateAddress")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public class ActivateAddress extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(ActivateAddress.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressService emailAddrDao;
	
	/**
	 * Activate email addresses. The column "DataTypeValues" from MsgAction
	 * table should contain address types (FROM, TO, etc) that need to be
	 * activated.
	 * 
	 * @return a Long value representing the number of addresses that have been
	 *         activated.
	 */
	public Integer process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		if (StringUtils.isBlank(taskArguments)) {
			throw new DataValidationException("Arguments is not valued, nothing to activate");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + taskArguments);
		}
		
		// example: $From,$To,myaddress@mydomain.com
		int addrsActiveted = 0;
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		List<String> list = TaskBaseAdaptor.getArgumentList(taskArguments);
		for (Iterator<String> it=list.iterator(); it.hasNext(); ) {
			String addrs = null;
			String token = it.next();
			if (token != null && token.startsWith("$")) { // address variable
				token = token.substring(1);
				if (EmailAddrType.FROM_ADDR.getValue().equals(token)) {
					addrs = messageBean.getFromAsString();
				}
				else if (EmailAddrType.FINAL_RCPT_ADDR.getValue().equals(token)) {
					addrs = messageBean.getFinalRcpt();
				}
				else if (EmailAddrType.ORIG_RCPT_ADDR.getValue().equals(token)) {
					addrs = messageBean.getOrigRcpt();
				}
				else if (EmailAddrType.FORWARD_ADDR.getValue().equals(token)) {
					addrs = messageBean.getForwardAsString();
				}
				else if (EmailAddrType.TO_ADDR.getValue().equals(token)) {
					addrs = messageBean.getToAsString();
				}
				else if (EmailAddrType.REPLYTO_ADDR.getValue().equals(token)) {
					addrs = messageBean.getReplytoAsString();
				}
			}
			else { // real email address
				addrs = token;
			}
			
			Address[] iAddrs = null;
			if (StringUtils.isNotBlank(addrs)) {
				try {
					iAddrs = InternetAddress.parse(addrs);
				}
				catch (AddressException e) {
					logger.error("AddressException caught for: " + addrs + ", skip...");
				}
			}
			for (int i=0; iAddrs!=null && i<iAddrs.length; i++) {
				Address iAddr = iAddrs[i];
				String addr = iAddr.toString();
				if (isDebugEnabled) {
					logger.debug("Address to actiavte: " + addr);
				}
				EmailAddress emailAddrVo = emailAddrDao.findSertAddress(addr);
				if (!StatusId.ACTIVE.getValue().equals(emailAddrVo.getStatusId())) {
					if (isDebugEnabled) {
						logger.debug("Activating EmailAddr: " + addr);
					}
					emailAddrVo.setStatusId(StatusId.ACTIVE.getValue());
					emailAddrVo.setBounceCount(0); // reset bounce count
					emailAddrVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
					emailAddrVo.setStatusChangeTime(updtTime);
					emailAddrDao.update(emailAddrVo);
				}
				else { // email address already active, reset bounce count
					emailAddrVo.setBounceCount(0); // reset bounce count
					emailAddrDao.update(emailAddrVo);
				}
				addrsActiveted++;
			}
		}
		return addrsActiveted;
	}
}

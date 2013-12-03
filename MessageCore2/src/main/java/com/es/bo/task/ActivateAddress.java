package com.es.bo.task;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.address.EmailAddressDao;
import com.es.data.constant.Constants;
import com.es.data.constant.EmailAddressType;
import com.es.data.constant.StatusId;
import com.es.exception.DataValidationException;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.address.EmailAddressVo;

@Component("activateAddress")
@Transactional(propagation=Propagation.REQUIRED)
public class ActivateAddress extends TaskBaseAdaptor {
	private static final long serialVersionUID = 7272826841955633003L;
	static final Logger logger = Logger.getLogger(ActivateAddress.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressDao emailAddrDao;
	
	/**
	 * Activate email addresses. The column "DataTypeValues" from MsgAction
	 * table should contain address types (FROM, TO, etc) that need to be
	 * activated.
	 * 
	 * @return a Long value representing the number of addresses that have been
	 *         activated.
	 */
	public Long process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (StringUtils.isBlank(ctx.getTaskArguments())) {
			throw new DataValidationException("Arguments is not valued, nothing to activate");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + ctx.getTaskArguments());
		}
		
		MessageBean messageBean = ctx.getMessageBean();
		// example: $From,$To,myaddress@mydomain.com
		long addrsActiveted = 0;
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		List<String> list = getArgumentList(ctx.getTaskArguments());
		for (Iterator<String> it=list.iterator(); it.hasNext(); ) {
			String addrs = null;
			String token = it.next();
			if (token != null && token.startsWith("$")) { // address variable
				token = token.substring(1);
				if (EmailAddressType.FROM_ADDR.getValue().equals(token)) {
					addrs = messageBean.getFromAsString();
				}
				else if (EmailAddressType.FINAL_RCPT_ADDR.getValue().equals(token)) {
					addrs = messageBean.getFinalRcpt();
				}
				else if (EmailAddressType.ORIG_RCPT_ADDR.getValue().equals(token)) {
					addrs = messageBean.getOrigRcpt();
				}
				else if (EmailAddressType.FORWARD_ADDR.getValue().equals(token)) {
					addrs = messageBean.getForwardAsString();
				}
				else if (EmailAddressType.TO_ADDR.getValue().equals(token)) {
					addrs = messageBean.getToAsString();
				}
				else if (EmailAddressType.REPLYTO_ADDR.getValue().equals(token)) {
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
				EmailAddressVo emailAddrVo = emailAddrDao.findSertAddress(addr);
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

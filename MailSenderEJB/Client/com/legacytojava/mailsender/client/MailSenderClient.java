package com.legacytojava.mailsender.client;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.legacytojava.mailsender.ejb.MailSenderRemote;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

public class MailSenderClient {
	/* retrieve an email address using findByAddress() */
    public static void main(String[] args) throws Exception {
        MailSenderRemote sender = (MailSenderRemote) lookupRemoteEjb("ejb:MailSenderEar/MailSenderEJB/MailSender!com.legacytojava.mailsender.ejb.MailSenderRemote");
        if (sender==null) {
        	System.err.println("Failed to locate MailSender Remote");
        }
        EmailAddrVo vo = sender.findByAddress("test@test.com");
        System.out.println(vo);
    }
    
    private static Context ctx = null;
	public static Object lookupRemoteEjb(String jndiName) {
		try {
			if (ctx == null) {
				Hashtable<String,String> jndiProperties = new Hashtable<String,String>();
				jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
				ctx = new InitialContext(jndiProperties);
			}
			return ctx.lookup(jndiName);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

}

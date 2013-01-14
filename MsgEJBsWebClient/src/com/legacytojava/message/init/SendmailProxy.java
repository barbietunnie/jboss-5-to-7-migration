package com.legacytojava.message.init;

public class SendmailProxy implements com.legacytojava.message.init.Sendmail {
  private String _endpoint = null;
  private com.legacytojava.message.init.Sendmail sendmail = null;
  
  public SendmailProxy() {
    _initSendmailProxy();
  }
  
  public SendmailProxy(String endpoint) {
    _endpoint = endpoint;
    _initSendmailProxy();
  }
  
  private void _initSendmailProxy() {
    try {
      sendmail = (new com.legacytojava.message.init.SendmailServiceLocator()).getSendmail();
      if (sendmail != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)sendmail)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)sendmail)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (sendmail != null)
      ((javax.xml.rpc.Stub)sendmail)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.legacytojava.message.init.Sendmail getSendmail() {
    if (sendmail == null)
      _initSendmailProxy();
    return sendmail;
  }
  
  public int sendMailFromSite(java.lang.String siteId, java.lang.String toAddr, java.lang.String subject, java.lang.String body) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (sendmail == null)
      _initSendmailProxy();
    return sendmail.sendMailFromSite(siteId, toAddr, subject, body);
  }
  
  public int sendMail(java.lang.String fromAddr, java.lang.String toAddr, java.lang.String subject, java.lang.String body) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (sendmail == null)
      _initSendmailProxy();
    return sendmail.sendMail(fromAddr, toAddr, subject, body);
  }
  
  public int sendMailToSite(java.lang.String siteId, java.lang.String fromAddr, java.lang.String subject, java.lang.String body) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (sendmail == null)
      _initSendmailProxy();
    return sendmail.sendMailToSite(siteId, fromAddr, subject, body);
  }
  
  public static void main(String[] args) {
	  SendmailProxy sendMail = new SendmailProxy();
	  // trace SOAP messages using TCP Monitor
	  sendMail.setEndpoint("http://localhost:8090/MsgEJBsWeb/services/Sendmail");
	  try {
			//System.out.println(sendMail.sendMail("testfrom@test.com", "test@test.com",
			//		"From Sendmail Web Service", "Test body"));
			System.out.println(sendMail.sendMailToSite(null, "test@test.com",
					"From Sendmail Web Service", "Test body"));
			System.out.println(sendMail.sendMailFromSite(null, "test@test.com",
					"From Sendmail Web Service", "Test body"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
  }

}
package com.legacytojava.message.init;

public class MailinglistProxy implements com.legacytojava.message.init.Mailinglist {
  private String _endpoint = null;
  private com.legacytojava.message.init.Mailinglist mailinglist = null;
  
  public MailinglistProxy() {
    _initMailinglistProxy();
  }
  
  public MailinglistProxy(String endpoint) {
    _endpoint = endpoint;
    _initMailinglistProxy();
  }
  
  private void _initMailinglistProxy() {
    try {
      mailinglist = (new com.legacytojava.message.init.MailinglistServiceLocator()).getMailinglist();
      if (mailinglist != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)mailinglist)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)mailinglist)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (mailinglist != null)
      ((javax.xml.rpc.Stub)mailinglist)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.legacytojava.message.init.Mailinglist getMailinglist() {
    if (mailinglist == null)
      _initMailinglistProxy();
    return mailinglist;
  }
  
  public int subscribe(java.lang.String emailAddr, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (mailinglist == null)
      _initMailinglistProxy();
    return mailinglist.subscribe(emailAddr, listId);
  }
  
  public int unSubscribe(java.lang.String emailAddr, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (mailinglist == null)
      _initMailinglistProxy();
    return mailinglist.unSubscribe(emailAddr, listId);
  }
  
  public void optInRequest(java.lang.String emailAddr, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (mailinglist == null)
      _initMailinglistProxy();
    mailinglist.optInRequest(emailAddr, listId);
  }
  
  public void optInConfirm(java.lang.String emailAddr, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (mailinglist == null)
      _initMailinglistProxy();
    mailinglist.optInConfirm(emailAddr, listId);
  }
  
  public int sendMail(java.lang.String toAddr, com.legacytojava.message.init.VariableDto[] variables, java.lang.String templateId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (mailinglist == null)
      _initMailinglistProxy();
    return mailinglist.sendMail(toAddr, variables, templateId);
  }
  
  public int updateOpenCount(long emailAddrId, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (mailinglist == null)
      _initMailinglistProxy();
    return mailinglist.updateOpenCount(emailAddrId, listId);
  }
  
  public int updateClickCount(long emailAddrId, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (mailinglist == null)
      _initMailinglistProxy();
    return mailinglist.updateClickCount(emailAddrId, listId);
  }
  
  public int updateMsgOpenCount(long broadcastMsgId) throws java.rmi.RemoteException{
    if (mailinglist == null)
      _initMailinglistProxy();
    return mailinglist.updateMsgOpenCount(broadcastMsgId);
  }
  
  public int updateMsgClickCount(long broadcastMsgId) throws java.rmi.RemoteException{
    if (mailinglist == null)
      _initMailinglistProxy();
    return mailinglist.updateMsgClickCount(broadcastMsgId);
  }
  
  public static void main(String[] args) {
	  MailinglistProxy mlist = new MailinglistProxy();
	  int loops = 10; //1000
	  // trace SOAP messages using TCP Monitor
	  mlist.setEndpoint("http://localhost:8090/MsgEJBsWeb/services/Mailinglist");
	  try {
		  System.out.println(mlist.subscribe("test2@test.com", "SMPLLST1"));
		  System.out.println(mlist.unSubscribe("test2@test.com", "SMPLLST1"));
		  System.out.println(mlist.updateOpenCount(1L, "SMPLLST1"));
		  System.out.println(mlist.updateClickCount(1L, "SMPLLST1"));
		  System.out.println(mlist.updateMsgOpenCount(2));
		  System.out.println(mlist.updateMsgClickCount(2));
		  long startTime = new java.util.Date().getTime();
		  int i;
		  for (i = 0; i < loops; i++) {
			  String suffix = (i % 100) + "";
			  if (suffix.length() < 2) suffix = "0" + suffix;
			  String emailAddr = "test" + suffix + "@localhost";
			  VariableDto dto = new VariableDto();
			  dto.setName("CustomerName");
			  dto.setValue("Test Customer " + suffix);
			  VariableDto dto1 = new VariableDto();
			  dto1.setName("SubjectNumber");
			  dto1.setValue(" - " + suffix);
			  VariableDto dto2 = new VariableDto();
			  dto2.setName("SendingApp");
			  dto2.setValue("Mailinglist WSClient");
			  VariableDto[] variables = { dto, dto1, dto2 };
			  System.out.println(mlist.sendMail(emailAddr, variables, "SampleNewsletter2"));
		  }
		  long timeSpent = new java.util.Date().getTime() - startTime;
		  System.out.println("Number of Mails sent: " + i + ", Time taken: " + timeSpent / 1000
					+ " seconds");
	  }
	  catch (Exception e) {
		  e.printStackTrace();
	  }
  }

}
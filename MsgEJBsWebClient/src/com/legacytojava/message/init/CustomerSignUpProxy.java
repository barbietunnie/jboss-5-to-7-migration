package com.legacytojava.message.init;

public class CustomerSignUpProxy implements com.legacytojava.message.init.CustomerSignUp {
  private String _endpoint = null;
  private com.legacytojava.message.init.CustomerSignUp customerSignUp = null;
  
  public CustomerSignUpProxy() {
    _initCustomerSignUpProxy();
  }
  
  public CustomerSignUpProxy(String endpoint) {
    _endpoint = endpoint;
    _initCustomerSignUpProxy();
  }
  
  private void _initCustomerSignUpProxy() {
    try {
      customerSignUp = (new com.legacytojava.message.init.CustomerSignUpServiceLocator()).getCustomerSignUp();
      if (customerSignUp != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)customerSignUp)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)customerSignUp)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (customerSignUp != null)
      ((javax.xml.rpc.Stub)customerSignUp)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.legacytojava.message.init.CustomerSignUp getCustomerSignUp() {
    if (customerSignUp == null)
      _initCustomerSignUpProxy();
    return customerSignUp;
  }
  
  public int addToList(java.lang.String emailAddr, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (customerSignUp == null)
      _initCustomerSignUpProxy();
    return customerSignUp.addToList(emailAddr, listId);
  }
  
  public java.lang.String signUpOnly(com.legacytojava.message.init.CustomerDto dto) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (customerSignUp == null)
      _initCustomerSignUpProxy();
    return customerSignUp.signUpOnly(dto);
  }
  
  public java.lang.String signUpAndSubscribe(com.legacytojava.message.init.CustomerDto dto, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (customerSignUp == null)
      _initCustomerSignUpProxy();
    return customerSignUp.signUpAndSubscribe(dto, listId);
  }
  
  public com.legacytojava.message.init.CustomerDto getCustomer(java.lang.String emailAddr) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (customerSignUp == null)
      _initCustomerSignUpProxy();
    return customerSignUp.getCustomer(emailAddr);
  }
  
  public java.lang.String updateCustomer(com.legacytojava.message.init.CustomerDto dto) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (customerSignUp == null)
      _initCustomerSignUpProxy();
    return customerSignUp.updateCustomer(dto);
  }
  
  public int removeCustomer(java.lang.String emailAddr) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (customerSignUp == null)
      _initCustomerSignUpProxy();
    return customerSignUp.removeCustomer(emailAddr);
  }
  
  public int removeFromList(java.lang.String emailAddr, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException{
    if (customerSignUp == null)
      _initCustomerSignUpProxy();
    return customerSignUp.removeFromList(emailAddr, listId);
  }
  
  public static void main(String[] args) {
	  CustomerSignUpProxy signup = new CustomerSignUpProxy();
	  // trace SOAP messages using TCP Monitor
	  signup.setEndpoint("http://localhost:8090/MsgEJBsWeb/services/CustomerSignUp");
	  try {
		  System.out.println(signup.removeFromList("jsmith@test.com", "SMPLLST1"));
		  System.out.println(signup.addToList("jsmith@test.com", "SMPLLST1"));
		  System.out.println(signup.removeCustomer("test2@test.com"));
		  CustomerDto dto = new CustomerDto();
		  dto.setClientId("System");
		  dto.setFirstName("test2");
		  dto.setLastName("Customer");
		  dto.setEmailAddr("test2@test.com");
		  System.out.println(signup.signUpAndSubscribe(dto, "SMPLLST1"));
		  System.out.println(StringUtil.prettyPrint(signup.getCustomer("jsmith@test.com")));
	  }
	  catch (Exception e) {
		  e.printStackTrace();
	  }
  }
}
/**
 * SendmailServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.legacytojava.message.init;

public class SendmailServiceLocator extends org.apache.axis.client.Service implements com.legacytojava.message.init.SendmailService {

    public SendmailServiceLocator() {
    }


    public SendmailServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SendmailServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for Sendmail
    private java.lang.String Sendmail_address = "http://localhost:8080/MsgEJBsWeb/services/Sendmail";

    public java.lang.String getSendmailAddress() {
        return Sendmail_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SendmailWSDDServiceName = "Sendmail";

    public java.lang.String getSendmailWSDDServiceName() {
        return SendmailWSDDServiceName;
    }

    public void setSendmailWSDDServiceName(java.lang.String name) {
        SendmailWSDDServiceName = name;
    }

    public com.legacytojava.message.init.Sendmail getSendmail() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(Sendmail_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSendmail(endpoint);
    }

    public com.legacytojava.message.init.Sendmail getSendmail(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.legacytojava.message.init.SendmailSoapBindingStub _stub = new com.legacytojava.message.init.SendmailSoapBindingStub(portAddress, this);
            _stub.setPortName(getSendmailWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSendmailEndpointAddress(java.lang.String address) {
        Sendmail_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.legacytojava.message.init.Sendmail.class.isAssignableFrom(serviceEndpointInterface)) {
                com.legacytojava.message.init.SendmailSoapBindingStub _stub = new com.legacytojava.message.init.SendmailSoapBindingStub(new java.net.URL(Sendmail_address), this);
                _stub.setPortName(getSendmailWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("Sendmail".equals(inputPortName)) {
            return getSendmail();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://init.message.legacytojava.com", "SendmailService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://init.message.legacytojava.com", "Sendmail"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("Sendmail".equals(portName)) {
            setSendmailEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}

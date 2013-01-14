/**
 * MailinglistServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.legacytojava.message.init;

public class MailinglistServiceLocator extends org.apache.axis.client.Service implements com.legacytojava.message.init.MailinglistService {

    public MailinglistServiceLocator() {
    }


    public MailinglistServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public MailinglistServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for Mailinglist
    private java.lang.String Mailinglist_address = "http://localhost:8080/MsgEJBsWeb/services/Mailinglist";

    public java.lang.String getMailinglistAddress() {
        return Mailinglist_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String MailinglistWSDDServiceName = "Mailinglist";

    public java.lang.String getMailinglistWSDDServiceName() {
        return MailinglistWSDDServiceName;
    }

    public void setMailinglistWSDDServiceName(java.lang.String name) {
        MailinglistWSDDServiceName = name;
    }

    public com.legacytojava.message.init.Mailinglist getMailinglist() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(Mailinglist_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getMailinglist(endpoint);
    }

    public com.legacytojava.message.init.Mailinglist getMailinglist(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.legacytojava.message.init.MailinglistSoapBindingStub _stub = new com.legacytojava.message.init.MailinglistSoapBindingStub(portAddress, this);
            _stub.setPortName(getMailinglistWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setMailinglistEndpointAddress(java.lang.String address) {
        Mailinglist_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.legacytojava.message.init.Mailinglist.class.isAssignableFrom(serviceEndpointInterface)) {
                com.legacytojava.message.init.MailinglistSoapBindingStub _stub = new com.legacytojava.message.init.MailinglistSoapBindingStub(new java.net.URL(Mailinglist_address), this);
                _stub.setPortName(getMailinglistWSDDServiceName());
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
        if ("Mailinglist".equals(inputPortName)) {
            return getMailinglist();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://init.message.legacytojava.com", "MailinglistService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://init.message.legacytojava.com", "Mailinglist"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("Mailinglist".equals(portName)) {
            setMailinglistEndpointAddress(address);
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

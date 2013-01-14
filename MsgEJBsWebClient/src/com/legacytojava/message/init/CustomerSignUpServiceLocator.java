/**
 * CustomerSignUpServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.legacytojava.message.init;

public class CustomerSignUpServiceLocator extends org.apache.axis.client.Service implements com.legacytojava.message.init.CustomerSignUpService {

    public CustomerSignUpServiceLocator() {
    }


    public CustomerSignUpServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public CustomerSignUpServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for CustomerSignUp
    private java.lang.String CustomerSignUp_address = "http://localhost:8080/MsgEJBsWeb/services/CustomerSignUp";

    public java.lang.String getCustomerSignUpAddress() {
        return CustomerSignUp_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String CustomerSignUpWSDDServiceName = "CustomerSignUp";

    public java.lang.String getCustomerSignUpWSDDServiceName() {
        return CustomerSignUpWSDDServiceName;
    }

    public void setCustomerSignUpWSDDServiceName(java.lang.String name) {
        CustomerSignUpWSDDServiceName = name;
    }

    public com.legacytojava.message.init.CustomerSignUp getCustomerSignUp() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(CustomerSignUp_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getCustomerSignUp(endpoint);
    }

    public com.legacytojava.message.init.CustomerSignUp getCustomerSignUp(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.legacytojava.message.init.CustomerSignUpSoapBindingStub _stub = new com.legacytojava.message.init.CustomerSignUpSoapBindingStub(portAddress, this);
            _stub.setPortName(getCustomerSignUpWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setCustomerSignUpEndpointAddress(java.lang.String address) {
        CustomerSignUp_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.legacytojava.message.init.CustomerSignUp.class.isAssignableFrom(serviceEndpointInterface)) {
                com.legacytojava.message.init.CustomerSignUpSoapBindingStub _stub = new com.legacytojava.message.init.CustomerSignUpSoapBindingStub(new java.net.URL(CustomerSignUp_address), this);
                _stub.setPortName(getCustomerSignUpWSDDServiceName());
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
        if ("CustomerSignUp".equals(inputPortName)) {
            return getCustomerSignUp();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://init.message.legacytojava.com", "CustomerSignUpService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://init.message.legacytojava.com", "CustomerSignUp"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("CustomerSignUp".equals(portName)) {
            setCustomerSignUpEndpointAddress(address);
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

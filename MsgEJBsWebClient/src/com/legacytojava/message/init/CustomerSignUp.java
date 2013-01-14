/**
 * CustomerSignUp.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.legacytojava.message.init;

public interface CustomerSignUp extends java.rmi.Remote {
    public int addToList(java.lang.String emailAddr, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public java.lang.String signUpOnly(com.legacytojava.message.init.CustomerDto dto) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public java.lang.String signUpAndSubscribe(com.legacytojava.message.init.CustomerDto dto, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public com.legacytojava.message.init.CustomerDto getCustomer(java.lang.String emailAddr) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public java.lang.String updateCustomer(com.legacytojava.message.init.CustomerDto dto) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public int removeCustomer(java.lang.String emailAddr) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public int removeFromList(java.lang.String emailAddr, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
}

/**
 * Mailinglist.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.legacytojava.message.init;

public interface Mailinglist extends java.rmi.Remote {
    public int subscribe(java.lang.String emailAddr, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public int unSubscribe(java.lang.String emailAddr, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public void optInRequest(java.lang.String emailAddr, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public void optInConfirm(java.lang.String emailAddr, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public int sendMail(java.lang.String toAddr, com.legacytojava.message.init.VariableDto[] variables, java.lang.String templateId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public int updateOpenCount(long emailAddrId, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public int updateClickCount(long emailAddrId, java.lang.String listId) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public int updateMsgOpenCount(long broadcastMsgId) throws java.rmi.RemoteException;
    public int updateMsgClickCount(long broadcastMsgId) throws java.rmi.RemoteException;
}

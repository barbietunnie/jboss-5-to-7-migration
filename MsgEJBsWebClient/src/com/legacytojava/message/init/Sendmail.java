/**
 * Sendmail.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.legacytojava.message.init;

public interface Sendmail extends java.rmi.Remote {
    public int sendMailFromSite(java.lang.String siteId, java.lang.String toAddr, java.lang.String subject, java.lang.String body) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public int sendMail(java.lang.String fromAddr, java.lang.String toAddr, java.lang.String subject, java.lang.String body) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
    public int sendMailToSite(java.lang.String siteId, java.lang.String fromAddr, java.lang.String subject, java.lang.String body) throws java.rmi.RemoteException, com.legacytojava.message.exception.DataValidationException;
}

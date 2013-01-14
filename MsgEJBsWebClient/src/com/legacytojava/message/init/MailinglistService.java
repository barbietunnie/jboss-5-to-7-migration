/**
 * MailinglistService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.legacytojava.message.init;

public interface MailinglistService extends javax.xml.rpc.Service {
    public java.lang.String getMailinglistAddress();

    public com.legacytojava.message.init.Mailinglist getMailinglist() throws javax.xml.rpc.ServiceException;

    public com.legacytojava.message.init.Mailinglist getMailinglist(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}

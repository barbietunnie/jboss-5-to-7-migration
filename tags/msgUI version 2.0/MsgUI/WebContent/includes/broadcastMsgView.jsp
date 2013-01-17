<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<h:messages styleClass="errors" layout="list"
	rendered="#{debug.showMessages}" />

<h:panelGrid columns="2" styleClass="smtpHeaders" 
	columnClasses="smtpLabelColumn, smtpTextColumn">
	
	<h:outputText value="#{msgs.fromAddressPrompt}" />
	<h:outputText value="#{broadcastsListBean.broadcastMsg.fromAddress}"/>

	<h:outputText value="#{msgs.toAddressPrompt}" />
	<h:outputText value="#{broadcastsListBean.broadcastMsg.toAddress}"/>

	<h:outputText value="#{msgs.msgSubjectPrompt}" />
	<h:outputText value="#{broadcastsListBean.broadcastMsg.msgSubject}" />

	<h:outputText value="#{msgs.receivedDatePrompt}" />
	<h:outputText value="#{broadcastsListBean.broadcastMsg.receivedDate}">
		<f:convertDateTime dateStyle="default"/>
	</h:outputText>

	<h:outputText value="#{msgs.siteIdPrompt}" 
		rendered="#{broadcastsListBean.broadcastMsg.clientId != null}" />
	<h:outputText value="#{broadcastsListBean.broadcastMsg.clientId}" 
		rendered="#{broadcastsListBean.broadcastMsg.clientId != null}" />
	
	<h:outputText value="#{msgs.ruleNamePrompt}" />
	<h:outputText value="#{broadcastsListBean.broadcastMsg.ruleName}" />

	<h:outputText value="#{msgs.bodyContentTypePrompt}" />
	<h:outputText value="#{broadcastsListBean.broadcastMsg.bodyContentType}" />
</h:panelGrid>

<h:dataTable value="#{broadcastsListBean.broadcastMsg.msgHeaders}" var="hdr" 
 			styleClass="smtpHeaders"
 			columnClasses="smtpLabelColumn, smtpTextColumn"
 			rendered="#{broadcastsListBean.broadcastMsg.showAllHeaders}">
   <h:column>
      <h:outputText value="#{hdr.headerName}:"/>
   </h:column>
   <h:column>
      <h:outputText value="#{hdr.headerValue}"/>
   </h:column>
</h:dataTable>

<f:verbatim><br/></f:verbatim>
<h:panelGrid columns="1" styleClass="smtpBody">
	<h:outputText value="#{broadcastsListBean.broadcastMsg.displayBody}" escape="false" 
		rendered="#{not broadcastsListBean.broadcastMsg.showRawMessage}"/>
	<h:outputText value="#{broadcastsListBean.broadcastMsg.rawMessage}" escape="false" 
		rendered="#{broadcastsListBean.broadcastMsg.showRawMessage}"/>
</h:panelGrid>

<h:panelGrid columns="2"
	styleClass="commandBar" columnClasses="alignLeft30, alignRight70">
	<h:panelGroup style="text-align: left;">
		<h:commandButton value="#{msgs.backButtonText}" title="Go back to List"
			action="#{broadcastsListBean.cancelEdit}"/>
	<%-- h:commandButton type="reset" value="#{msgs.backButtonText}"
			onclick="javascript:history.back()"/ --%>
	</h:panelGroup>
	<h:panelGroup style="text-align: right;">
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>

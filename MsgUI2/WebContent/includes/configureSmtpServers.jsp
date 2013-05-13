<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.configureSmtpServers}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable value="#{smtpsvrs.all}" var="smtp" 
   styleClass="jsfDataTable" 
   headerClass="dataTableHeader" columnClasses="oddColumns, evenColumns">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.deleteColumnHeader}"/>
      </f:facet>
      <h:selectBooleanCheckbox value="#{smtp.markedForDeletion}" 
         disabled="#{not smtp.editable}" onchange="submit()"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.serverNameHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:commandLink action="#{smtpsvrs.viewSmtpServer}">
      	<h:outputText value="#{smtp.serverName}" title="Click to Edit"/>
      	<f:param name="serverName" value="#{smtp.serverName}"/>
      </h:commandLink>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.hostNameHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:outputText value="#{smtp.smtpHost}"/>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.portNumberHeader}"/>
      </f:facet>
      <h:outputText value="#{smtp.smtpPort}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.userIdHeader}"/>
      </f:facet>
      <h:outputText value="#{smtp.userId}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.useSslHeader}"/>
      </f:facet>
      <h:outputText value="#{smtp.useSsl}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.persistenceHeader}"/>
      </f:facet>
      <h:outputText value="#{smtp.persistence}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.serverTypeHeader}"/>
      </f:facet>
      <h:outputText value="#{smtp.serverType}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.statusIdHeader}"/>
      </f:facet>
      <h:outputText value="#{smtp.statusIdDesc}"/>
   </h:column>
</h:dataTable>
<f:verbatim><p/></f:verbatim>
<h:panelGrid columns="2" styleClass="commandBar"
	columnClasses="alignLeft70, alignRight30">
	<h:panelGroup>
		<h:commandButton value="#{msgs.deleteButtonText}"
			title="Delete selected rows" action="#{smtpsvrs.deleteSmtpServers}"
			disabled="#{not smtpsvrs.anyServersMarkedForDeletion}"
			onclick="javascript:return confirmDelete();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshFromDB}"
			title="Refresh from database" action="#{smtpsvrs.refresh}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.copyToNewButtonText}"
			title="Create a new row from selected"
			action="#{smtpsvrs.copySmtpServer}"
			disabled="#{not smtpsvrs.anyServersMarkedForDeletion}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.addNewButtonText}"
			title="Add a new row" action="#{smtpsvrs.addSmtpServer}" />
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>

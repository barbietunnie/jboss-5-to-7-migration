<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.configureMailboxes}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable value="#{mailboxes.all}" var="mbox" 
   styleClass="jsfDataTable" 
   headerClass="dataTableHeader" columnClasses="oddColumns, evenColumns">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.deleteColumnHeader}"/>
      </f:facet>
      <h:selectBooleanCheckbox value="#{mbox.markedForDeletion}" 
         disabled="#{not mbox.editable}" onchange="submit()"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.hostNameHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:commandLink action="#{mailboxes.viewMailBox}">
      	<h:outputText value="#{mbox.hostName}" title="Click to Edit"/>
      	<f:param name="hostName" value="#{mbox.hostName}"/>
      	<f:param name="userId" value="#{mbox.userId}"/>
      </h:commandLink>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.portNumberHeader}"/>
      </f:facet>
      <h:outputText value="#{mbox.portNumber}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.protocolHeader}"/>
      </f:facet>
      <h:outputText value="#{mbox.protocol}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.userIdHeader}"/>
      </f:facet>
      <h:outputText value="#{mbox.userId}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.descriptionHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:outputText value="#{mbox.mailBoxDesc}"/>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.useSslHeader}"/>
      </f:facet>
      <h:outputText value="#{mbox.useSsl}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.statusIdHeader}"/>
      </f:facet>
      <h:outputText value="#{mbox.statusIdDesc}"/>
   </h:column>
</h:dataTable>
<f:verbatim><p/></f:verbatim>
<h:panelGrid columns="2" styleClass="commandBar"
	columnClasses="alignLeft70, alignRight30">
	<h:panelGroup>
		<h:commandButton value="#{msgs.deleteButtonText}"
			title="Delete selected rows" action="#{mailboxes.deleteMailBoxes}"
			disabled="#{not mailboxes.anyMailBoxsMarkedForDeletion}"
			onclick="javascript:return confirmDelete();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshFromDB}"
			title="Refresh from database" action="#{mailboxes.refresh}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.copyToNewButtonText}"
			title="Create a new row from selected"
			action="#{mailboxes.copyMailbox}"
			disabled="#{not mailboxes.anyMailBoxsMarkedForDeletion}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.addNewButtonText}"
			title="Add a new row" action="#{mailboxes.addMailbox}" />
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.manageUserAccounts}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable value="#{useraccounts.all}" var="list" 
   styleClass="jsfDataTable" 
   headerClass="dataTableHeader" columnClasses="oddColumns, evenColumns">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.deleteColumnHeader}"/>
      </f:facet>
      <h:selectBooleanCheckbox value="#{list.markedForDeletion}" 
         disabled="#{not list.editable}" onchange="submit()"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.userIdHeader}"/>
      </f:facet>
      <h:commandLink action="#{useraccounts.viewUser}">
      	<h:outputText value="#{list.userId}" title="Click to Edit"/>
      	<f:param name="userId" value="#{list.userId}"/>
      </h:commandLink>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.firstNameHeader}"/>
      </f:facet>
      <h:outputText value="#{list.firstName}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.lastNameHeader}"/>
      </f:facet>
      <h:outputText value="#{list.lastName}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.roleHeader}"/>
      </f:facet>
      <h:outputText value="#{list.role}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.emailAddrHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:outputText value="#{list.emailAddr}"/>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.statusIdHeader}"/>
      </f:facet>
      <h:outputText value="#{list.statusIdDesc}"/>
   </h:column>
</h:dataTable>
<f:verbatim><p/></f:verbatim>
<h:panelGrid columns="2" styleClass="commandBar"
	columnClasses="alignLeft70, alignRight30">
	<h:panelGroup>
		<h:commandButton value="#{msgs.deleteButtonText}"
			title="Delete selected rows" action="#{useraccounts.deleteUsers}"
			disabled="#{not useraccounts.anyUsersMarkedForDeletion}"
			onclick="javascript:return confirmDelete();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshFromDB}"
			title="Refresh from database" action="#{useraccounts.refresh}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.copyToNewButtonText}"
			title="Create a new row from selected"
			action="#{useraccounts.copyUser}"
			disabled="#{not useraccounts.anyUsersMarkedForDeletion}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.addNewButtonText}"
			title="Add a new row" action="#{useraccounts.addUser}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.backButtonText}" action="#{useraccounts.cancelEdit}"/>
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>

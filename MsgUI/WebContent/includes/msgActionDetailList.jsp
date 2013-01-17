<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.maintainActionDetails}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable value="#{actiondetails.all}" var="actiondetail" 
   styleClass="jsfDataTable" headerClass="dataTableHeader"
   rowClasses="oddRows, evenRows">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.deleteColumnHeader}"/>
      </f:facet>
      <h:selectBooleanCheckbox value="#{actiondetail.markedForDeletion}" 
         disabled="#{not actiondetail.editable}" onchange="submit()"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.actionIdHeader}"/>
      </f:facet>
      <h:commandLink action="#{actiondetails.viewMsgActionDetail}">
      	<h:outputText value="#{actiondetail.actionId}" title="Click to Edit"/>
      	<f:param name="actionDetailId" value="#{actiondetail.actionId}"/>
      </h:commandLink>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.descriptionHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:outputText value="#{actiondetail.description}"/>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.processBeanIdHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:outputText value="#{actiondetail.processBeanId}"/>
      </div>
   </h:column>
   <%-- h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.processClassNameHeader}"/>
      </f:facet>
      <h:outputText value="#{actiondetail.processClassName}"/>
   </h:column --%>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.dataTypeHeader}"/>
      </f:facet>
      <h:outputText value="#{actiondetail.dataType}"/>
   </h:column>
</h:dataTable>
<f:verbatim><p/></f:verbatim>
<h:panelGrid columns="2" styleClass="commandBar"
	columnClasses="alignLeft70, alignRight30">
	<h:panelGroup>
		<h:commandButton value="#{msgs.deleteButtonText}"
			title="Delete selected rows"
			action="#{actiondetails.deleteMsgActionDetails}"
			disabled="#{not actiondetails.anyActionDetailsMarkedForDeletion}"
			onclick="javascript:return confirmDelete();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshFromDB}"
			title="Refresh from database" action="#{actiondetails.refresh}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.copyToNewButtonText}"
			title="Create a new row from selected"
			action="#{actiondetails.copyMsgActionDetail}"
			disabled="#{not actiondetails.anyActionDetailsMarkedForDeletion}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.addNewButtonText}"
			title="Add a new row" action="#{actiondetails.addMsgActionDetail}" />
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>

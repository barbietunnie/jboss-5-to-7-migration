<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.configureEmailVariables}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable value="#{emailvariables.all}" var="varbl" 
   styleClass="jsfDataTable" 
   headerClass="dataTableHeader" columnClasses="oddColumns, evenColumns">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.deleteColumnHeader}"/>
      </f:facet>
      <h:selectBooleanCheckbox value="#{varbl.markedForDeletion}" 
         disabled="#{not varbl.editable || varbl.isBuiltInVariable}" onchange="submit()"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.variableNameHeader}"/>
      </f:facet>
      <h:commandLink action="#{emailvariables.viewEmailVariable}">
      	<h:outputText value="#{varbl.variableName}" title="Click to Edit"/>
      	<f:param name="variableName" value="#{varbl.variableName}"/>
      </h:commandLink>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.defaultValueHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:outputText value="#{varbl.defaultValue}"/>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.variableQueryHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:outputText value="#{varbl.variableQueryShort}"/>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.variableProcHeader}"/>
      </f:facet>
      <h:outputText value="#{varbl.classNameShort}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.statusIdHeader}"/>
      </f:facet>
      <h:outputText value="#{varbl.statusIdDesc}"/>
   </h:column>
</h:dataTable>
<f:verbatim><p/></f:verbatim>
<h:panelGrid columns="2" styleClass="commandBar"
	columnClasses="alignLeft70, alignRight30">
	<h:panelGroup>
		<h:commandButton value="#{msgs.deleteButtonText}"
			title="Delete selected rows" action="#{emailvariables.deleteEmailVariables}"
			disabled="#{not emailvariables.anyListsMarkedForDeletion}"
			onclick="javascript:return confirmDelete();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshFromDB}"
			title="Refresh from database" action="#{emailvariables.refresh}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.copyToNewButtonText}"
			title="Create a new row from selected"
			action="#{emailvariables.copyEmailVariable}"
			disabled="#{not emailvariables.anyListsMarkedForDeletion}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.addNewButtonText}"
			title="Add a new row" action="#{emailvariables.addEmailVariable}" />
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>

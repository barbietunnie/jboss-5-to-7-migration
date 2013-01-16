<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.configureEmailTemplates}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable value="#{emailtemplates.all}" var="tmplt" 
   styleClass="jsfDataTable" 
   headerClass="dataTableHeader" columnClasses="oddColumns, evenColumns">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.deleteColumnHeader}"/>
      </f:facet>
      <h:selectBooleanCheckbox value="#{tmplt.markedForDeletion}" 
         disabled="#{not tmplt.editable || tmplt.isBuiltInTemplate}" onchange="submit()"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.templateIdHeader}"/>
      </f:facet>
      <h:commandLink action="#{emailtemplates.viewEmailTemplate}">
      	<h:outputText value="#{tmplt.templateId}" title="Click to Edit"/>
      	<f:param name="templateId" value="#{tmplt.templateId}"/>
      </h:commandLink>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.defaultListIdHeader}"/>
      </f:facet>
      <h:outputText value="#{tmplt.listId}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.subjectHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:outputText value="#{tmplt.subject}"/>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.listTypeHeader}"/>
      </f:facet>
      <h:outputText value="#{tmplt.listType}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.deliveryOptionHeader}"/>
      </f:facet>
      <h:outputText value="#{tmplt.deliveryOptionDesc}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.schedulesHeader}"/>
      </f:facet>
      <h:commandLink action="#{emailtemplates.editSchedules}">
      	<h:outputText value="Edit" title="Click to Edit Schedules"/>
      	<f:param name="templateId" value="#{tmplt.templateId}"/>
      </h:commandLink>
   </h:column>
</h:dataTable>
<f:verbatim><p/></f:verbatim>
<h:panelGrid columns="2" styleClass="commandBar"
	columnClasses="alignLeft70, alignRight30">
	<h:panelGroup>
		<h:commandButton value="#{msgs.deleteButtonText}"
			title="Delete selected rows" action="#{emailtemplates.deleteEmailTemplates}"
			disabled="#{not emailtemplates.anyTemplatesMarkedForDeletion}"
			onclick="javascript:return confirmDelete();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshFromDB}"
			title="Refresh from database" action="#{emailtemplates.refresh}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.copyToNewButtonText}"
			title="Create a new row from selected"
			action="#{emailtemplates.copyEmailTemplate}"
			disabled="#{not emailtemplates.anyTemplatesMarkedForDeletion}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.addNewButtonText}"
			title="Add a new row" action="#{emailtemplates.addEmailTemplate}" />
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>

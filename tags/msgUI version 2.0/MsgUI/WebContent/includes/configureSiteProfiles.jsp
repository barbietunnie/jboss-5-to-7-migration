<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.configureSiteProfiles}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable value="#{profileBean.all}" var="list" 
   styleClass="jsfDataTable" 
   headerClass="dataTableHeader" columnClasses="oddColumns, evenColumns">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.deleteColumnHeader}"/>
      </f:facet>
      <h:selectBooleanCheckbox value="#{list.markedForDeletion}" 
         disabled="#{not list.editable || list.isSystemClient}" onchange="submit()"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.siteIdHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:commandLink action="#{profileBean.viewSiteProfile}">
      	<h:outputText value="#{list.clientId}" title="Click to Edit"/>
      	<f:param name="clientId" value="#{list.clientId}"/>
      </h:commandLink>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.siteNameHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:outputText value="#{list.clientName}"/>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.emberEmailIdHeader}"/>
      </f:facet>
      <h:outputText value="#{list.embedEmailId}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.useTestAddressHeader}"/>
      </f:facet>
      <h:outputText value="#{list.useTestAddr}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.enableVerpHeader}"/>
      </f:facet>
      <h:outputText value="#{list.isVerpEnabled}"/>
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
			title="Delete selected rows" action="#{profileBean.deleteSiteProfiles}"
			disabled="#{not profileBean.anySitesMarkedForDeletion}"
			onclick="javascript:return confirmDelete();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshFromDB}"
			title="Refresh from database" action="#{profileBean.refresh}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.copyToNewButtonText}"
			title="Create a new row from selected"
			action="#{profileBean.copySiteProfile}"
			disabled="#{not profileBean.anySitesMarkedForDeletion}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.addNewButtonText}"
			title="Add a new row" action="#{profileBean.addSiteProfile}" />
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>

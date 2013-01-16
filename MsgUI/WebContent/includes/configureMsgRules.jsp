<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.configureCustomRules}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable value="#{msgrules.all}" var="rule" 
   styleClass="jsfDataTable" 
   headerClass="dataTableHeader" columnClasses="oddColumns, evenColumns">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.deleteColumnHeader}"/>
      </f:facet>
      <h:selectBooleanCheckbox value="#{rule.markedForDeletion}" 
         disabled="#{not rule.editable}" onchange="submit()"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.ruleNameHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:commandLink action="#{msgrules.viewRuleLogic}">
      	<h:outputText value="#{rule.ruleName}" title="Click to Edit Rules"/>
      	<f:param name="ruleName" value="#{rule.ruleName}"/>
      </h:commandLink>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.msgActionsHeader}"/>
      </f:facet>
      <h:commandLink action="#{msgrules.viewMsgActions}" rendered="#{not rule.subRule}">
      	<h:outputText value="Add" title="Click to Add Actions"
      		rendered="#{not msgrules.hasMsgActions}"/>
      	<h:outputText value="Edit" title="Click to Edit Actions"
      		rendered="#{msgrules.hasMsgActions}"/>
      </h:commandLink>
      <h:outputText value="n/a" rendered="#{rule.subRule}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.isSubRuleHeader}"/>
      </f:facet>
      <h:commandLink action="#{msgrules.viewSubRules}" rendered="#{not rule.subRule}">
      	<h:outputText value="#{rule.isSubRuleDesc}" title="Click to Add/Edit Sub-Rules"/>
      </h:commandLink>
      <h:outputText value="#{rule.isSubRuleDesc}" style="font-style: italic; font-size: 0.8em;"
      	rendered="#{rule.subRule}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.ruleTypeHeader}"/>
      </f:facet>
      <h:outputText value="#{rule.ruleType}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.startTimeHeader}"/>
      </f:facet>
      <h:outputText value="#{rule.startTime}">
      	<f:convertDateTime pattern="MM/dd/yyyy"/>
      	<%-- f:convertDateTime pattern="MM/dd/yyyy kk:mm"/ --%>
      </h:outputText>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.mailTypeHeader}"/>
      </f:facet>
      <h:outputText value="#{rule.mailType}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.statusIdHeader}"/>
      </f:facet>
      <h:outputText value="#{rule.statusIdDesc}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.ruleCategoryHeader}"/>
      </f:facet>
      <h:outputText value="#{rule.ruleCategoryDesc}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.moveUpHeader}"/>
      </f:facet>
      <h:commandLink action="#{msgrules.moveUp}" immediate="true"
      	rendered="#{msgrules.ruleLogics.rowIndex > 0 && msgrules.canMoveUp}">
      	<h:graphicImage value="/images/greenUp.gif" title="Move Up" style="border: 0"/>
      </h:commandLink>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.moveDownHeader}"/>
      </f:facet>
      <h:commandLink action="#{msgrules.moveDown}" immediate="true"
      	rendered="#{msgrules.ruleLogics.rowIndex < (msgrules.ruleLogics.rowCount-1)
      		&& msgrules.canMoveDown}">
      	<h:graphicImage value="/images/greenDown.gif" title="Move Down" style="border: 0"/>
      </h:commandLink>
   </h:column>
</h:dataTable>
<f:verbatim><p/></f:verbatim>
<h:panelGrid columns="2" styleClass="commandBar"
	columnClasses="alignLeft70, alignRight30">
	<h:panelGroup>
		<h:commandButton value="#{msgs.deleteButtonText}"
			title="Delete selected rules" action="#{msgrules.deleteRuleLogics}"
			disabled="#{not msgrules.anyRulesMarkedForDeletion}"
			onclick="javascript:return confirmDelete();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshFromDB}"
			title="Refresh from database" action="#{msgrules.refresh}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.copyToNewButtonText}"
			title="Create a new rule from selected"
			action="#{msgrules.copyRuleLogic}"
			disabled="#{not msgrules.anyRulesMarkedForDeletion}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.addNewButtonText}"
			title="Add a new rule" action="#{msgrules.addRuleLogic}" />
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>

<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.customizeBuiltInRules}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable value="#{builtinrules.all}" var="rule" 
   styleClass="jsfDataTable" 
   headerClass="dataTableHeader" columnClasses="oddColumns, evenColumns">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.ruleNameHeader}"/>
      </f:facet>
   	  <h:outputText value="#{rule.ruleName}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.msgActionsHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:commandLink action="#{builtinrules.viewMsgActions}">
      	<h:outputText value="Create" title="Click to Creat Actions"
      		rendered="#{not builtinrules.hasMsgActions}"/>
      	<h:outputText value="Edit" title="Click to Edit Actions"
      		rendered="#{builtinrules.hasMsgActions}"/>
      </h:commandLink>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.isSubRuleHeader}"/>
      </f:facet>
      <h:outputText value="#{rule.isSubRuleDesc}"/>
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
</h:dataTable>
<f:verbatim><p/></f:verbatim>
<h:panelGrid columns="2" styleClass="commandBar"
	columnClasses="alignLeft70, alignRight30">
	<h:panelGroup>
		<h:commandButton value="#{msgs.refreshFromDB}"
			title="Refresh from database" action="#{builtinrules.refresh}" />
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>
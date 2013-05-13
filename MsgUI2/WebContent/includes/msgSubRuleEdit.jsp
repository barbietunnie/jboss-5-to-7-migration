<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.msgRuleEditLabel}" styleClass="gridHeader">
	   <f:param value="#{msgrules.ruleLogic.ruleName}"/>
	</h:outputFormat>
	<h:panelGrid columns="2" styleClass="editPaneHeader" 
		columnClasses="labelColumn, textColumn">
		<h:outputText value="#{msgs.ruleNamePrompt}"/>
		<h:outputText id="rulename" value="#{msgrules.ruleLogic.ruleName}"/>
		
		<h:outputText value="#{msgs.ruleSeqPrompt}"/>
		<h:outputText id="ruleseq" value="#{msgrules.ruleLogic.ruleSeq}"/>
		
		<h:outputText value="#{msgs.statusIdPrompt}"/>
		<h:outputText id="statusid" value="#{msgrules.ruleLogic.statusIdDesc}"/>
		
		<h:outputText value="#{msgs.startTimePrompt}"/>
		<h:outputText id="starttime" value="#{msgrules.ruleLogic.startTime}">
			<f:convertDateTime pattern="MM/dd/yyyy kk:mm"/>
		</h:outputText>
		
		<h:outputText value="#{msgs.mailTypePrompt}"/>
		<h:outputText id="mailtype" value="#{msgrules.ruleLogic.mailType}"/>
		
		<h:outputText value="#{msgs.ruleCategoryPrompt}"/>
		<h:outputText id="rulecategory" value="#{msgrules.ruleLogic.ruleCategoryDesc}"/>
		
		<h:outputText value="#{msgs.ruleTypePrompt}"/>
		<h:outputText id="ruletype" value="#{msgrules.ruleLogic.ruleType}"/>
	</h:panelGrid>
	<h:dataTable value="#{msgrules.subRules}" var="subrule"
			styleClass="jsfDataTable"
            headerClass="dataTableHeader"
            footerClass="dataTableFooter"
            rowClasses="oddRows, evenRows">
        <h:column>
           <f:facet name="header">
              <h:outputText value="#{msgs.deleteColumnHeader}"/>
           </f:facet>
           <h:selectBooleanCheckbox value="#{subrule.markedForDeletion}" 
              onclick="submit()"/>
        </h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.subRuleNameHeader}"/> 
			</f:facet>
			<h:selectOneMenu value="#{subrule.subRuleName}">
				<f:selectItems value="#{dynacodes.subRuleItems}"/>
			</h:selectOneMenu>
        </h:column>
	   <h:column>
	      <f:facet name="header">
	         <h:outputText value="#{msgs.moveUpHeader}"/>
	      </f:facet>
	      <h:commandLink action="#{msgrules.moveUpSubRule}" immediate="true"
	      	rendered="#{msgrules.subRules.rowIndex > 0}">
	      	<h:graphicImage value="/images/greenUp.gif" title="Move Up" style="border: 0"/>
	      </h:commandLink>
	   </h:column>
	   <h:column>
	      <f:facet name="header">
	         <h:outputText value="#{msgs.moveDownHeader}"/>
	      </f:facet>
	      <h:commandLink action="#{msgrules.moveDownSubRule}" immediate="true"
	      	rendered="#{msgrules.subRules.rowIndex < (msgrules.subRules.rowCount-1)}">
	      	<h:graphicImage value="/images/greenDown.gif" title="Move Down" style="border: 0"/>
	      </h:commandLink>
	   </h:column>
    </h:dataTable>
    <f:verbatim><p/></f:verbatim>
    <h:panelGroup>
	    <h:commandButton value="#{msgs.deleteButtonText}" title="Delete selected rows"
	   		action="#{msgrules.deleteSubRules}" 
	   		disabled="#{not msgrules.anySubRulesMarkedForDeletion}"
	   		onclick="javascript:return confirmDelete();"/>
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshFromDB}" title="Refresh from database"
		   action="#{msgrules.refreshSubRules}"/>
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.addNewButtonText}" title="Add a new row"
		   action="#{msgrules.addSubRule}"/>
    </h:panelGroup>
    <f:verbatim><p/></f:verbatim>
	<h:outputText value="#{msgs[msgrules.testResult]}"
		rendered="#{msgrules.testResult != null}" styleClass="errorMessage"
		id="testResult" />
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{msgrules.saveSubRules}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{msgrules.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

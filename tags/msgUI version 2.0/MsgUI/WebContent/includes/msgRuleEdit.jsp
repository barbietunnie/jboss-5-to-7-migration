<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.msgRuleEditLabel}" styleClass="gridHeader">
	   <f:param value="#{msgrules.ruleLogic.ruleName}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader" 
		columnClasses="promptColumn, inputColumn, messageColumn">
		<h:outputText value="#{msgs.ruleNamePrompt}"/>
		<h:inputText id="rulename" value="#{msgrules.ruleLogic.ruleName}"
			required="true" binding="#{msgrules.ruleNameInput}" 
			label="#{msgs.ruleNamePrompt}" maxlength="26" size="30">
			<f:validateLength minimum="1" maximum="26"/>
		</h:inputText>
		<h:message for="rulename" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.ruleSeqPrompt}"/>
		<h:outputText id="ruleseq" value="#{msgrules.ruleLogic.ruleSeq}"/>
		<f:verbatim>&nbsp;</f:verbatim>
		
		<h:outputText value="#{msgs.descriptionPrompt}"/>
		<h:inputText id="desc" value="#{msgrules.ruleLogic.description}"
			required="true" label="#{msgs.descriptionPrompt}" size="80" maxlength="255">
		</h:inputText>
		<h:message for="desc" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.statusIdPrompt}"/>
		<h:selectOneMenu id="statusid" value="#{msgrules.ruleLogic.statusId}"
			required="true" label="#{msgs.statusIdPrompt}">
			<f:selectItems value="#{codes.simpleStatusIdItems}"/>
		</h:selectOneMenu>
		<h:message for="statusid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.startTimePrompt}(MM/dd/yyyy)"/>
		<h:panelGroup>
			<h:inputText id="starttime" value="#{msgrules.ruleLogic.startDate}"
				binding="#{msgrules.startDateInput}" size="10" maxlength="10" 
				required="true" label="#{msgs.startTimePrompt}"
				validator="#{msgrules.checkStartDate}"
				onclick="displayDatePicker(this.name);">
				<f:convertDateTime pattern="MM/dd/yyyy" type="date"/>
			</h:inputText>
			<h:selectOneMenu id="starthour" value="#{msgrules.ruleLogic.startHour}">
				<f:selectItems value="#{codes.hourAmPmItems}"/>
			</h:selectOneMenu>
			<h:selectOneMenu id="startminute" value="#{msgrules.ruleLogic.startMinute}">
				<f:selectItems value="#{codes.minuteItems}"/>
			</h:selectOneMenu>
		</h:panelGroup>
		<h:message for="starttime" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.mailTypePrompt}"/>
		<h:selectOneMenu id="mailtype" value="#{msgrules.ruleLogic.mailType}"
			required="true" label="#{msgs.mailTypePrompt}">
			<f:selectItems value="#{codes.mailTypeItems}"/>
		</h:selectOneMenu>
		<h:message for="mailtype" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.ruleCategoryPrompt}"/>
		<h:selectOneMenu id="rulecategory" value="#{msgrules.ruleLogic.ruleCategory}"
			label="#{msgs.ruleCategoryPrompt}">
			<f:selectItems value="#{codes.ruleCategoryItems}"/>
		</h:selectOneMenu>
		<h:message for="rulecategory" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.subRulePrompt}"/>
		<h:selectOneMenu id="subrule" value="#{msgrules.ruleLogic.isSubRule}"
			required="true" label="#{msgs.subRulePrompt}">
			<f:selectItems value="#{codes.yorNItems}"/>
		</h:selectOneMenu>
		<h:message for="subrule" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.hasSubRulePrompt}"/>
		<h:outputText value="#{msgrules.hasSubRules}"/>
		<f:verbatim>&nbsp;</f:verbatim>
		
		<h:outputText value="#{msgs.ruleTypePrompt}"/>
		<h:selectOneRadio id="ruletype" value="#{msgrules.ruleLogic.ruleType}"
			required="true" label="#{msgs.ruleTypePrompt}" layout="lineDirection">
			<f:selectItems value="#{codes.ruleTypeItems}"/>
		</h:selectOneRadio>
		<h:message for="ruletype" styleClass="errorMessage"/>
	</h:panelGrid>
	<h:dataTable value="#{msgrules.ruleElements}" var="element"
			styleClass="jsfDataTable"
            headerClass="dataTableHeader"
            footerClass="dataTableFooter"
            rowClasses="oddRows, evenRows">
        <h:column>
           <f:facet name="header">
              <h:outputText value=""/>
           </f:facet>
           <h:selectBooleanCheckbox value="#{element.markedForDeletion}" 
              onclick="submit()"/>
        </h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.dataNameColumn}"/> 
			</f:facet>
			<h:selectOneMenu value="#{element.dataName}" onchange="submit()">
				<f:selectItems value="#{codes.ruleDataNameItems}"/>
			</h:selectOneMenu>
        </h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.headerNameColumn}"/> 
			</f:facet>
			<h:inputText value="#{element.headerName}" maxlength="50"
				rendered="#{element.dataName == 'X-Header'}"/>
			<h:outputText value="" rendered="#{element.dataName != 'X-Header'}"/>
        </h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.criteriaColumn}"/> 
			</f:facet>
			<h:selectOneMenu value="#{element.criteria}" onchange="submit()">
				<f:selectItems value="#{codes.ruleCriteriaItems}"/>
			</h:selectOneMenu>
        </h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.targetTextColumn}"/> 
			</f:facet>
			<h:selectOneMenu value="#{element.targetText}"
				rendered="#{element.dataName == 'CarrierCode'}">
				<f:selectItems value="#{codes.mailCarrierCodeItems}"/>
			</h:selectOneMenu>
			<h:selectOneMenu value="#{element.targetText}"
				rendered="#{element.dataName == 'RuleName'}">
				<f:selectItems value="#{dynacodes.builtinRuleNameItems}"/>
			</h:selectOneMenu>
			<h:inputText value="#{element.targetText}" maxlength="2000" title="#{element.targetText}"
				rendered="#{element.dataName != 'CarrierCode' && element.dataName != 'RuleName'
					&& element.criteria != 'reg_ex'}"/>
			<h:inputText value="#{element.targetText}" maxlength="2000" title="#{element.targetText}"
				rendered="#{element.dataName != 'CarrierCode' && element.dataName != 'RuleName'
					&& element.criteria == 'reg_ex'}"
				validator="#{msgrules.validateRegex}"/>
        </h:column>
		<%-- h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.targetProcColumn}"/> 
			</f:facet>
			<h:selectOneMenu value="#{element.targetProc}">
				<f:selectItem itemValue="" itemLabel="Not Selected"/>
				<f:selectItems value="#{codes.targetProcItems}"/>
			</h:selectOneMenu>
        </h:column --%>
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.caseSensitiveColumn}"/> 
			</f:facet>
			<h:selectBooleanCheckbox value="#{element.textCaseSensitive}"/>
        </h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.advancedColumn}"/>
			</f:facet>
			<h:commandLink action="#{msgrules.viewRuleElement}" 
				style="color: darkblue; font-size: 1em; font-weight: bold;">
				<f:param name="rulename" value="#{element.ruleName}"/>
				<f:param name="seq" value="#{element.elementSeq}"/>
				<h:outputText value="#{msgs.editButtonText}" title="Click to Edit Rule Element"/>
			</h:commandLink>
        </h:column>
    </h:dataTable>
    <f:verbatim><p/></f:verbatim>
    <h:panelGroup>
	    <h:commandButton value="#{msgs.deleteButtonText}" title="Delete selected rows"
	   		action="#{msgrules.deleteRuleElements}" 
	   		disabled="#{not msgrules.anyElementsMarkedForDeletion}"
	   		onclick="javascript:return confirmDelete();"/>
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshFromDB}" title="Refresh from database"
		   action="#{msgrules.refreshElements}"/>
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.copyToNewButtonText}" title="Create a new row from selected"
		   action="#{msgrules.copyRuleElement}"
		   disabled="#{not msgrules.anyElementsMarkedForDeletion}"/>
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.addNewButtonText}" title="Add a new row"
		   action="#{msgrules.addRuleElement}"/>
	</h:panelGroup>
    <f:verbatim><p/></f:verbatim>
	<h:outputText value="#{msgs[msgrules.testResult]}" rendered="#{msgrules.testResult != null}"
		styleClass="errorMessage" id="testResult"/>
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit Changes"
				action="#{msgrules.saveRuleLogic}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel Changes"
				immediate="true" action="#{msgrules.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

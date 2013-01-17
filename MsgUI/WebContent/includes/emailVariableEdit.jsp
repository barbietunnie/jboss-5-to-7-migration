<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.emailVariableEditLabel}" styleClass="gridHeader">
	   <f:param value="#{emailvariables.emailVariable.variableName}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader" 
		columnClasses="promptColumn, inputColumn, messageColumn">
		<h:outputText value="#{msgs.variableNamePrompt}"/>
		<h:inputText id="variablename" value="#{emailvariables.emailVariable.variableName}"
			required="true" binding="#{emailvariables.variableNameInput}" 
			label="#{msgs.variableNamePrompt}" maxlength="26" size="26"
			disabled="#{emailvariables.editMode}"
			validator="#{emailvariables.validatePrimaryKey}">
			<f:validateLength minimum="1" maximum="26"/>
		</h:inputText>
		<h:message for="variablename" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.variableTypePrompt}"/>
		<h:selectOneMenu id="varbltype" value="#{emailvariables.emailVariable.variableType}"
			required="true" label="#{msgs.variableTypePrompt}">
			<f:selectItems value="#{codes.emailVariableTypeItems}"/>
		</h:selectOneMenu>
		<h:message for="varbltype" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.tableNamePrompt}"/>
		<h:inputText id="tablename" value="#{emailvariables.emailVariable.tableName}"
			required="false" label="#{msgs.tableNamePrompt}" size="50" maxlength="50">
		</h:inputText>
		<h:message for="tablename" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.columnNamePrompt}"/>
		<h:inputText id="columnname" value="#{emailvariables.emailVariable.columnName}"
			label="#{msgs.columnNamePrompt}" maxlength="50" size="50">
		</h:inputText>
		<h:message for="columnname" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.statusIdPrompt}"/>
		<h:selectOneMenu id="statusid" value="#{emailvariables.emailVariable.statusId}"
			required="true" label="#{msgs.statusIdPrompt}">
			<f:selectItems value="#{codes.mailboxStatusIdItems}"/>
		</h:selectOneMenu>
		<h:message for="statusid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.defaultValuePrompt}"/>
		<h:inputTextarea id="defaultvalue" value="#{emailvariables.emailVariable.defaultValue}"
			label="#{msgs.defaultValuePrompt}" cols="80" rows="3">
			<f:validateLength maximum="255"/>
		</h:inputTextarea>
		<h:message for="defaultvalue" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.variableQueryPrompt}"/>
		<h:inputTextarea id="variablequery" value="#{emailvariables.emailVariable.variableQuery}"
			cols="80" rows="5" label="#{msgs.variableQueryPrompt}">
			<f:validateLength maximum="255"/>
		</h:inputTextarea>
		<h:message for="variablequery" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.variableProcPrompt}"/>
		<h:inputText id="variableproc" value="#{emailvariables.emailVariable.variableProc}"
			label="#{msgs.variableProcPrompt}" maxlength="100" size="80">
		</h:inputText>
		<h:message for="variableproc" styleClass="errorMessage"/>
	</h:panelGrid>
	<h:outputText value="#{msgs[emailvariables.testResult]}"
		rendered="#{emailvariables.testResult != null}" styleClass="errorMessage"
		id="testResult" />
	<h:outputText value="#{emailvariables.actionFailure}"
		rendered="#{emailvariables.actionFailure != null}" styleClass="errorMessage"/>
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.testButtonText}"
				title="Test Variable Query" id="testquery"
				action="#{emailvariables.testEmailVariable}" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{emailvariables.saveEmailVariable}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{emailvariables.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

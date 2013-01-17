<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.msgActionDetailEditLabel}" styleClass="gridHeader">
	   <f:param value="#{actiondetails.actionDetail.actionId}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader" 
		columnClasses="promptColumn, inputColumn, messageColumn">
		<h:outputText value="#{msgs.actionIdPrompt}"/>
		<h:inputText id="actionid" value="#{actiondetails.actionDetail.actionId}"
			required="true" binding="#{actiondetails.actionIdInput}"
			disabled="#{actiondetails.editMode}"
			validator="#{actiondetails.validatePrimaryKey}" 
			label="#{msgs.actionIdPrompt}" maxlength="16" size="16">
			<f:validateLength minimum="1" maximum="16"/>
		</h:inputText>
		<h:message for="actionid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.descriptionPrompt}"/>
		<h:inputText id="description" value="#{actiondetails.actionDetail.description}"
			label="#{msgs.descriptionPrompt}" maxlength="100" size="50">
			<f:validateLength maximum="100"/>
			<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="description" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.processBeanIdPrompt}"/>
		<h:inputText id="beanid" value="#{actiondetails.actionDetail.processBeanId}"
			required="true"
			label="#{msgs.processBeanIdPrompt}" maxlength="50" size="50">
		</h:inputText>
		<h:message for="beanid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.processClassNamePrompt}"/>
		<h:inputText id="classname" value="#{actiondetails.actionDetail.processClassName}"
			label="#{msgs.processClassNamePrompt}" maxlength="100" size="50">
			<f:validateLength maximum="100"/>
			<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="classname" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.dataTypePrompt}"/>
		<h:selectOneMenu id="datatype" value="#{actiondetails.actionDetail.dataType}"
			label="#{msgs.dataTypePrompt}">
			<f:selectItems value="#{dynacodes.msgDataTypeItems}"/>
			<f:converter converterId="NullableStringConverter"/>
		</h:selectOneMenu>
		<h:message for="datatype" styleClass="errorMessage"/>
	</h:panelGrid>
	<h:outputText value="#{msgs[actiondetails.testResult]}"
		rendered="#{actiondetails.testResult != null}"
		styleClass="errorMessage" id="testResult" />
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.testButtonText}"
				title="Test Action Detail Bean"
				action="#{actiondetails.testActionDetail}" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{actiondetails.saveMsgActionDetail}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{actiondetails.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

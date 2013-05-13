<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.emailTemplateEditLabel}" styleClass="gridHeader">
	   <f:param value="#{emailtemplates.emailTemplate.templateId}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader" 
		columnClasses="promptColumn, inputColumn, messageColumn">
		<h:outputText value="#{msgs.templateIdPrompt}"/>
		<h:inputText id="templateid" value="#{emailtemplates.emailTemplate.templateId}"
			required="true" binding="#{emailtemplates.templateIdInput}" 
			label="#{msgs.templateIdPrompt}" maxlength="26" size="26"
			validator="#{emailtemplates.validatePrimaryKey}">
			<f:validateLength minimum="1" maximum="26"/>
		</h:inputText>
		<h:message for="templateid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.defaultListIdPrompt}"/>
		<h:selectOneMenu id="listid" value="#{emailtemplates.emailTemplate.listId}"
			required="true" label="#{msgs.defaultListIdPrompt}">
			<f:selectItems value="#{dynacodes.mailingListIdItems}"/>
		</h:selectOneMenu>
		<h:message for="listid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.subjectPrompt}"/>
		<h:inputText id="subject" value="#{emailtemplates.emailTemplate.subject}"
			required="false" label="#{msgs.subjectPrompt}" size="80" maxlength="255">
		</h:inputText>
		<h:message for="subject" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.listTypePrompt}"/>
		<h:selectOneMenu id="listtype" value="#{emailtemplates.emailTemplate.listType}"
			required="true" label="#{msgs.listTypePrompt}" onchange="submit()"
			valueChangeListener="#{emailtemplates.fieldValueChanged}">
			<f:selectItems value="#{codes.mailingListTypeItems}"/>
		</h:selectOneMenu>
		<h:message for="listtype" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.deliveryOptionPrompt}"/>
		<h:selectOneMenu id="dlvropt" value="#{emailtemplates.emailTemplate.deliveryOption}"
			required="true" label="#{msgs.deliveryOptionPrompt}">
			<f:selectItems value="#{codes.mailingListDeliveryOptionItems}"/>
		</h:selectOneMenu>
		<h:message for="dlvropt" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.selectionCriteriaPrompt}"/>
		<h:inputText id="select" value="#{emailtemplates.emailTemplate.selectCriteria}"
			label="#{msgs.selectionCriteriaPrompt}" maxlength="100" size="50">
			<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="select" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.embedEmailIdPrompt}"/>
		<h:selectOneMenu value="#{emailtemplates.emailTemplate.embedEmailId}"
			id="emailid" label="#{msgs.embedEmailIdPrompt}">
			<f:selectItems value="#{codes.yorNItems}"/>
			<f:selectItem itemLabel="Use System default" itemValue=" "/>
		</h:selectOneMenu>
		<h:message for="emailid" styleClass="errorMessage"/>

	</h:panelGrid>
	
	<h:panelGrid columns="1" styleClass="smtpBody">
		<h:panelGrid columns="2" styleClass="commandBar" columnClasses="alignLeft70,alignRight30">
		<h:panelGroup>
			<h:outputText value="#{msgs.variableNamePrompt}" styleClass="columnHeader"/>
			<h:selectOneMenu id="vname" required="true">
				<f:selectItems value="#{emailtemplates.emailVariables}"/>
			</h:selectOneMenu>
			<f:verbatim>
			<input type="button" value="Insert Selected Variable"
				onclick="insertFieldToBody('emailtmplt:content:vname');"/>
				<%--onclick="insertIntoBody('emailtmplt:content:bodytext', 'emailtmplt:content:vname');"/ --%>
			</f:verbatim>
		</h:panelGroup>
		<h:panelGroup>
			<h:outputText value="#{msgs.htmlContentPrompt}"/>
			<h:selectBooleanCheckbox value="#{emailtemplates.emailTemplate.isHtml}"/>
		</h:panelGroup>
		</h:panelGrid>
		<h:inputTextarea value="#{emailtemplates.emailTemplate.bodyText}"
			id="bodytext" rows="24" style="width: 100%;"/>
		<f:verbatim>
		<script type="text/javascript">
			buttonPath = "images/whizzywigbuttons/";
			makeWhizzyWig("emailtmplt:content:bodytext", "all");
		</script>
		</f:verbatim>
	</h:panelGrid>
	
	<h:outputText value="#{msgs[emailtemplates.testResult]}"
		rendered="#{emailtemplates.testResult != null}" styleClass="errorMessage"
		id="testResult" />
	<h:outputText value="#{emailtemplates.actionFailure}"
		rendered="#{emailtemplates.actionFailure != null}" styleClass="errorMessage"/>
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{emailtemplates.saveEmailTemplate}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{emailtemplates.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

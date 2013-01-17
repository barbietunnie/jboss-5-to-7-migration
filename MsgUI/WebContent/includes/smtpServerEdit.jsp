<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.smtpServerEditLabel}" styleClass="gridHeader">
	   <f:param value="#{smtpsvrs.smtpServer.serverName}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader" 
		columnClasses="promptColumn, inputColumn, messageColumn">
		<h:outputText value="#{msgs.serverNamePrompt}"/>
		<h:inputText id="servername" value="#{smtpsvrs.smtpServer.serverName}"
			required="true" binding="#{smtpsvrs.serverNameInput}" 
			label="#{msgs.serverNamePrompt}" maxlength="50" size="50"
			disabled="#{smtpsvrs.editMode}"
			validator="#{smtpsvrs.validatePrimaryKey}">
			<f:validateLength minimum="1" maximum="50"/>
		</h:inputText>
		<h:message for="servername" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.hostNamePrompt}"/>
		<h:inputText id="smtphost" value="#{smtpsvrs.smtpServer.smtpHost}"
			required="true" label="#{msgs.hostNamePrompt}" maxlength="100"
			size="50">
			<f:validateLength minimum="1" maximum="100" />
		</h:inputText>
		<h:message for="smtphost" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.portNumberPrompt}"/>
		<h:inputText id="port" value="#{smtpsvrs.smtpServer.smtpPort}"
			required="true" label="#{msgs.portNumberPrompt}" size="10">
			<f:validateLongRange minimum="-1" maximum="9999"/>
		</h:inputText>
		<h:message for="port" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.useSslPrompt}"/>
		<h:selectOneMenu id="ssl" value="#{smtpsvrs.smtpServer.useSsl}"
			binding="#{smtpsvrs.useSslInput}"
			required="true" label="#{msgs.useSslPrompt}" onchange="submit()">
			<f:selectItems value="#{codes.yesNoItems}"/>
		</h:selectOneMenu>
		<h:message for="ssl" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.useAuthPrompt}"/>
		<h:selectOneMenu id="auth" value="#{smtpsvrs.smtpServer.useAuth}"
			binding="#{smtpsvrs.useAuthInput}"
			label="#{msgs.useAuthPrompt}" onchange="submit()">
			<f:selectItems value="#{codes.yesNoItems}"/>
		</h:selectOneMenu>
		<h:message for="auth" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.userIdPrompt}"/>
		<h:inputText id="userid" value="#{smtpsvrs.smtpServer.userId}"
			required="#{smtpsvrs.isUseSslInput || smtpsvrs.isUseAuthInput}"
			label="#{msgs.userIdPrompt}" maxlength="30" size="30">
		</h:inputText>
		<h:message for="userid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.passwordPrompt}"/>
		<h:inputText id="password" value="#{smtpsvrs.smtpServer.userPswd}"
			required="#{smtpsvrs.isUseSslInput || smtpsvrs.isUseAuthInput}"
			label="#{msgs.passwordPrompt}" maxlength="30" size="30">
		</h:inputText>
		<h:message for="password" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.descriptionPrompt}"/>
		<h:inputText id="desc" value="#{smtpsvrs.smtpServer.description}"
			label="#{msgs.descriptionPrompt}" maxlength="100" size="50">
		</h:inputText>
		<h:message for="desc" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.persistencePrompt}"/>
		<h:selectOneMenu id="persistence" value="#{smtpsvrs.smtpServer.persistence}"
			required="true" label="#{msgs.persistencePrompt}">
			<f:selectItems value="#{codes.yesNoItems}"/>
		</h:selectOneMenu>
		<h:message for="persistence" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.statusIdPrompt}"/>
		<h:selectOneMenu id="statusid" value="#{smtpsvrs.smtpServer.statusId}"
			required="true" label="#{msgs.statusIdPrompt}">
			<f:selectItems value="#{codes.mailboxStatusIdItems}"/>
		</h:selectOneMenu>
		<h:message for="statusid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.serverTypePrompt}"/>
		<h:selectOneMenu id="servertype" value="#{smtpsvrs.smtpServer.serverType}"
			label="#{msgs.serverTypePrompt}">
			<f:selectItems value="#{codes.smtpServerTypeItems}"/>
		</h:selectOneMenu>
		<h:message for="servertype" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.retriesPrompt}"/>
		<h:inputText id="retries" value="#{smtpsvrs.smtpServer.retries}"
			required="true" label="#{msgs.retriesPrompt}" size="10">
			<f:validateLongRange minimum="-1" maximum="500"/>
		</h:inputText>
		<h:message for="retries" styleClass="errorMessage"/>
		
		<%-- h:outputText value="#{msgs.retryFreqPrompt}"/>
		<h:inputText id="retryfreq" value="#{smtpsvrs.smtpServer.retryFreq}"
			required="true" label="#{msgs.retryFreqPrompt}" size="10">
			<f:validateLongRange minimum="-1" maximum="500"/>
		</h:inputText>
		<h:message for="retryfreq" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.messageCountPrompt}"/>
		<h:inputText id="messagecount" value="#{smtpsvrs.smtpServer.messageCount}"
			required="true" label="#{msgs.messageCountPrompt}" size="10">
			<f:validateLongRange minimum="-1"/>
		</h:inputText>
		<h:message for="messagecount" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.alertAfterPrompt}" escape="false"/>
		<h:inputText id="alertafter" value="#{smtpsvrs.smtpServer.alertAfter}"
			label="#{msgs.alertAfterPrompt}" size="10">
		</h:inputText>
		<h:message for="alertafter" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.alertLevelPrompt}"/>
		<h:selectOneMenu id="alertlevel" value="#{smtpsvrs.smtpServer.alertLevel}"
			label="#{msgs.alertLevelPrompt}">
			<f:selectItems value="#{codes.alertLevelItems}"/>
		</h:selectOneMenu>
		<h:message for="alertlevel" styleClass="errorMessage"/ --%>
		
		<h:outputText value="#{msgs.threadsPrompt}"/>
		<h:inputText id="threads" value="#{smtpsvrs.smtpServer.threads}"
			required="true" label="#{msgs.threadsPrompt}" size="10">
			<f:validateLongRange minimum="1" maximum="20"/>
		</h:inputText>
		<h:message for="threads" styleClass="errorMessage"/>
	</h:panelGrid>
	<h:outputText value="#{msgs[smtpsvrs.testResult]}"
		rendered="#{smtpsvrs.testResult != null}" styleClass="errorMessage"
		id="testResult" />
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.testButtonText}"
				title="Test SMTP server configuration" id="testsmtp"
				action="#{smtpsvrs.testSmtpServer}" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{smtpsvrs.saveSmtpServer}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{smtpsvrs.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

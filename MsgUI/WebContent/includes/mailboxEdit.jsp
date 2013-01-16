<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.mailboxEditLabel}" styleClass="gridHeader">
	   <f:param value="#{mailboxes.mailbox.userId}@#{mailboxes.mailbox.hostName}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader"
		columnClasses="promptColumn, inputColumn, messageColumn">
		<h:outputText value="#{msgs.hostNamePrompt}"/>
		<h:inputText id="hostname" value="#{mailboxes.mailbox.hostName}"
			required="true" label="#{msgs.hostNamePrompt}" maxlength="100" size="50"
			disabled="#{mailboxes.editMode}">
			<f:validateLength minimum="1" maximum="100"/>
		</h:inputText>
		<h:message for="hostname" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.portNumberPrompt}"/>
		<h:inputText id="port" value="#{mailboxes.mailbox.portNumber}"
			required="true" label="#{msgs.portNumberPrompt}" size="10">
			<f:validateLongRange minimum="-1" maximum="9999"/>
		</h:inputText>
		<h:message for="port" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.protocolPrompt}"/>
		<h:selectOneMenu id="protocol" value="#{mailboxes.mailbox.protocol}"
			required="true" label="#{msgs.protocolPrompt}">
			<f:selectItems value="#{codes.mailProtocolItems}"/>
		</h:selectOneMenu>
		<h:message for="protocol" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.userIdPrompt}"/>
		<h:inputText id="userid" value="#{mailboxes.mailbox.userId}"
			required="true" label="#{msgs.userIdPrompt}" maxlength="30" size="30"
			binding="#{mailboxes.userIdInput}"
			validator="#{mailboxes.validatePrimaryKey}">
			<f:validateLength minimum="1" maximum="30"/>
		</h:inputText>
		<h:message for="userid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.passwordPrompt}"/>
		<h:inputText id="password" value="#{mailboxes.mailbox.userPswd}"
			label="#{msgs.passwordPrompt}" maxlength="30" size="30">
			<f:validateLength maximum="30"/>
		</h:inputText>
		<h:message for="password" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.descriptionPrompt}"/>
		<h:inputText id="desc" value="#{mailboxes.mailbox.mailBoxDesc}"
			label="#{msgs.descriptionPrompt}" maxlength="50" size="50">
		</h:inputText>
		<h:message for="desc" styleClass="errorMessage"/>
		
		<%-- h:outputText value="#{msgs.carrierCodePrompt}"/>
		<h:selectOneMenu id="carriercode" value="#{mailboxes.mailbox.carrierCode}"
			required="true" label="#{msgs.carrierCodePrompt}">
			<f:selectItems value="#{codes.mailCarrierCodeItems}"/>
		</h:selectOneMenu>
		<h:message for="carriercode" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.internalOnlyPrompt}"/>
		<h:selectOneMenu id="internal" value="#{mailboxes.mailbox.internalOnly}"
			label="#{msgs.internalOnlyPrompt}">
			<f:selectItems value="#{codes.yesNoItems}"/>
		</h:selectOneMenu>
		<h:message for="internal" styleClass="errorMessage"/ --%>
		
		<h:outputText value="#{msgs.statusIdPrompt}"/>
		<h:selectOneMenu id="statusid" value="#{mailboxes.mailbox.statusId}"
			required="true" label="#{msgs.statusIdPrompt}">
			<f:selectItems value="#{codes.mailboxStatusIdItems}"/>
		</h:selectOneMenu>
		<h:message for="statusid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.serverTypePrompt}"/>
		<h:selectOneMenu id="servertype" value="#{mailboxes.mailbox.serverType}"
			label="#{msgs.serverTypePrompt}">
			<f:selectItems value="#{codes.smtpServerTypeItems}"/>
		</h:selectOneMenu>
		<h:message for="servertype" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.useSslPrompt}"/>
		<h:selectOneMenu id="ssl" value="#{mailboxes.mailbox.useSsl}"
			required="true" label="#{msgs.useSslPrompt}">
			<f:selectItems value="#{codes.yesNoItems}"/>
		</h:selectOneMenu>
		<h:message for="ssl" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.readPerPassPrompt}"/>
		<h:inputText id="readperpass" value="#{mailboxes.mailbox.readPerPass}"
			required="true" label="#{msgs.readPerPassPrompt}" size="10">
			<f:validateLongRange minimum="1" maximum="200"/>
		</h:inputText>
		<h:message for="readperpass" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.retryMaxPrompt}"/>
		<h:inputText id="retrymax" value="#{mailboxes.mailbox.retryMax}"
			label="#{msgs.retryMaxPrompt}" size="10">
			<f:validateLongRange maximum="500"/>
		</h:inputText>
		<h:message for="retrymax" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.minimumWaitPrompt}"/>
		<h:inputText id="minimumwait" value="#{mailboxes.mailbox.minimumWait}"
			label="#{msgs.minimumWaitPrompt}" size="10">
			<f:validateLongRange maximum="300"/>
		</h:inputText>
		<h:message for="minimumwait" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.toPlainTextPrompt}"/>
		<h:selectOneMenu id="toplaintext" value="#{mailboxes.mailbox.toPlainText}"
			label="#{msgs.toPlainTextPrompt}">
			<f:selectItems value="#{codes.yesNoItems}"/>
		</h:selectOneMenu>
		<h:message for="toplaintext" styleClass="errorMessage"/>
		
		<%-- h:outputText value="#{msgs.toAddrDomainPrompt}"/>
		<h:inputTextarea id="toaddrdomain" value="#{mailboxes.mailbox.toAddrDomain}"
			label="#{msgs.toAddrDomainPrompt}" rows="2" cols="40">
			<f:validateLength maximum="500"/>
		</h:inputTextarea>
		<h:message for="toaddrdomain" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.checkDuplicatePrompt}"/>
		<h:selectOneMenu id="checkduplicate" value="#{mailboxes.mailbox.checkDuplicate}"
			label="#{msgs.checkDuplicatePrompt}">
			<f:selectItems value="#{codes.yesNoItems}"/>
		</h:selectOneMenu>
		<h:message for="checkduplicate" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.alertDuplicatePrompt}" escape="false"/>
		<h:selectOneMenu id="alertduplicate" value="#{mailboxes.mailbox.alertDuplicate}"
			label="#{msgs.alertDuplicatePrompt}">
			<f:selectItems value="#{codes.yesNoItems}"/>
		</h:selectOneMenu>
		<h:message for="alertduplicate" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.logDuplicatePrompt}"/>
		<h:selectOneMenu id="logduplicate" value="#{mailboxes.mailbox.logDuplicate}"
			label="#{msgs.logDuplicatePrompt}">
			<f:selectItems value="#{codes.yesNoItems}"/>
		</h:selectOneMenu>
		<h:message for="logduplicate" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.purgeDupsAfterPrompt}"/>
		<h:inputText id="purgedupsAfter" value="#{mailboxes.mailbox.purgeDupsAfter}"
			label="#{msgs.purgeDupsAfterPrompt}" size="10">
			<f:validateLongRange maximum="720"/>
		</h:inputText>
		<h:message for="purgedupsAfter" styleClass="errorMessage"/ --%>
		
		<h:outputText value="#{msgs.threadsPrompt}"/>
		<h:inputText id="threads" value="#{mailboxes.mailbox.threads}"
			required="true" label="#{msgs.threadsPrompt}" size="10">
			<f:validateLongRange minimum="1" maximum="20"/>
		</h:inputText>
		<h:message for="threads" styleClass="errorMessage"/>
	</h:panelGrid>
	<h:outputText value="#{msgs[mailboxes.testResult]}"
		rendered="#{mailboxes.testResult != null}" styleClass="errorMessage"
		id="testResult" />
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.testButtonText}"
				title="Test mailbox configuration" action="#{mailboxes.testMailbox}"
				id="testmbox" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{mailboxes.saveMailbox}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{mailboxes.cancelEdit}" />
			<%-- h:commandButton type="reset" value="#{msgs.cancelButtonText}"
				onclick="javascript:history.back()"/ --%>
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

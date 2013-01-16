<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.userAccountEditLabel}" styleClass="gridHeader">
	   <f:param value="#{useraccounts.user.userId}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader" 
		columnClasses="promptColumn, inputColumn, messageColumn">
		<h:outputText value="#{msgs.userIdPrompt}"/>
		<h:inputText id="userid" value="#{useraccounts.user.userId}"
			required="true" binding="#{useraccounts.userIdInput}" 
			label="#{msgs.userIdPrompt}" maxlength="10" size="15"
			disabled="#{useraccounts.editMode}"
			validator="#{useraccounts.validatePrimaryKey}">
			<f:validateLength minimum="1" maximum="10"/>
		</h:inputText>
		<h:message for="userid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.firstNamePrompt}"/>
		<h:inputText id="firstname" value="#{useraccounts.user.firstName}"
			required="true" label="#{msgs.firstNamePrompt}" maxlength="32" size="30">
			<f:validateLength minimum="1" maximum="32"/>
		</h:inputText>
		<h:message for="firstname" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.lastNamePrompt}"/>
		<h:inputText id="lastname" value="#{useraccounts.user.lastName}"
			required="true" label="#{msgs.lastNamePrompt}" maxlength="32" size="30">
			<f:validateLength minimum="1" maximum="32"/>
		</h:inputText>
		<h:message for="lastname" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.middleInitPrompt}"/>
		<h:inputText id="middleinit" value="#{useraccounts.user.middleInit}"
			label="#{msgs.middleInitPrompt}" maxlength="1" size="2">
		</h:inputText>
		<h:message for="middleinit" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.passwordPrompt}"/>
		<h:inputText id="password" value="#{useraccounts.user.password}"
			required="true" label="#{msgs.passwordPrompt}" size="30" maxlength="30">
			<f:validateLength minimum="4" maximum="30"/>
		</h:inputText>
		<h:message for="password" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.statusIdPrompt}"/>
		<h:selectOneMenu id="statusid" value="#{useraccounts.user.statusId}"
			required="true" label="#{msgs.statusIdPrompt}">
			<f:selectItems value="#{codes.mailboxStatusIdItems}"/>
		</h:selectOneMenu>
		<h:message for="statusid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.emailAddrPrompt}"/>
		<h:inputText id="emailaddr" value="#{useraccounts.user.emailAddr}"
			label="#{msgs.emailAddrPrompt}" maxlength="255" size="50">
			<f:validator validatorId="msgui.EmailAddressValidator"/>
		</h:inputText>
		<h:message for="emailaddr" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.rolePrompt}"/>
		<h:selectOneMenu id="role" value="#{useraccounts.user.role}"
			label="#{msgs.rolePrompt}">
			<f:selectItems value="#{codes.roleItems}"/>
		</h:selectOneMenu>
		<h:message for="role" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.defaultFolderPrompt}"/>
		<h:selectOneMenu id="folder" value="#{useraccounts.user.defaultFolder}"
			label="#{msgs.defaultFolderPrompt}">
			<f:selectItems value="#{codes.folderTypeItems}"/>
		</h:selectOneMenu>
		<h:message for="folder" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.defaultRuleNamePrompt}"/>
		<h:selectOneMenu id="rulename" value="#{useraccounts.user.defaultRuleName}"
			label="#{msgs.defaultRuleNamePrompt}">
			<f:selectItem itemValue="All" itemLabel="All Rule Names"/>
			<f:selectItems value="#{dynacodes.builtinRuleNameItems}"/>
			<f:selectItems value="#{dynacodes.customRuleNameItems}"/>
		</h:selectOneMenu>
		<h:message for="rulename" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.defaultToAddrPrompt}"/>
		<h:inputText id="toaddr" value="#{useraccounts.user.defaultToAddr}"
			label="#{msgs.defaultToAddrPrompt}" maxlength="255" size="50">
			<f:validator validatorId="msgui.EmailAddressValidator"/>
		</h:inputText>
		<h:message for="toaddr" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.defaultClientIdPrompt}"/>
		<h:selectOneMenu id="clientid" value="#{useraccounts.user.clientId}"
			label="#{msgs.defaultClientIdPrompt}">
			<f:selectItems value="#{dynacodes.clientIdItems}"/>
		</h:selectOneMenu>
		<h:message for="clientid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.hitsPrompt}"/>
		<h:inputText id="hits" value="#{useraccounts.user.hits}" readonly="true">
		</h:inputText>
		<h:message for="hits" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.lastVisitTimePrompt}"/>
		<h:inputText id="lastvisit" value="#{useraccounts.user.lastVisitTime}"
			readonly="true">
			<f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/>
		</h:inputText>
		<h:message for="lastvisit" styleClass="errorMessage"/>
	</h:panelGrid>
	<h:outputText value="#{msgs[useraccounts.testResult]}"
		rendered="#{useraccounts.testResult != null}" styleClass="errorMessage"
		id="testResult" />
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{useraccounts.saveUser}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{useraccounts.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.mailingListEditLabel}" styleClass="gridHeader">
	   <f:param value="#{maillsts.mailingList.listId}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader" 
		columnClasses="promptColumn, inputColumn, messageColumn">
		<h:outputText value="#{msgs.listIdPrompt}"/>
		<h:inputText id="listid" value="#{maillsts.mailingList.listId}"
			required="true" binding="#{maillsts.listIdInput}" 
			label="#{msgs.listIdPrompt}" maxlength="8" size="10"
			disabled="#{maillsts.mailingList.isBuiltInList}"
			validator="#{maillsts.validatePrimaryKey}">
			<f:validateLength minimum="1" maximum="8"/>
		</h:inputText>
		<h:message for="listid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.displayNamePrompt}"/>
		<h:inputText id="dispname" value="#{maillsts.mailingList.displayName}"
			required="false" label="#{msgs.displayNamePrompt}" maxlength="50"
			size="50">
		</h:inputText>
		<h:message for="dispname" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.accountUserNamePrompt}"/>
		<h:inputText id="acctuser" value="#{maillsts.mailingList.acctUserName}"
			required="true" label="#{msgs.accountUserNamePrompt}" size="50" maxlength="100"
			validator="#{maillsts.validateAccountUserName}">
			<f:validateLength minimum="1" maximum="100"/>
		</h:inputText>
		<h:message for="acctuser" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.descriptionPrompt}"/>
		<h:inputText id="desc" value="#{maillsts.mailingList.description}"
			label="#{msgs.descriptionPrompt}" maxlength="255" size="50">
		</h:inputText>
		<h:message for="desc" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.statusIdPrompt}"/>
		<h:selectOneMenu id="statusid" value="#{maillsts.mailingList.statusId}"
			required="true" label="#{msgs.statusIdPrompt}">
			<f:selectItems value="#{codes.mailboxStatusIdItems}"/>
		</h:selectOneMenu>
		<h:message for="statusid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.clientIdPrompt}"/>
		<h:selectOneMenu id="clientid" value="#{maillsts.mailingList.clientId}"
			required="true" label="#{msgs.clientIdPrompt}">
			<f:selectItems value="#{dynacodes.clientIdItems}"/>
		</h:selectOneMenu>
		<h:message for="clientid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.listMasterEmailPrompt}"/>
		<h:inputText id="mstraddr" value="#{maillsts.mailingList.listMasterEmailAddr}"
			label="#{msgs.listMasterEmailPrompt}" size="50" maxlength="255"
			validator="#{maillsts.validateEmailAddress}">
			<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="mstraddr" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.createTimePrompt}"/>
		<h:outputText id="createtime" value="#{maillsts.mailingList.createTime}">
		</h:outputText>
		<h:message for="createtime" styleClass="errorMessage"/>
	</h:panelGrid>
	<h:outputText value="#{msgs[maillsts.testResult]}"
		rendered="#{maillsts.testResult != null}" styleClass="errorMessage"
		id="testResult" />
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{maillsts.saveMailingList}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{maillsts.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
	<f:verbatim><p/></f:verbatim>
	<%  // always disable the next panel regardless the value of "editMode"
		boolean renderNextPanel = false; %>
	<h:panelGrid columns="1" styleClass="editPaneHeader"
		rendered="#{maillsts.editMode && renderNextPanel}">
		<f:verbatim>&nbsp;</f:verbatim>
		<h:outputText value="#{msgs.uploadEmailAddrsToList}" 
			style="color: blue; font-size: 1.4em;"/>
		<f:verbatim>&nbsp;<p/></f:verbatim>
		<h:outputText value="#{msgs.uploadFileToListLabel}"
			style="color: black; font-size: 1.1em;"/>
		<f:verbatim>&nbsp;<p/></f:verbatim>
		<h:commandButton value="#{msgs.uploadFileButtonText}"
			title="Upload files with Email Address list"
			action="#{maillsts.uploadFiles}"/>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGrid>
</h:panelGrid>

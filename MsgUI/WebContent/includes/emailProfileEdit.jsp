<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.siteProfileEditLabel}" styleClass="gridHeader">
	   <f:param value="#{profileBean.client.clientId}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader" 
		columnClasses="promptColumn, inputColumn, messageColumn">
		
		<h:panelGroup>
		<h:outputText value="#{msgs.siteIdPrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{profileBean.siteMeta.clientId != null}"/>
		</h:panelGroup>
		<h:inputText id="clientid" value="#{profileBean.client.clientId}"
			required="true" label="#{msgs.siteIdPrompt}" 
			binding="#{profileBean.clientIdInput}" 
			validator="#{profileBean.validatePrimaryKey}"
			readonly="#{profileBean.client.isSystemClient}"
			maxlength="16" size="16">
			<f:validateLength minimum="1" maximum="16"/>
		</h:inputText>
		<h:message for="clientid" styleClass="errorMessage"/>
		
		<h:panelGroup>
		<h:outputText value="#{msgs.siteNamePrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{profileBean.siteMeta.clientName != null}"/>
		</h:panelGroup>
		<h:inputText id="sitename" value="#{profileBean.client.clientName}"
			required="true" label="#{msgs.siteNamePrompt}" 
			maxlength="40" size="40">
			<f:validateLength minimum="1" maximum="40"/>
		</h:inputText>
		<h:message for="sitename" styleClass="errorMessage"/>
		
		<h:panelGroup>
		<h:outputText value="#{msgs.domainAddressPrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{profileBean.siteMeta.domainName != null}"/>
		</h:panelGroup>
		<h:inputText id="domain" value="#{profileBean.client.domainName}"
			required="true" label="#{msgs.domainAddressPrompt}" 
			maxlength="100" size="50">
			<f:validateLength minimum="1" maximum="100"/>
		</h:inputText>
		<h:message for="domain" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.webSiteUrlPrompt}"/>
		<h:inputText id="website" value="#{profileBean.client.webSiteUrl}"
			required="false"
			label="#{msgs.webSiteUrlPrompt}" maxlength="255" size="50">
	        	<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="website" styleClass="errorMessage"/>

		<h:panelGroup>
		<h:outputText value="#{msgs.siteContactEmailPrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{profileBean.siteMeta.contactEmail != null}"/>
		</h:panelGroup>
		<h:inputText id="contact" value="#{profileBean.client.contactEmail}"
			required="true" label="#{msgs.siteContactEmailPrompt}" 
			maxlength="255" size="50"
			validator="#{profileBean.validateEmailAddress}">
			<f:validateLength minimum="1" maximum="255"/>
		</h:inputText>
		<h:message for="contact" styleClass="errorMessage"/>
		
		<h:panelGroup>
		<h:outputText value="#{msgs.returnPathLeftPrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{profileBean.siteMeta.returnPathLeft != null}"/>
		</h:panelGroup>
		<h:inputText id="returnpath" value="#{profileBean.client.returnPathLeft}"
			binding="#{profileBean.returnPathLeftInput}"
			required="true" label="#{msgs.returnPathLeftPrompt}" 
			maxlength="50" size="30"
			validator="#{profileBean.validateEmailLocalPart}">
			<f:validateLength minimum="1" maximum="50"/>
		</h:inputText>
		<h:message for="returnpath" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.embedEmailIdPrompt}"/>
		<h:selectOneMenu value="#{profileBean.client.embedEmailId}"
			id="emailid" label="#{msgs.embedEmailIdPrompt}">
			<f:selectItems value="#{codes.yesNoItems}"/>
		</h:selectOneMenu>
		<h:message for="emailid" styleClass="errorMessage"/>

		<h:panelGroup>
		<h:outputText value="#{msgs.securityEmailPrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{profileBean.siteMeta.securityEmail != null}"/>
		</h:panelGroup>
		<h:inputText id="security" value="#{profileBean.client.securityEmail}"
			required="true" label="#{msgs.securityEmailPrompt}" 
			maxlength="255" size="50"
			validator="#{profileBean.validateEmailAddress}">
			<f:validateLength minimum="1" maximum="255"/>
		</h:inputText>
		<h:message for="security" styleClass="errorMessage"/>
		
		<h:panelGroup>
		<h:outputText value="#{msgs.custcareEmailPrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{profileBean.siteMeta.custcareEmail != null}"/>
		</h:panelGroup>
		<h:inputText id="custcare" value="#{profileBean.client.custcareEmail}"
			required="true" label="#{msgs.custcareEmailPrompt}" 
			maxlength="255" size="50"
			validator="#{profileBean.validateEmailAddress}">
			<f:validateLength minimum="1" maximum="255"/>
		</h:inputText>
		<h:message for="custcare" styleClass="errorMessage"/>
		
		<h:panelGroup>
		<h:outputText value="#{msgs.rmaControlEmailPrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{profileBean.siteMeta.rmaDeptEmail != null}"/>
		</h:panelGroup>
		<h:inputText id="rmadept" value="#{profileBean.client.rmaDeptEmail}"
			required="true" label="#{msgs.rmaControlEmailPrompt}" 
			maxlength="255" size="50"
			validator="#{profileBean.validateEmailAddress}">
			<f:validateLength minimum="1" maximum="255"/>
		</h:inputText>
		<h:message for="rmadept" styleClass="errorMessage"/>
		
		<h:panelGroup>
		<h:outputText value="#{msgs.spamControlEmailPrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{profileBean.siteMeta.spamCntrlEmail != null}"/>
		</h:panelGroup>
		<h:inputText id="spamctrl" value="#{profileBean.client.spamCntrlEmail}"
			required="true" label="#{msgs.spamControlEmailPrompt}" 
			maxlength="255" size="50"
			validator="#{profileBean.validateEmailAddress}">
			<f:validateLength minimum="1" maximum="255"/>
		</h:inputText>
		<h:message for="spamctrl" styleClass="errorMessage"/>
		
		<h:panelGroup>
		<h:outputText value="#{msgs.challengeRespEmailPrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{profileBean.siteMeta.chaRspHndlrEmail != null}"/>
		</h:panelGroup>
		<h:inputText id="challenge" value="#{profileBean.client.chaRspHndlrEmail}"
			required="true" label="#{msgs.challengeRespEmailPrompt}" 
			maxlength="255" size="50"
			validator="#{profileBean.validateEmailAddress}">
			<f:validateLength minimum="1" maximum="255"/>
		</h:inputText>
		<h:message for="challenge" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.useTestAddressPrompt}"/>
		<h:selectOneMenu id="usetest" value="#{profileBean.client.useTestAddr}"
			binding="#{profileBean.useTestAddrInput}"
			required="true" label="#{msgs.useTestAddressPrompt}" onchange="submit()">
			<f:selectItems value="#{codes.yesNoItems}"/>
		</h:selectOneMenu>
		<h:message for="usetest" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.testFromAddressPrompt}"/>
		<h:inputText id="testfrom" value="#{profileBean.client.testFromAddr}"
			required="false"
			label="#{msgs.testFromAddressPrompt}" maxlength="255" size="50"
			validator="#{profileBean.validateEmailAddress}">
	        	<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="testfrom" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.testToAddressPrompt}"/>
		<h:inputText id="testto" value="#{profileBean.client.testToAddr}"
			required="#{profileBean.isUseTestAddrInput}"
			label="#{msgs.testToAddressPrompt}" maxlength="255" size="50"
			validator="#{profileBean.validateEmailAddress}">
	        	<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="testto" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.testReplytoAddressPrompt}"/>
		<h:inputText id="replyto" value="#{profileBean.client.testReplytoAddr}"
			required="false"
			label="#{msgs.testReplytoAddressPrompt}" maxlength="255" size="50"
			validator="#{profileBean.validateEmailAddress}">
	        	<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="replyto" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.isVerpEnabledPrompt}"/>
		<h:selectOneMenu id="useverp" value="#{profileBean.client.isVerpEnabled}"
			binding="#{profileBean.verpEnabledInput}"
			required="true" label="#{msgs.isVerpEnabledPrompt}" onchange="submit()">
			<f:selectItems value="#{codes.yesNoItems}"/>
		</h:selectOneMenu>
		<h:message for="useverp" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.verpSubDomainPrompt}"/>
		<h:inputText id="verpsub" value="#{profileBean.client.verpSubDomain}"
			required="false"
			label="#{msgs.verpSubDomainPrompt}" maxlength="50" size="50">
	        	<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="verpsub" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.verpInboxNamePrompt}"/>
		<h:inputText id="verpinbox" value="#{profileBean.client.verpInboxName}"
			required="#{profileBean.isVerpEnabledInput}"
			label="#{msgs.verpInboxNamePrompt}" maxlength="50" size="50">
	        	<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="verpinbox" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.verpRemoveInboxPrompt}"/>
		<h:inputText id="verpremove" value="#{profileBean.client.verpRemoveInbox}"
			required="#{profileBean.isVerpEnabledInput}"
			label="#{msgs.verpRemoveInboxPrompt}" maxlength="50" size="50">
	        	<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="verpremove" styleClass="errorMessage"/>
	</h:panelGrid>

	<h:outputText value="#{msgs[profileBean.testResult]}"
		rendered="#{profileBean.testResult != null}" styleClass="errorMessage"
		id="testResult" />
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{profileBean.saveClient}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.refreshLinkText}" 
				action="#{profileBean.refreshClient}"
				immediate="true" title="Refresh" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{profileBean.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

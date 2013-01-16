<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.customerEditLabel}" styleClass="gridHeader">
	   <f:param value="#{customers.customer.custId}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader" 
		columnClasses="promptColumn, inputColumn, messageColumn">
		
		<h:panelGroup>
		<h:outputText value="#{msgs.customerIdPrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{customers.custMeta.custId != null}"/>
		</h:panelGroup>
		<h:inputText id="custid" value="#{customers.customer.custId}"
			required="true" label="#{msgs.customerIdPrompt}" 
			binding="#{customers.custIdInput}" 
			validator="#{customers.validatePrimaryKey}"
			maxlength="16" size="16">
			<f:validateLength minimum="1" maximum="16"/>
		</h:inputText>
		<h:message for="custid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.siteIdPrompt}"/>
		<h:selectOneMenu value="#{customers.customer.clientId}"
			id="clientid" label="#{msgs.siteIdPrompt}">
			<f:selectItems value="#{dynacodes.clientIdItems}"/>
		</h:selectOneMenu>
		<h:message for="clientid" styleClass="errorMessage"/>

		<h:panelGroup>
		<h:outputText value="#{msgs.firstNamePrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{customers.custMeta.firstName != null}"/>
		</h:panelGroup>
		<h:inputText id="firstnm" value="#{customers.customer.firstName}"
			label="#{msgs.firstNamePrompt}"	maxlength="32" size="32">
			<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="firstnm" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.middleNamePrompt}"/>
		<h:inputText id="middlenm" value="#{customers.customer.middleName}"
			label="#{msgs.middleNamePrompt}" maxlength="32" size="32">
			<f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="middlenm" styleClass="errorMessage"/>
		
		<h:panelGroup>
		<h:outputText value="#{msgs.lastNamePrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{customers.custMeta.lastName != null}"/>
		</h:panelGroup>
		<h:inputText id="lastnm" value="#{customers.customer.lastName}"
			required="true" label="#{msgs.lastNamePrompt}" 
			maxlength="32" size="32">
			<f:validateLength minimum="1" maximum="32"/>
		</h:inputText>
		<h:message for="lastnm" styleClass="errorMessage"/>
		
		<h:panelGroup>
		<h:outputText value="#{msgs.emailAddrPrompt}"/>
		<h:outputText value="*" style="color: red;" rendered="#{customers.custMeta.emailAddr != null}"/>
		</h:panelGroup>
		<h:panelGroup>
		<div class="cellHeight">
		<h:inputText id="emailaddr" value="#{customers.customer.emailAddr}"
			binding="#{customers.emailAddrInput}"
			required="true" validator="#{customers.validateEmailAddress}"
			label="#{msgs.emailAddrPrompt}" maxlength="255" size="50">
        	<f:validateLength minimum="1" maximum="255"/>
		</h:inputText>
		</div>
		</h:panelGroup>
		<h:message for="emailaddr" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.ssnNumberPrompt}"/>
		<h:inputText id="ssnnbr" value="#{customers.customer.ssnNumber}"
			label="#{msgs.ssnNumberPrompt}"	maxlength="15" size="11"
			binding="#{customers.ssnNumberInput}"
			validator="#{customers.validateSsnNumber}">
		</h:inputText>
		<h:message for="ssnnbr" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.birthDatePrompt}"/>
		<h:inputText id="birthdt" value="#{customers.customer.birthDate}"
			label="#{msgs.birthDatePrompt}"	maxlength="15" size="11"
			onclick="displayDatePicker(this.name, false, 'ymd', '-');"
			binding="#{customers.birthDateInput}"
			validator="#{customers.validateDate}">
			<f:convertDateTime pattern="yyyy-MM-dd" type="date"/>
		</h:inputText>
		<h:message for="birthdt" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.streetAddressPrompt}"/>
		<h:inputText id="staddr" value="#{customers.customer.streetAddress}"
			label="#{msgs.streetAddressPrompt}" maxlength="60" size="40">
		</h:inputText>
		<h:message for="staddr" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.streetAddress2Prompt}"/>
		<h:inputText id="staddr2" value="#{customers.customer.streetAddress2}"
			label="#{msgs.streetAddress2Prompt}" maxlength="40" size="40">
		</h:inputText>
		<h:message for="staddr2" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.cityNamePrompt}"/>
		<h:inputText id="citynm" value="#{customers.customer.cityName}"
			label="#{msgs.cityNamePrompt}" maxlength="32" size="32">
		</h:inputText>
		<h:message for="citynm" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.stateCodePrompt}"/>
		<h:selectOneMenu id="statecd" value="#{customers.customer.stateCode}"
			label="#{msgs.stateCodePrompt}">
			<f:selectItems value="#{codes.stateCodeWithAbbrItems}"/>
		</h:selectOneMenu>
		<h:message for="statecd" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.zipCodePrompt}"/>
		<h:panelGroup>
			<h:inputText id="zipcode5" value="#{customers.customer.zipCode5}"
				label="#{msgs.zipCodePrompt}" maxlength="5" size="5"
				validator="#{customers.validateZipCode5}">
			</h:inputText>
			<h:inputText id="zipcode4" value="#{customers.customer.zipCode4}"
				label="#{msgs.zipCodePrompt}" maxlength="4" size="4"
				validator="#{customers.validateZipCode4}">
			</h:inputText>
		</h:panelGroup>
		<h:panelGroup>
			<h:message for="zipcode5" styleClass="errorMessage"/>
			<h:message for="zipcode4" styleClass="errorMessage"/>
		</h:panelGroup>

		<h:outputText value="#{msgs.countryPrompt}"/>
		<h:inputText id="country" value="#{customers.customer.country}"
			label="#{msgs.countryPrompt}" maxlength="50" size="30">
		</h:inputText>
		<h:message for="country" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.dayTimePhonePrompt}"/>
		<h:inputText id="dayphone" value="#{customers.customer.dayPhone}"
			label="#{msgs.dayTimePhonePrompt}" maxlength="18" size="18"
			binding="#{customers.dayPhoneInput}"
			validator="#{customers.validatePhoneNumber}">
	        <f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="dayphone" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.eveningTimePhonePrompt}"/>
		<h:inputText id="evening" value="#{customers.customer.eveningPhone}"
			label="#{msgs.eveningTimePhonePrompt}" maxlength="18" size="18"
			binding="#{customers.eveningPhoneInput}"
			validator="#{customers.validatePhoneNumber}">
	        <f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="evening" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.mobilePhonePrompt}"/>
		<h:inputText id="mobile" value="#{customers.customer.mobilePhone}"
			label="#{msgs.mobilePhonePrompt}" maxlength="18" size="18"
			binding="#{customers.mobilePhoneInput}"
			validator="#{customers.validatePhoneNumber}">
	       <f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="mobile" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.faxNumberPrompt}"/>
		<h:inputText id="faxnbr" value="#{customers.customer.faxNumber}"
			label="#{msgs.faxNumberPrompt}" maxlength="18" size="18"
			binding="#{customers.faxNumberInput}"
			validator="#{customers.validatePhoneNumber}">
	       <f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="faxnbr" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.statusIdPrompt}"/>
		<h:selectOneMenu id="statusid" value="#{customers.customer.statusId}"
			required="true" label="#{msgs.statusIdPrompt}">
			<f:selectItems value="#{codes.simpleStatusIdItems}"/>
		</h:selectOneMenu>
		<h:message for="statusid" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.securityQuestionPrompt}"/>
		<h:selectOneMenu id="question" value="#{customers.customer.securityQuestion}"
			label="#{msgs.securityQuestionPrompt}">
			<f:selectItem itemLabel="" itemValue=""/>
			<f:selectItems value="#{codes.securityQuestionItems}"/>
	        <f:converter converterId="NullableStringConverter"/>
		</h:selectOneMenu>
		<h:message for="question" styleClass="errorMessage"/>

		<h:outputText value="#{msgs.securityAnswerPrompt}"/>
		<h:inputText id="answer" value="#{customers.customer.securityAnswer}"
			label="#{msgs.securityAnswerPrompt}" maxlength="26" size="26">
	        <f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="answer" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.userPasswordPrompt}"/>
		<h:inputText id="passwd" value="#{customers.customer.userPassword}"
			label="#{msgs.userPasswordPrompt}" maxlength="32" size="32">
	        <f:converter converterId="NullableStringConverter"/>
		</h:inputText>
		<h:message for="passwd" styleClass="errorMessage"/>
	</h:panelGrid>

	<h:outputText value="#{msgs[customers.testResult]}"
		rendered="#{customers.testResult != null}" styleClass="errorMessage"
		id="testResult" />
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{customers.saveCustomer}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.refreshLinkText}" 
				action="#{customers.refreshCustomer}"
				immediate="true" title="Refresh" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{customers.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

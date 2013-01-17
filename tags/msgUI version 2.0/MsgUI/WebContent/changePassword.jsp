<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<f:view>
	<head>
	<link href="/MsgUI/styles.css" rel="stylesheet" type="text/css" />
	<title><h:outputText value="#{msgs.changePassword}" /></title>
	</head>
	<body onLoad="document.getElementById('changepswd:currpswd').focus();">
	<div align="center">
	<h:form id="changepswd">
	<h:panelGrid columns="1" styleClass="loginHeaderContent">
	<c:import url="includes/gettingStartedHeader.jsp"/>
	<h:panelGroup style="text-align: left;">
		<h:messages styleClass="errors" layout="list"
			rendered="#{debug.showMessages}" />
		<f:verbatim><p/></f:verbatim>
		<h:graphicImage value="/images/space.gif" style="border: 0px" height="1" width="10"/>
		<h:outputText value="#{msgs.changePasswordMessage}" styleClass="gridHeader"/>
		<f:verbatim><p/></f:verbatim>
		<h:panelGrid columns="3" styleClass="smtpHeaders" 
			columnClasses="loginLabelColumn, loginInputColumn, loginMessageColumn">

			<h:outputText value="#{msgs.currPasswordPrompt}"/>
			<h:inputSecret value="#{changePassword.currPassword}" id="currpswd"
				label="#{msgs.currPasswordPrompt}" required="true" maxlength="30">
				<f:validateLength minimum="4" maximum="30"/>
			</h:inputSecret>
			<h:message for="currpswd" styleClass="errorMessage"/>

			<h:outputText value="#{msgs.newPasswordPrompt}" />
			<h:inputSecret value="#{changePassword.password}" id="password"
				label="#{msgs.newPasswordPrompt}" required="true" maxlength="30">
				<f:validateLength minimum="4" maximum="30"/>
			</h:inputSecret>
			<h:message for="password" styleClass="errorMessage"/>
			
			<h:outputText value="#{msgs.confirmPasswordPrompt}" />
			<h:inputSecret value="#{changePassword.confirm}" id="confirm"
				label="#{msgs.confirmPasswordPrompt}" required="true" maxlength="30">
				<f:validateLength minimum="4" maximum="30"/>
				<f:validator validatorId="passwordValidator" />
				<f:attribute name="password1" value="changepswd:password" />
			</h:inputSecret>
			<h:message for="confirm" styleClass="errorMessage"/>
		</h:panelGrid>
		<h:panelGrid columns="1" styleClass="fullWidth" columnClasses="alignCenter">
			<h:panelGroup rendered="#{changePassword.message != null}">
				<f:verbatim><br></f:verbatim>
				<h:outputText value="#{changePassword.message}" styleClass="errorMessage"/>
			</h:panelGroup>
			<f:verbatim><br></f:verbatim>
			<h:commandButton value="#{msgs.changeButtonText}" title="Change Password"
				action="#{changePassword.changePassword}"
				onclick="javascript:return confirmSubmit();" />
		</h:panelGrid>
	</h:panelGroup>
	<c:import url="includes/gettingStartedFooter.jsp"/>
	</h:panelGrid>
	</h:form>
	</div></body>
</f:view>
</html>
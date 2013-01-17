<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<f:view>
	<head>
	<link href="styles.css" rel="stylesheet" type="text/css" />
	<title><h:outputText value="#{msgs.timeoutPageTitle}" /></title>
	</head>
	<body>
	<div align="center">
	<h:form id="login">
	<h:panelGrid columns="1" styleClass="loginHeaderContent">
	<c:import url="includes/gettingStartedHeader.jsp"/>
	<h:panelGroup  style="text-align: center;">
		<h:messages styleClass="errors" layout="list"
			rendered="#{debug.showMessages}" />
		<f:verbatim><p/></f:verbatim>
		<h:outputText value="#{msgs.sessionTimedoutMessage}" styleClass="errorMessage"
			style="font-size: large;"/>
		<f:verbatim><p/></f:verbatim>
		<h:panelGrid columns="3" styleClass="smtpHeaders" 
			columnClasses="loginLabelColumn, loginInputColumn, loginMessageColumn">
			
			<h:outputText value="#{msgs.userIdPrompt}" />
			<h:inputText value="#{login.userId}"
				label="#{msgs.userIdPrompt}" required="true" maxlength="10"
				id="userid">
			</h:inputText>
			<h:message for="userid" styleClass="errorMessage"/>

			<h:outputText value="#{msgs.passwordPrompt}" />
			<h:inputSecret value="#{login.password}"
				label="#{msgs.passwordPrompt}" required="true" maxlength="30"
				id="password">
			</h:inputSecret>
			<h:message for="password" styleClass="errorMessage"/>
		</h:panelGrid>
		<h:panelGrid columns="1" styleClass="fullWidth" columnClasses="alignCenter">
			<f:verbatim rendered="#{login.message != null}"><br></f:verbatim>
			<h:outputText value="#{msgs.invalidLoginMessage}"
				rendered="#{login.message != null}" styleClass="errorMessage"/>
			<f:verbatim><br></f:verbatim>
			<h:commandButton value="#{msgs.loginButtonText}" title="Login"
				action="#{login.login}"/>
		</h:panelGrid>
	</h:panelGroup>
	<c:import url="includes/gettingStartedFooter.jsp"/>
	</h:panelGrid>
	</h:form>
	</div></body>
</f:view>
</html>
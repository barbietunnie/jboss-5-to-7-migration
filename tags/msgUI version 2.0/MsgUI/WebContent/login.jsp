<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<f:view>
	<head>
	<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
	<META HTTP-EQUIV="Expires" CONTENT="-1">
	<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
	<link href="/MsgUI/styles.css" rel="stylesheet" type="text/css" />
	<title><h:outputText value="#{msgs.loginPageTitle}" /></title>
	</head>
	<body onLoad="document.getElementById('login:userid').focus();">
	<div align="center">
	<h:form id="login">
	<h:panelGrid columns="1" styleClass="loginHeaderContent">
	<c:import url="includes/gettingStartedHeader.jsp"/>
	<h:panelGroup style="text-align: left;">
		<h:messages styleClass="errors" layout="list"
			rendered="#{debug.showMessages}" />
		<f:verbatim><p/></f:verbatim>
		<h:graphicImage value="/images/space.gif" style="border: 0px" height="1" width="10"/>
		<h:outputText value="#{msgs.loginMessage}"
			style="color: blue; font-size: large;"
			rendered="#{login.source != 'timeout'}"/>
		<h:outputText value="#{msgs.sessionTimedoutMessage}"
			style="color: red; font-size: large;" 
			rendered="#{login.source == 'timeout'}"/>
		<f:verbatim><p/></f:verbatim>
		<h:panelGrid columns="3" styleClass="smtpHeaders" 
			columnClasses="loginLabelColumn, loginInputColumn, loginMessageColumn">

			<h:outputText value="#{msgs.userIdPrompt}"/>
			<h:inputText value="#{login.userId}" id="userid"
				label="#{msgs.userIdPrompt}" required="true" maxlength="10">
			</h:inputText>
			<h:message for="userid" styleClass="errorMessage"/>

			<h:outputText value="#{msgs.passwordPrompt}" />
			<h:inputSecret value="#{login.password}" id="password"
				label="#{msgs.passwordPrompt}" required="true" maxlength="30">
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
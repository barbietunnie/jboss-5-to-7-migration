<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<f:view>
	<head>
	<link href="/MsgUI/styles.css" rel="stylesheet" type="text/css" />
	<title><h:outputText value="#{msgs.enterProductKey}" /></title>
	</head>
	<body onLoad="document.getElementById('enterkey:prodkey').focus();">
	<div align="center">
	<h:form id="enterkey">
	<h:panelGrid columns="1" styleClass="loginHeaderContent">
	<c:import url="includes/gettingStartedHeader.jsp"/>
	<h:panelGroup style="text-align: left;">
		<h:messages styleClass="errors" layout="list"
			rendered="#{debug.showMessages}" />
		<f:verbatim><p/></f:verbatim>
		<h:graphicImage value="/images/space.gif" style="border: 0px" height="1" width="10"/>
		<h:outputText value="#{msgs.enterProductKeyMessage}" styleClass="gridHeader"/>
		<f:verbatim><p/></f:verbatim>
		<h:panelGrid columns="3" styleClass="smtpHeaders" 
			columnClasses="loginLabelColumn, loginInputColumn, loginMessageColumn">

			<h:outputText value="#{msgs.productKeyPrompt}"/>
			<h:inputText value="#{enterProductKey.productKey}" id="prodkey"
				label="#{msgs.productKeyPrompt}" required="true" size="40" maxlength="30">
				<f:validateLength minimum="29" maximum="29"/>
			</h:inputText>
			<h:message for="prodkey" styleClass="errorMessage"/>
		</h:panelGrid>
		<h:panelGrid columns="1" styleClass="fullWidth" columnClasses="alignCenter">
			<h:panelGroup rendered="#{enterProductKey.message != null}">
				<f:verbatim><br></f:verbatim>
				<h:outputText value="#{enterProductKey.message}" styleClass="errorMessage"/>
			</h:panelGroup>
			<f:verbatim><br></f:verbatim>
			<h:commandButton value="#{msgs.submitButtonText}" title="submit product key"
				action="#{enterProductKey.enterProductKey}"
				onclick="javascript:return confirmSubmit();" />
		</h:panelGrid>
	</h:panelGroup>
	<c:import url="includes/gettingStartedFooter.jsp"/>
	</h:panelGrid>
	</h:form>
	</div></body>
</f:view>
</html>
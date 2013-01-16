<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<f:view>
	<head>
	<link href="styles.css" rel="stylesheet" type="text/css" />
	<title><h:outputText value="#{msgs.previewMailingListEmailPageTitle}" /></title>
	</head>
	<body>
	<div align="center">
	<h:form id="preview">
	<h:panelGrid columns="1" styleClass="headerMenuContent">
	<c:import url="includes/gettingStartedHeader.jsp"/>
	<h:panelGroup>
		<h:messages styleClass="errors" layout="list"
			rendered="#{debug.showMessages}" />
		<f:verbatim><br/></f:verbatim>
		<h:outputText value="#{msgs.previewMessageTitle}" styleClass="mediumLargeSizeTitle"/>
		<f:verbatim>&nbsp;<p/></f:verbatim>
		<h:panelGrid columns="2" styleClass="smtpHeaders" 
			columnClasses="smtpLabelColumn, smtpTextColumn">
			<h:outputText value="#{msgs.msgSubjectPrompt}" />
			<h:outputText value="#{maillistcomp.renderedSubj}" escape="false"/>
		</h:panelGrid>
		<f:verbatim>&nbsp;<p/></f:verbatim>
		<h:panelGrid columns="1" style="background: White;" styleClass="fullWidthWithBorder">
			<h:outputText value="#{maillistcomp.renderedBody}" escape="false"/>
		</h:panelGrid>
		<f:verbatim><br/>
			<input type="button" value="Back" onclick="history.back()"/>
		</f:verbatim>
	</h:panelGroup>
	<c:import url="includes/gettingStartedFooter.jsp"/>
	</h:panelGrid>
	</h:form>
	</div></body>
</f:view>
</html>
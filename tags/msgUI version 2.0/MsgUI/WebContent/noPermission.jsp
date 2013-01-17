<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<f:view>
	<head>
	<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
	<META HTTP-EQUIV="Expires" CONTENT="-1">
	<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
	<link href="styles.css" rel="stylesheet" type="text/css" />
	<title><h:outputText value="#{msgs.noPermissionPageTitle}" /></title>
	</head>
	<body>
	<div align="center">
	<h:form id="nopermit">
	<h:panelGrid columns="1" styleClass="loginHeaderContent">
	<c:import url="includes/gettingStartedHeader.jsp"/>
	<h:panelGroup style="text-align: left;">
		<h:messages styleClass="errors" layout="list"
			rendered="#{debug.showMessages}" />
		<f:verbatim><p/></f:verbatim>
		<h:graphicImage value="/images/space.gif" style="border: 0px" height="1" width="10"/>
		<h:outputText value="#{msgs.noPermissionMessage}"
			style="color: red; font-size: x-large;"/>
		<f:verbatim><p/></f:verbatim>
	</h:panelGroup>
	<c:import url="includes/gettingStartedFooter.jsp"/>
	</h:panelGrid>
	</h:form>
	</div></body>
</f:view>
</html>
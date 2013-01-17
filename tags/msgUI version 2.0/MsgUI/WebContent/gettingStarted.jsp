<html>
   <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
   <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
   <f:view>
      <head>
         <link href="styles.css" rel="stylesheet" type="text/css"/>
         <title><h:outputText value="#{msgs.gettingStartedWindowTitle}"/></title>
      </head>
      <body onLoad="document.getElementById('mailbox:content:hostname').focus();">
      <div align="center">
         <h:form id="mboxlist">
         <c:set var="browser" value="${header['User-Agent']}"/>
         <h:panelGrid columns="2" styleClass="headerMenuContent"
                columnClasses="menuColumn, contentColumn">
			<f:facet name="header">
			   <f:subview id="header">
			      <c:import url="/includes/gettingStartedHeader.jsp"/>
			   </f:subview>
			</f:facet>
			<f:subview id="menu">
			   <c:import url="/includes/gettingStartedMenu.jsp"/>
			</f:subview>
			<f:subview id="content">
		   	<h:panelGroup rendered="#{gettingStarted.functionKey == 'configureMailboxes'}">
           		<c:import url="/includes/configureMailboxes.jsp"/>
           	</h:panelGroup>
		   	<h:panelGroup rendered="#{gettingStarted.functionKey == 'configureSmtpServers'}">
           		<c:import url="/includes/configureSmtpServers.jsp"/>
           	</h:panelGroup>
		   	<h:panelGroup rendered="#{gettingStarted.functionKey == 'configureSiteProfiles'}">
           		<c:import url="/includes/configureSiteProfiles.jsp"/>
           	</h:panelGroup>
		   	<h:panelGroup rendered="#{gettingStarted.functionKey == 'customizeBuiltInRules'}">
           		<c:import url="/includes/customizeBuiltInRules.jsp"/>
           	</h:panelGroup>
		   	<h:panelGroup rendered="#{gettingStarted.functionKey == 'configureCustomRules'}">
           		<c:import url="/includes/configureMsgRules.jsp"/>
           	</h:panelGroup>
		   	<h:panelGroup rendered="#{gettingStarted.functionKey == 'maintainActionDetails'}">
           		<c:import url="/includes/msgActionDetailList.jsp"/>
           	</h:panelGroup>
		   	<h:panelGroup rendered="#{gettingStarted.functionKey == 'configureMailingLists'}">
           		<c:import url="/includes/configureMailingLists.jsp"/>
           	</h:panelGroup>
		   	<h:panelGroup rendered="#{gettingStarted.functionKey == 'configureEmailVariables'}">
           		<c:import url="/includes/configureEmailVariables.jsp"/>
           	</h:panelGroup>
		   	<h:panelGroup rendered="#{gettingStarted.functionKey == 'configureEmailTemplates'}">
           		<c:import url="/includes/configureEmailTemplates.jsp"/>
           	</h:panelGroup>
		   	<h:panelGroup rendered="#{gettingStarted.functionKey == 'manageUserAccounts'}">
           		<c:import url="/includes/manageUserAccounts.jsp"/>
           	</h:panelGroup>
            </f:subview>
         </h:panelGrid>
         </h:form>
      </div></body>
   </f:view>
</html>

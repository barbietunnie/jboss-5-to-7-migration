<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:panelGrid id="gettingStartedFooter" columns="2" styleClass="gettingStartedFooter"
	columnClasses="gettingStartedFooterHeight, gettingStartedFooterHeight">
	<h:panelGroup style="text-align: left;">
		<h:outputText value="&copy; #{msgs.copyrightText}" styleClass="headerLinkText" escape="false"/>
	</h:panelGroup>
   	<h:panelGroup style="text-align: right;">
   		<h:outputText value="#{codes.poweredByHtmlTag}" styleClass="headerLinkText" escape="false"/>
   	</h:panelGroup>
</h:panelGrid>

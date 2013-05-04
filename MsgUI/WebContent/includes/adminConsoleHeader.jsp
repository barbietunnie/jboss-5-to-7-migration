<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:panelGrid id="simpleMailTrackingHeader" columns="3" styleClass="gettingStartedHeader"
	columnClasses="fifteenPercent, seventyPercent, fifteenPercent">
   	<f:verbatim>&nbsp;</f:verbatim>
   	<h:outputText value="#{msgs.adminConsoleHeaderText}" styleClass="gettingStartedTitle"/>
   	<h:panelGroup style="text-align: right; vertical-align: bottom;">
   		<f:verbatim>&nbsp;<br></f:verbatim>
   		<h:outputLink value="#{login.mainPage}" styleClass="headerLinkText"
   			rendered="#{login.userLoggedin && !login.currentPageMainPage}">
   			<h:outputText value="Main"/>
   		</h:outputLink>
		<h:graphicImage value="/images/space.gif" style="border: 0px" height="1" width="10"/>
		<h:commandLink value="Logout" action="#{login.logout}" immediate="true"
   			styleClass="headerLinkText" rendered="#{login.userLoggedin}"/>
   	</h:panelGroup>
</h:panelGrid>

<f:verbatim>
<!-- verbatim tag is used to prevent JSF from rendering tr/td tags to javascript -->
<script type="text/javascript" src="includes/msguiCommon.js"></script>
</f:verbatim>

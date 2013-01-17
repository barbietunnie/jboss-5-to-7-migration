<html>
   <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
   <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

   <f:view>
      <head>
         <link href="styles.css" rel="stylesheet" type="text/css"/>
         <script type="text/javascript" src="includes/insertAtCursor.js"></script>
				<script type="text/javascript" src="includes/whizzywig.js"></script>
         <title><h:outputText value="#{msgs.editEmailTemplatePageTitle}"/></title>
      </head>
	  <noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>

      <body onLoad="document.getElementById('emailtmplt:content:templateid').focus();">
      <div align="center">
         <h:form id="emailtmplt">
            <h:panelGrid columns="1" styleClass="headerMenuContent"
                   columnClasses="contentColumn">
               <f:facet name="header">
                  <f:subview id="header">
                     <c:import url="/includes/gettingStartedHeader.jsp"/>
                  </f:subview>
               </f:facet>
				<f:subview id="content">
               	<c:import url="/includes/emailTemplateEdit.jsp"/>
               </f:subview>
               <f:subview id="footer">
              	<c:import url="includes/gettingStartedFooter.jsp"/>
               </f:subview>
            </h:panelGrid>
         </h:form>
      </div></body>
   </f:view>
</html>

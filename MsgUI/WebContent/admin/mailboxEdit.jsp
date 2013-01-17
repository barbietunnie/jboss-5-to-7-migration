<html>
   <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
   <%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

   <f:view>
      <head>
         <link href="../styles.css" rel="stylesheet" type="text/css"/>
         <title><h:outputText value="#{msgs.editMailboxPageTitle}"/></title>
      </head>

      <body onLoad="document.getElementById('mboxedit:content:hostname').focus();">
      <div align="center">
         <h:form id="mboxedit">
            <h:panelGrid columns="1" styleClass="headerMenuContent"
                   columnClasses="contentColumn">
               <f:facet name="header">
                  <f:subview id="header">
                     <c:import url="/includes/adminConsoleHeader.jsp"/>
                  </f:subview>
               </f:facet>

               <%-- f:subview id="menu">
                  <c:import url="/includes/gettingStartedMenu.jsp"/>
               </f:subview --%>

			   <f:subview id="content">
			   	<c:import url="/includes/mailboxEdit.jsp"/>
               </f:subview>
               <f:subview id="footer">
              	<c:import url="/includes/gettingStartedFooter.jsp"/>
               </f:subview>
            </h:panelGrid>
         </h:form>
      </div></body>
   </f:view>
</html>


<html>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>  
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <f:view beforePhase="#{phaseform.beforePhase}"
	   afterPhase="#{phaseform.afterPhase}">
      <head>                  
         <link href="styles.css" rel="stylesheet" type="text/css"/>
         <title>
            <h:outputText value="#{msgs.phaseWindowTitle}"/>
         </title>
      </head>

      <body>
         <h:form>
            <h:panelGrid columns="2" columnClasses="phaseFormColumns">
               <h:outputText value="#{msgs.phasePrompt}"/>

               <h:selectOneListbox valueChangeListener="#{phaseform.phaseChange}">
                  <f:selectItems value="#{phaseform.phases}"/>
               </h:selectOneListbox>

               <h:commandButton value="#{msgs.submitPrompt}"/>
            </h:panelGrid>
         </h:form>
      </body>
   </f:view>
</html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<%-- @ include file="/displayRequestParams.jsp" --%>
<h:dataTable value="#{gettingStarted.functionKeys}" var="functionKey" 
       styleClass="gettingStartedMenu" columnClasses="gettingStartedMenuColumn">
   <h:column>
	  <h:outputText value="Step #{gettingStarted.functionKeys.rowIndex + 1}"
	  	styleClass="menuLinkLables"/>
	  <h:outputText value=" (Required)" styleClass="menuLinkLables" style="font-size: 1em;"
	  	rendered="#{gettingStarted.functionRequired[gettingStarted.functionKeys.rowIndex] == 'yes'}"/>
	  <%-- h:outputText value=" (Optional)" styleClass="menuLinkLables" style="font-size: 1em;"
	  	rendered="#{gettingStarted.functionRequired[gettingStarted.functionKeys.rowIndex] == 'no'}"/ --%>
	  <f:verbatim>:<br/></f:verbatim>
      <h:commandLink action="#{gettingStarted.selectFunction}" immediate="true"
      	styleClass="#{gettingStarted.functionKey == functionKey ? 'menuLinkTextSelected' : 'menuLinkText'}"
      		>
		<f:param name="functionKey" value="#{functionKey}"/>
		<h:outputText value="#{msgs[functionKey]}" title="#{functionKey}"/>
      </h:commandLink>
      <h:inputHidden value="#{gettingStarted.functionKey}"/>
      <%-- h:outputLink value="gettingStarted.faces"
      	styleClass="#{gettingStarted.functionKey == functionKey ? 'menuLinkTextSelected' : 'menuLinkText'}">
      	<f:param name="functionKey" value="#{functionKey}"/>
      	<h:outputText value="#{msgs[functionKey]}" title="#{functionKey}"/>
      </h:outputLink --%>
   </h:column>
</h:dataTable>

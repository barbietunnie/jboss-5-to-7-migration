<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>

	<c:if test="${param.functionKey == null}">
		<c:import url="includes/configureMailboxes.jsp"/>
	</c:if>

	<c:if test="${param.functionKey > ''}">
		<c:import url="includes/${param.functionKey}.jsp"/>
	</c:if>

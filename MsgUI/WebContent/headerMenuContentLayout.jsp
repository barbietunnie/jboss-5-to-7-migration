<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:importAttribute scope="request"/>

<h:panelGrid id="headerMenuContentTiles" columns="2" styleClass="#{gridClass}"
      headerClass="#{headerClass}"
      columnClasses="#{menuColumnClass}, #{contentColumnClass}">
   <f:facet name="header">
      <f:subview id="header">
         <tiles:insertAttribute name="header" flush="false"/>
      </f:subview>
   </f:facet>

   <f:subview id="menu">
      <tiles:insertAttribute name="menu" flush="false"/>
   </f:subview>

   <f:subview id="content">
      <tiles:insertAttribute name="content" flush="false"/>
   </f:subview>
</h:panelGrid>

<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.subscribersListLabel}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable binding="#{subscribersListBean.dataTable}" 
   value="#{subscribersListBean.subscribers}" var="list" 
   rows="#{subscribersListBean.pagingVo.pageSize}"
   styleClass="jsfDataTable" rowClasses="oddRows, evenRows"
   headerClass="dataTableHeader" footerClass="dataTableFooter">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value=""/>
      </f:facet>
      <h:selectBooleanCheckbox value="#{list.markedForDeletion}" 
         disabled="#{not list.editable}" onchange="submit()"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.emailAddrHeader}"/>
      </f:facet>
      <h:outputText value="#{list.emailAddrShort}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.subscribedHeader}"/>
      </f:facet>
		<h:selectOneMenu value="#{list.subscribed}">
			<f:selectItems value="#{codes.yorNItems}"/>
			<f:selectItem itemLabel="Pending" itemValue="P"/>
		</h:selectOneMenu>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.createDateHeader}"/>
      </f:facet>
      <h:outputText value="#{list.createTime}">
      	<f:convertDateTime pattern="yyyy-MM-dd"/>
      </h:outputText>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.acceptHtmlHeader}"/>
      </f:facet>
		<h:selectOneMenu value="#{list.acceptHtml}">
			<f:selectItems value="#{codes.yorNItems}"/>
		</h:selectOneMenu>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.sentCountHeader}"/>
      </f:facet>
      <h:outputText value="#{list.sentCount}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.openCountHeader}"/>
      </f:facet>
      <h:outputText value="#{list.openCount}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.clickCountHeader}"/>
      </f:facet>
      <h:outputText value="#{list.clickCount}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.lastOpenedHeader}"/>
      </f:facet>
      <h:outputText value="#{list.lastOpenTime}">
      	<f:convertDateTime pattern="yyyy-MM-dd"/>
      </h:outputText>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.lastClickedHeader}"/>
      </f:facet>
      <h:outputText value="#{list.lastClickTime}">
      	<f:convertDateTime pattern="yyyy-MM-dd"/>
      </h:outputText>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.customerNameHeader}"/>
      </f:facet>
	  <h:outputText value="#{list.customerName}"/>
   </h:column>
	<f:facet name="footer">
    <h:panelGroup>
	<h:panelGrid columns="2" styleClass="fullWidth"
		columnClasses="alignLeft50, alignRight50">
		<h:panelGroup>
			<h:inputText value="#{subscribersListBean.searchString}" size="40"
				maxlength="100"/>
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.searchByEmailButtonText}" title="Search"
				action="#{subscribersListBean.searchByAddress}" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.resetButtonText}" title="Reset"
				action="#{subscribersListBean.resetSearch}" />
		</h:panelGroup>
		<h:panelGroup>
          	<h:outputText value="#{subscribersListBean.dataTable.first + 1}"
          		style="font-weight: bold;"/>
          	<h:outputText value=" - "/>
			<h:outputText value="#{subscribersListBean.lastPageRow}"
				style="font-weight: bold;" />
			<h:outputText value=" of #{subscribersListBean.dataTable.rowCount}" 
          		style="font-weight: bold;"/>
          	<f:verbatim>&nbsp;&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.firstLinkText}" action="#{subscribersListBean.pageFirst}"
                disabled="#{subscribersListBean.dataTable.first == 0}" />
            <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.prevLinkText}" action="#{subscribersListBean.pagePrevious}"
                disabled="#{subscribersListBean.dataTable.first == 0}" />
            <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.nextLinkText}" action="#{subscribersListBean.pageNext}"
                disabled="#{subscribersListBean.dataTable.first + subscribersListBean.dataTable.rows
                    >= subscribersListBean.dataTable.rowCount}" />
            <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.lastLinkText}" action="#{subscribersListBean.pageLast}"
                disabled="#{subscribersListBean.dataTable.first + subscribersListBean.dataTable.rows
                    >= subscribersListBean.dataTable.rowCount}" />
        </h:panelGroup>
    </h:panelGrid>
    </h:panelGroup>
	</f:facet>
</h:dataTable>
<f:verbatim><p/></f:verbatim>
<h:panelGrid columns="2" styleClass="commandBar"
	columnClasses="alignLeft70, alignRight30">
	<h:panelGroup>
		<h:commandButton value="#{msgs.deleteButtonText}"
			title="Delete selected rows" action="#{subscribersListBean.deleteSubscribers}"
			disabled="#{not subscribersListBean.anySubscribersMarkedForDeletion}"
			onclick="javascript:return confirmDelete();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.saveSelectedButtonText}"
			title="Delete selected rows" action="#{subscribersListBean.saveSubscribers}"
			disabled="#{not subscribersListBean.anySubscribersMarkedForDeletion}"
			onclick="javascript:return confirmSubmit();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<%-- h:commandButton value="#{msgs.addNewButtonText}"
			title="Add a new row" action="#{subscribersListBean.addSubscriber}" />
		<f:verbatim>&nbsp;</f:verbatim --%>
		<h:commandButton value="#{msgs.refreshLinkText}"
			title="Refresh from database" action="#{subscribersListBean.refreshPage}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.backButtonText}" action="#{subscribersListBean.cancelEdit}"/>
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>
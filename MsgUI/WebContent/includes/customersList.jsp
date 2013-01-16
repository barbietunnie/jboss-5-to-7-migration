<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
<h:outputFormat value="#{msgs.manageCustomerInformation}" styleClass="gridHeader">
   <f:param value=""/>
</h:outputFormat>
<h:dataTable binding="#{customers.dataTable}" 
   value="#{customers.customers}" var="list" 
   rows="#{customers.pagingVo.pageSize}"
   styleClass="jsfDataTable" rowClasses="oddRows, evenRows"
   headerClass="dataTableHeader" footerClass="dataTableFooter"
   columnClasses="twoPercent,tenPercent,tenPercent,tenPercent,fourtyPercent,tenPercent,tenPercent,tenPercent">
   
   <h:column>
      <f:facet name="header">
         <h:outputText value=""/>
      </f:facet>
      <h:selectBooleanCheckbox value="#{list.markedForDeletion}" 
         disabled="#{not list.editable}" onchange="submit()"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.customerIdHeader}"/>
      </f:facet>
      <h:commandLink action="#{customers.viewCustomer}">
      	<h:outputText value="#{list.custId}" title="Click to Edit"/>
      	<f:param name="custId" value="#{list.custId}"/>
      </h:commandLink>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.firstNameHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:outputText value="#{list.firstName}"/>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.lastNameHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:outputText value="#{list.lastName}"/>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.emailAddrHeader}"/>
      </f:facet>
      <div class="cellHeight">
      <h:outputText value="#{list.emailAddr}"/>
      </div>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.startDateHeader}"/>
      </f:facet>
		<h:outputText value="#{list.startDate}">
			<f:convertDateTime pattern="yyyy-MM-dd" type="date"/>
		</h:outputText>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.dayPhoneNumberHeader}"/>
      </f:facet>
	  <h:outputText value="#{list.dayPhone}"/>
   </h:column>
   <h:column>
      <f:facet name="header">
         <h:outputText value="#{msgs.birthDateHeader}"/>
      </f:facet>
	  <h:outputText value="#{list.birthDate}">
	  	<f:convertDateTime pattern="yyyy-MM-dd" type="date"/>
	  </h:outputText>
   </h:column>
	<f:facet name="footer">
    <h:panelGroup>
	<h:panelGrid columns="2" styleClass="fullWidth"
		columnClasses="alignLeft50, alignRight50">
		<h:panelGroup>
			<h:inputText value="#{customers.searchString}" size="30"
				maxlength="100"/>
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.searchByEmailButtonText}"
				title="Search By Email Address"
				action="#{customers.searchByAddress}" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.resetButtonText}" title="Reset"
				action="#{customers.resetSearch}" />
		</h:panelGroup>
		<h:panelGroup>
          	<h:outputText value="#{customers.dataTable.first + 1}"
          		style="font-weight: bold;"/>
          	<h:outputText value=" - "/>
			<h:outputText value="#{customers.lastPageRow}"
				style="font-weight: bold;" />
			<h:outputText value=" of #{customers.dataTable.rowCount}" 
          		style="font-weight: bold;"/>
          	<f:verbatim>&nbsp;&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.firstLinkText}" action="#{customers.pageFirst}"
                disabled="#{customers.dataTable.first == 0}" />
            <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.prevLinkText}" action="#{customers.pagePrevious}"
                disabled="#{customers.dataTable.first == 0}" />
            <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.nextLinkText}" action="#{customers.pageNext}"
                disabled="#{customers.dataTable.first + customers.dataTable.rows
                    >= customers.dataTable.rowCount}" />
            <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
            <h:commandLink value="#{msgs.lastLinkText}" action="#{customers.pageLast}"
                disabled="#{customers.dataTable.first + customers.dataTable.rows
                    >= customers.dataTable.rowCount}" />
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
			title="Delete selected rows" action="#{customers.deleteCustomers}"
			disabled="#{not customers.anyCustomersMarkedForDeletion}"
			onclick="javascript:return confirmDelete();" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.copyToNewButtonText}"
			title="Delete selected rows" action="#{customers.copyCustomer}"
			disabled="#{not customers.anyCustomersMarkedForDeletion}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.addNewButtonText}"
			title="Add a new row" action="#{customers.addCustomer}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshLinkText}"
			title="Refresh from database" action="#{customers.refreshPage}" />
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.backButtonText}" action="#{customers.cancelEdit}"/>
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;</f:verbatim>
	</h:panelGroup>
</h:panelGrid>
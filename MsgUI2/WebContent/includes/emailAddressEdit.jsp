<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.emailAddressEditLabel}" styleClass="gridHeader">
	   <f:param value="#{emailAddrsBean.emailAddr.emailAddr}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader" 
		columnClasses="promptColumn, inputColumn, messageColumn">
		<h:outputText value="#{msgs.emailAddrPrompt}"/>
		<h:inputText id="emailaddr" value="#{emailAddrsBean.emailAddr.emailAddr}"
			required="true" binding="#{emailAddrsBean.emailAddrInput}" 
			label="#{msgs.emailAddrPrompt}" maxlength="255" size="80"
			validator="#{emailAddrsBean.validatePrimaryKey}">
			<f:validateLength minimum="1" maximum="255"/>
			<f:validator validatorId="msgui.EmailAddressValidator"/>
		</h:inputText>
		<h:message for="emailaddr" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.statusIdPrompt}"/>
		<h:selectOneMenu id="statusid" value="#{emailAddrsBean.emailAddr.statusId}"
			required="true" label="#{msgs.statusIdPrompt}">
			<f:selectItems value="#{codes.mailboxStatusIdItems}"/>
		</h:selectOneMenu>
		<h:message for="statusid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.acceptHtmlPrompt}"/>
		<h:selectOneMenu id="html" value="#{emailAddrsBean.emailAddr.acceptHtml}"
			label="#{msgs.acceptHtmlPrompt}">
			<f:selectItems value="#{codes.yorNItems}"/>
		</h:selectOneMenu>
		<h:message for="html" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.bounceCountPrompt}"/>
		<h:inputText id="bounce" value="#{emailAddrsBean.emailAddr.bounceCount}"
			label="#{msgs.bounceCountPrompt}" size="8" maxlength="5">
			<f:validateLongRange minimum="0" maximum="99999"/>
		</h:inputText>
		<h:message for="bounce" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.statusChangeTimePrompt}"/>
		<h:inputText id="chgtime" value="#{emailAddrsBean.emailAddr.statusChangeTime}"
			readonly="true" disabled="true">
			<f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/>
		</h:inputText>
		<h:message for="chgtime" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.statusChnageUserPrompt}"/>
		<h:inputText id="chguser" value="#{emailAddrsBean.emailAddr.statusChangeUserId}"
			readonly="true" disabled="true">
		</h:inputText>
		<h:message for="chguser" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.lastBounceTimePrompt}"/>
		<h:inputText id="btime" value="#{emailAddrsBean.emailAddr.lastBounceTime}"
			readonly="true" disabled="true">
			<f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/>
		</h:inputText>
		<h:message for="btime" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.lastSentTimePrompt}"/>
		<h:inputText id="senttime" value="#{emailAddrsBean.emailAddr.lastSentTime}" 
			readonly="true" disabled="true">
			<f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/>
		</h:inputText>
		<h:message for="senttime" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.lastReceivedTimePrompt}"/>
		<h:inputText id="rcpttime" value="#{emailAddrsBean.emailAddr.lastRcptTime}"
			readonly="true" disabled="true">
			<f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/>
		</h:inputText>
		<h:message for="rcpttime" styleClass="errorMessage"/>
	</h:panelGrid>
	<h:panelGrid columns="1" styleClass="editPaneHeader"
			rendered="#{emailAddrsBean.editMode}">
		<h:outputText value="Mailing Lists subscribed: " styleClass="mediumSizeTitle"/>
		<h:outputText value="0" rendered="#{emailAddrsBean.mailingListsEmpty}"/>
		<h:dataTable value="#{emailAddrsBean.mailingLists}" var="list"
			styleClass="jsfDataTable" rowClasses="oddRows, evenRows"
   			headerClass="dataTableHeader" footerClass="dataTableFooter"
   			rendered="#{!emailAddrsBean.mailingListsEmpty}">
		<h:column>
      		<f:facet name="header">
        		<h:outputText value="#{msgs.listIdHeader}"/>
      		</f:facet>
      		<h:outputText value="#{list.listId}"/>
   		</h:column>
		<h:column>
      		<f:facet name="header">
       			<h:outputText value="#{msgs.displayNameHeader}"/>
      		</f:facet>
      		<div class="cellHeight">
      			<h:outputText value="#{list.displayName}"/>
      		</div>
   		</h:column>		
		<h:column>
      		<f:facet name="header">
        		<h:outputText value="#{msgs.listEmailAddressHeader}"/>
      		</f:facet>
      		<div class="cellHeight">
      			<h:outputText value="#{list.emailAddr}"/>
      		</div>
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
		</h:dataTable>
	</h:panelGrid>
	<h:outputText value="#{msgs[emailAddrsBean.testResult]}"
		rendered="#{emailAddrsBean.testResult != null}" styleClass="errorMessage"
		id="testResult" />
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{emailAddrsBean.saveEmailAddr}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{emailAddrsBean.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

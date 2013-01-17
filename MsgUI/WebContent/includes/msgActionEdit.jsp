<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>

<script type="text/javascript">
function displayDP(rowIndex) {
	var fieldName = "actionedit:content:jsftable:" + rowIndex + ":startdate";
	var dateField = document.getElementById(fieldName);
	displayDatePicker(dateField.name);
}
</script>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.msgActionEditLabel}" styleClass="gridHeader">
	   <f:param value="#{msgrules.ruleLogic.ruleName}"/>
	</h:outputFormat>
	<h:panelGrid columns="2" styleClass="editPaneHeader" 
		columnClasses="labelColumn, textColumn">
		<h:outputText value="#{msgs.ruleNamePrompt}"/>
		<h:outputText id="rulename" value="#{msgrules.ruleLogic.ruleName}"/>
		
		<h:outputText value="#{msgs.ruleSeqPrompt}"/>
		<h:outputText id="ruleseq" value="#{msgrules.ruleLogic.ruleSeq}"/>
		
		<h:outputText value="#{msgs.statusIdPrompt}"/>
		<h:outputText id="statusid" value="#{msgrules.ruleLogic.statusIdDesc}"/>
		
		<h:outputText value="#{msgs.startTimePrompt}"/>
		<h:outputText id="starttime" value="#{msgrules.ruleLogic.startTime}">
			<f:convertDateTime pattern="MM/dd/yyyy kk:mm"/>
		</h:outputText>
		
		<h:outputText value="#{msgs.mailTypePrompt}"/>
		<h:outputText id="mailtype" value="#{msgrules.ruleLogic.mailType}"/>
		
		<h:outputText value="#{msgs.ruleCategoryPrompt}"/>
		<h:outputText id="rulecategory" value="#{msgrules.ruleLogic.ruleCategoryDesc}"/>
		
		<h:outputText value="#{msgs.ruleTypePrompt}"/>
		<h:outputText id="ruletype" value="#{msgrules.ruleLogic.ruleType}"/>
	</h:panelGrid>
	<h:dataTable value="#{msgrules.msgActions}" var="msgaction" id="jsftable"
			styleClass="jsfDataTable"
            headerClass="dataTableHeader"
            footerClass="dataTableFooter"
            rowClasses="oddRows, evenRows">
        <h:column>
           <f:facet name="header">
              <h:outputText value="#{msgs.deleteColumnHeader}"/>
           </f:facet>
           <h:selectBooleanCheckbox value="#{msgaction.markedForDeletion}" 
              onclick="submit()"/>
        </h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.actionSeqHeader}"/> 
			</f:facet>
			<h:inputText id="actionseq" value="#{msgaction.actionSeq}"
				required="true">
				<f:validateLongRange minimum="0" maximum="100"/>
			</h:inputText>
        </h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.actionIdHeader}"/> 
			</f:facet>
			<h:selectOneMenu id="actionid" value="#{msgaction.actionId}"
				onchange="submit()" required="true">
				<f:selectItems value="#{dynacodes.actionIdItems}"/>
			</h:selectOneMenu>
        </h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.clientIdHeader}"/>
			</f:facet>
			<h:selectOneMenu id="clientid" value="#{msgaction.clientId}">
				<f:selectItem itemLabel="" itemValue=""/>
				<f:selectItems value="#{dynacodes.clientIdItems}"/>
				<f:converter converterId="NullableStringConverter"/>
			</h:selectOneMenu>
        </h:column>
        
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.startDateHeader}"/> 
			</f:facet>
       		<h:inputText id="startdate" value="#{msgaction.startDate}"
				size="10" maxlength="10" required="true"
				onclick="displayDP('#{msgrules.msgActions.rowIndex}');">
				<f:convertDateTime pattern="MM/dd/yyyy" type="date"/>
			</h:inputText>
        </h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.startHourHeader}"/>
			</f:facet>
			<h:selectOneMenu id="starthour" value="#{msgaction.startHour}">
				<f:selectItems value="#{codes.hourAmPmItems}"/>
			</h:selectOneMenu>
        </h:column>
        
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.statusIdHeader}"/> 
			</f:facet>
	        <h:selectOneMenu id="statusid" value="#{msgaction.statusId}"
	        	required="true">
				<f:selectItems value="#{codes.simpleStatusIdItems}"/>
			</h:selectOneMenu>
	    </h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msgs.dataTypeValuesHeader}"/> 
			</f:facet>
	        <h:selectManyListbox id="datatypevalues1" value="#{msgaction.dataTypeValuesUI}"
	        	size="2"
	        	rendered="#{msgaction.hasDataTypeValues && msgaction.isDataTypeEmailAddress}">
				<f:selectItems value="#{msgaction.dataTypeValuesList}"/>
			</h:selectManyListbox>
	        <h:selectOneMenu id="datatypevalues2" value="#{msgaction.dataTypeValues}"
	        	rendered="#{msgaction.hasDataTypeValues && msgaction.isDataTypeNotEmailAddress}">
				<f:selectItems value="#{msgaction.dataTypeValuesList}"/>
			</h:selectOneMenu>
	        <h:outputText value="#{msgaction.dataTypeValues}" 
	        	rendered="#{!msgaction.hasDataTypeValues}"/>
	    </h:column>
    </h:dataTable>
    <f:verbatim><p/></f:verbatim>
    <h:panelGroup>
	    <h:commandButton value="#{msgs.deleteButtonText}" title="Delete selected rows"
	   		action="#{msgrules.deleteMsgActions}" 
	   		disabled="#{not msgrules.anyMsgActionsMarkedForDeletion}"
	   		onclick="javascript:return confirmDelete();"/>
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.refreshFromDB}" title="Refresh from database"
		   action="#{msgrules.refreshMsgActions}"/>
		<f:verbatim>&nbsp;</f:verbatim>
		<h:commandButton value="#{msgs.addNewButtonText}" title="Add a new row"
		   action="#{msgrules.addMsgAction}"/>
    </h:panelGroup>
    <f:verbatim><p/></f:verbatim>
	<h:outputText value="#{msgs[msgrules.testResult]}" rendered="#{msgrules.testResult != null}"
		styleClass="errorMessage" id="testResult"/>
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{msgrules.saveMsgActions}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{msgrules.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

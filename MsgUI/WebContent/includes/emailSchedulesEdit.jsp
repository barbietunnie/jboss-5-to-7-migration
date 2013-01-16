<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h:panelGrid columns="1" styleClass="gettingStartedContent">
	<h:messages styleClass="errors" layout="list" rendered="#{debug.showMessages}"/>
	<h:outputFormat value="#{msgs.mailingListSchedulesLabel}" styleClass="gridHeader">
	   <f:param value="#{emailtemplates.emailTemplate.listId}"/>
	</h:outputFormat>
	<h:panelGrid columns="3" styleClass="editPaneHeader" 
		columnClasses="promptColumn, inputColumn, messageColumn">
		<h:outputText value="#{msgs.templateIdPrompt}"/>
		<h:inputText id="tmpltid" value="#{emailtemplates.emailTemplate.templateId}"
			label="#{msgs.templateIdPrompt}" maxlength="26" size="26"
			disabled="true">
			<f:validateLength minimum="1" maximum="26"/>
		</h:inputText>
		<h:message for="tmpltid" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.startTimePrompt}"/>
		<h:panelGroup>
		<h:outputText value="#{msgs.hoursPrompt}"/>
		<h:selectOneMenu id="starttime" required="true" 
			value="#{emailtemplates.emailTemplate.schedulesBlob.startHour}"
			label="#{msgs.scheduledStartTimePrompt}" >
			<f:selectItems value="#{codes.hoursOfDayItems}"/>
		</h:selectOneMenu>
		<f:verbatim>&nbsp;&nbsp;</f:verbatim>
		<h:outputText value="#{msgs.minutesPrompt}"/>
		<h:selectOneMenu id="startminute" required="true" 
			value="#{emailtemplates.emailTemplate.schedulesBlob.startMinute}">
			<f:selectItems value="#{codes.minuteItems}"/>
		</h:selectOneMenu>
		</h:panelGroup>
		<h:message for="starttime" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.weeklySelectionsPrompt}"/>
		<h:selectManyCheckbox value="#{emailtemplates.emailTemplate.schedulesBlob.weekly}" 
			id="weekly" label="#{msgs.weeklySelectionsPrompt}">
			<f:selectItems value="#{codes.daysOfTheWeekItems}"/>
		</h:selectManyCheckbox>
		<h:message for="weekly" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.biweeklySelectionsPrompt}"/>
		<h:selectManyCheckbox value="#{emailtemplates.emailTemplate.schedulesBlob.biweekly}" 
			id="biweekly" label="#{msgs.biweeklySelectionsPrompt}">
			<f:selectItems value="#{codes.daysOfTheWeekItems}"/>
		</h:selectManyCheckbox>
		<h:message for="biweekly" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.monthlySelectionsPrompt}"/>
		<h:panelGroup>
		<h:selectManyCheckbox value="#{emailtemplates.emailTemplate.schedulesBlob.monthly}" 
			id="monthly" label="#{msgs.monthlySelectionsPrompt}">
			<f:selectItems value="#{codes.daysOfMonthItems}"/>
		</h:selectManyCheckbox>
		<f:verbatim><p></f:verbatim>
		<h:outputText value="#{msgs.monthEndDayPrompt}"/>
		<h:selectBooleanCheckbox value="#{emailtemplates.emailTemplate.schedulesBlob.endOfMonth}">
		</h:selectBooleanCheckbox>
		<f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>
		<h:outputText value="#{msgs.monthEndMinus1DayPrompt}"/>
		<h:selectBooleanCheckbox value="#{emailtemplates.emailTemplate.schedulesBlob.eomMinus1Day}">
		</h:selectBooleanCheckbox>
		<f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>
		<h:outputText value="#{msgs.monthEndMinus2DayPrompt}"/>
		<h:selectBooleanCheckbox value="#{emailtemplates.emailTemplate.schedulesBlob.eomMinus2Day}">
		</h:selectBooleanCheckbox>
		</h:panelGroup>
		<h:message for="monthly" styleClass="errorMessage"/>
		
		<h:outputText value="#{msgs.selectDatesPrompt}"/>
		<h:panelGroup>
		<h:dataTable value="#{emailtemplates.dateList}" var="wrapper" id="wrapper"
			style="width: auto; border: none;">
			<h:column>
				<c:set var="idx" value="#{emailtemplates.dateList.rowIndex}" scope="page"/>
				<h:outputText value="Date: #{emailtemplates.dateList.rowIndex}: "/>
			</h:column>
			<h:column>
				<h:inputText id="dateitem" maxlength="10" size="10"
					value="#{wrapper.date}" validator="#{emailtemplates.checkDate}"
					onclick="displayDatePicker(this.name);">
					<f:convertDateTime pattern="MM/dd/yyyy" type="date"/>
				</h:inputText>
				<h:message for="dateitem" styleClass="errorMessage"/>
			</h:column>
		</h:dataTable>
		</h:panelGroup>
		<h:message for="wrapper" styleClass="errorMessage"/>
		
	</h:panelGrid>
	<h:outputText value="#{msgs[emailtemplates.testResult]}"
		rendered="#{emailtemplates.testResult != null}" styleClass="errorMessage"
		id="testResult" />
	<f:verbatim><p/></f:verbatim>
	<h:panelGrid columns="2" styleClass="commandBar"
		columnClasses="alignLeft70, alignRight30">
		<h:panelGroup>
			<h:commandButton value="#{msgs.submitButtonText}" title="Submit changes"
				action="#{emailtemplates.saveSchedules}"
				onclick="javascript:return confirmSubmit();" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton type="reset" value="#{msgs.resetButtonText}"
				title="Reset" />
			<f:verbatim>&nbsp;</f:verbatim>
			<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel changes"
				immediate="true" action="#{emailtemplates.cancelEdit}" />
		</h:panelGroup>
		<h:panelGroup>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:panelGroup>
	</h:panelGrid>
</h:panelGrid>

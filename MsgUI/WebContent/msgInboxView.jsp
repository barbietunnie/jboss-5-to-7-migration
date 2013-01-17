<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<f:view>
	<head>
	<link href="styles.css" rel="stylesheet" type="text/css" />
	<title><h:outputText value="#{msgs.emailDisplayPageTitle}" /></title>
	</head>
	<body><div align="center">
	<h:form id="inboxview">
	<h:panelGrid columns="1" styleClass="headerMenuContent">
    <c:import url="includes/gettingStartedHeader.jsp"/>
	<h:panelGroup>
		<h:messages styleClass="errors" layout="list"
			rendered="#{debug.showMessages}" />
		<h:panelGrid columns="2" styleClass="commandBar" 
			columnClasses="alignLeft50, alignRight50">
			<h:panelGroup style="text-align: left;">
				<h:commandButton value="#{msgs.deleteButton}" title="Delete message"
					action="#{msgfolder.deleteMessage}"
					onclick="javascript:return confirmSubmit();"/>
				<f:verbatim>&nbsp;</f:verbatim>
				<h:commandButton value="#{msgs.replyButton}" title="Reply message"
					action="#{msgfolder.replyMessage}"/>
				<f:verbatim>&nbsp;</f:verbatim>
				<h:commandButton value="#{msgs.forwardButton}" title="Forward message"
					action="#{msgfolder.forwardMessage}" />
				<f:verbatim rendered="#{msgfolder.message.closedStatus}">&nbsp;</f:verbatim>
				<h:commandButton value="#{msgs.openButton}" title="Open this message"
					action="#{msgfolder.openMessage}" 
					rendered="#{msgfolder.message.closedStatus}"
					onclick="javascript:return confirmOpen();"/>
			</h:panelGroup>
			<h:panelGroup style="align: right; text-align: right;">
				<h:outputText value="#{msgs.showFullHeaderPrompt}"/>
				<h:selectBooleanCheckbox value="#{msgfolder.message.showAllHeaders}"
					onchange="submit()"/>
				<f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>
				<h:outputText value="#{msgs.showRawMessagePrompt}"/>
				<h:selectBooleanCheckbox value="#{msgfolder.message.showRawMessage}"
					onchange="submit()"/>
			</h:panelGroup>
		</h:panelGrid>
		<f:verbatim>
			<p />
		</f:verbatim>
		<h:panelGrid columns="2" styleClass="smtpHeaders" 
			columnClasses="smtpLabelColumn, smtpTextColumn"
			rendered="#{not msgfolder.message.showAllHeaders}">
			
			<h:outputText value="#{msgs.fromAddressPrompt}" />
			<h:outputText value="#{msgfolder.message.fromAddress}"/>

			<h:outputText value="#{msgs.toAddressPrompt}" />
			<h:outputText value="#{msgfolder.message.toAddress}"/>

			<h:outputText value="#{msgs.ccAddressPrompt}" />
			<h:outputText value="#{msgfolder.message.ccAddress}" />

			<h:outputText value="#{msgs.msgSubjectPrompt}" />
			<h:outputText value="#{msgfolder.message.msgSubject}" />

			<h:outputText value="#{msgs.receivedDatePrompt}" />
			<h:outputText value="#{msgfolder.message.receivedDate}">
				<f:convertDateTime dateStyle="default"/>
			</h:outputText>

			<h:outputText value="#{msgs.finalRecipientPrompt}" 
				rendered="#{msgfolder.rfcFields != null}" />
			<h:outputText value="#{msgfolder.rfcFields.finalRcpt}" 
				rendered="#{msgfolder.rfcFields != null}" />
			
			<h:outputText value="#{msgs.siteIdPrompt}" 
				rendered="#{msgfolder.message.clientId != null}" />
			<h:outputText value="#{msgfolder.message.clientId}" 
				rendered="#{msgfolder.message.clientId != null}" />
			
			<h:outputText value="#{msgs.ruleNamePrompt}" />
			<h:outputText value="#{msgfolder.message.ruleName}" />

			<h:outputText value="#{msgs.bodyContentTypePrompt}" />
			<h:outputText value="#{msgfolder.message.bodyContentType}" />
		</h:panelGrid>
		
		<h:dataTable value="#{msgfolder.message.msgHeaders}" var="hdr" 
   			styleClass="smtpHeaders"
   			columnClasses="smtpLabelColumn, smtpTextColumn"
   			rendered="#{msgfolder.message.showAllHeaders}">
		   <h:column>
		      <h:outputText value="#{hdr.headerName}:"/>
		   </h:column>
		   <h:column>
		      <h:outputText value="#{hdr.headerValue}"/>
		   </h:column>
		</h:dataTable>
		
		<f:verbatim><br/></f:verbatim>
		<h:panelGrid columns="1" styleClass="smtpBody">
			<h:outputText value="#{msgfolder.message.displayBody}" escape="false" 
				rendered="#{not msgfolder.message.showRawMessage}"/>
			<h:outputText value="#{msgfolder.message.rawMessage}" escape="false" 
				rendered="#{msgfolder.message.showRawMessage}"/>
		</h:panelGrid>
		<h:panelGrid columns="1" styleClass="fullWidth">
			<h:panelGroup rendered="#{msgfolder.message.hasAttachments}">
				<f:verbatim><br></f:verbatim>
				<h:panelGrid columns="1" bgcolor="#D4EAF2" styleClass="fullWidth">
					<h:outputText value="#{msgs.attachmentsPrompt}"
						style="color: #2F4F4F; font-size: 1.2em;"/>
				</h:panelGrid>
				<h:dataTable value="#{msgfolder.message.attachments}" var="attch"
					style="width: 100%; background: white; border: thin solid LightGray;">
					<h:column>
						<h:graphicImage value="/images/clip_1.gif" style="border: 0px"
							title="attachment"/>
						<h:outputLink value="file"
							style="color: darkblue; font-size: 1em; font-weight: bold;">
							<f:param name="id" value="#{attch.msgId}"/>
							<f:param name="depth" value="#{attch.attchmntDepth}"/>
							<f:param name="seq" value="#{attch.attchmntSeq}"/>
							<h:outputText value="#{attch.attchmntName}"/>
						</h:outputLink>
						<f:verbatim>&nbsp;&nbsp;</f:verbatim>
						<h:outputText value="#{msgs.sizePrompt}"/>
						<h:outputText value="(#{attch.sizeAsString})"/>
					</h:column>
				</h:dataTable>
			</h:panelGroup>
			<h:panelGroup rendered="#{msgfolder.messageThreads != null}">
				<f:verbatim><br></f:verbatim>
				<h:panelGrid columns="1" bgcolor="#D4EAF2" styleClass="fullWidth">
					<h:outputText value="#{msgs.messageThreadsPrompt}"
						style="color: #2F4F4F; font-size: 1.2em;"/>
				</h:panelGrid>
				<h:dataTable value="#{msgfolder.messageThreads}" var="thread"
					style="width: 100%; background: white; border: thin solid LightGray;">
					<%-- h:column>
						<h:outputText value="#{msgs.previousThreadPrompt}" style="font-weight: bold;"
							rendered="#{msgfolder.message.msgId > thread.msgId}"/>
						<h:outputText value="#{msgs.nextThreadPrompt}" style="font-weight: bold;"
							rendered="#{msgfolder.message.msgId < thread.msgId}"/>
						<h:outputText value="#{msgs.currentThreadPrompt}" style="font-weight: bold;"
							rendered="#{msgfolder.message.msgId == thread.msgId}"/>
					</h:column --%>
					<h:column>
						<h:outputText value="#{thread.levelPrefix}" escape="false"
							style="font-size: 1.0em; font-weight: bold; font-family: monospace;"/>
						<h:graphicImage value="/images/unopened.gif"
							rendered="#{msgfolder.message.msgId != thread.msgId}"/>
						<h:commandLink action="#{msgfolder.viewThread}"
							rendered="#{msgfolder.message.msgId != thread.msgId}"
							value=" #{thread.msgSubject}"
							style="color: darkblue; font-size: 1em; font-weight: bold;">
							<f:param name="msgThreadId" value="#{thread.msgId}" />
						</h:commandLink>
						<h:graphicImage value="/images/opened.gif"
							rendered="#{msgfolder.message.msgId == thread.msgId}"/>
						<h:outputText value=" #{thread.msgSubject}"
							rendered="#{msgfolder.message.msgId == thread.msgId}"
							style="color: darkgray; font-size: 1em; font-weight: bold;"/>
						<h:outputText value=" #{msgs.newLabelText}" 
							style="color: green; font-size: 1em; font-weight: bold;"
							rendered="#{thread.readCount<=0}"/>
					</h:column>
					<h:column>
					<h:outputText value="#{thread.ruleName}"/>
					</h:column>
					<h:column>
					<h:outputText value="#{thread.receivedTime}"/>
					</h:column>
				</h:dataTable>
			</h:panelGroup>
		</h:panelGrid>
		<h:panelGrid columns="2" rendered="#{not msgfolder.message.closedStatus}"
			styleClass="commandBar" columnClasses="alignLeft30, alignRight70">
			<h:panelGroup style="text-align: left;">
				<h:commandButton value="#{msgs.closeButton}" title="Close this message"
					action="#{msgfolder.closeMessage}" 
					onclick="javascript:return confirmClose();"/>
				<f:verbatim>&nbsp;</f:verbatim>
				<h:commandButton value="#{msgs.closeThreadButton}" title="Close entire thread"
					action="#{msgfolder.closeThread}" 
					onclick="javascript:return confirmClose();"/>
				<f:verbatim>&nbsp;</f:verbatim>
				<h:commandButton value="#{msgs.backButtonText}" title="Go back to List"
					action="#{msgfolder.cancelSend}"/>
			</h:panelGroup>
			<h:panelGroup style="text-align: right;">
				<h:selectOneMenu id="newrulename" value="#{msgfolder.message.ruleName}">
					<f:selectItems value="#{dynacodes.builtinRuleNameItems}"/>
					<f:selectItems value="#{dynacodes.customRuleNameItems}"/>
				</h:selectOneMenu>
				<f:verbatim>&nbsp;</f:verbatim>
				<h:commandButton value="#{msgs.reassignRuleButton}" title="Reassign to a new Rule"
					action="#{msgfolder.reassignRule}" 
					disabled="#{msgfolder.message.ruleName == 'SEND_MAIL' || msgfolder.message.ruleName == 'BROADCAST'}"
					onclick="javascript:return confirmReassignRule('#{msgfolder.message.ruleName}');"/>
			</h:panelGroup>
		</h:panelGrid>
		<h:panelGrid columns="2" rendered="#{msgfolder.message.closedStatus}"
			styleClass="commandBar" columnClasses="alignLeft30, alignRight70">
			<h:panelGroup style="text-align: left;">
				<h:commandButton value="#{msgs.backButtonText}" title="Go back to List"
					action="#{msgfolder.cancelSend}"/>
			</h:panelGroup>
			<h:panelGroup style="text-align: right;">
				<f:verbatim>&nbsp;</f:verbatim>
			</h:panelGroup>
		</h:panelGrid>
	</h:panelGroup>
	<c:import url="includes/gettingStartedFooter.jsp"/>
	</h:panelGrid>
	</h:form>
	</div></body>
<script type="text/javascript">
// <!--
function confirmDelete() {
	varText = "<h:outputText value='#{msgs.confirmDeleteText}'/>";
	return confirm(varText);
}
function confirmSubmit() {
	varText = "<h:outputText value='#{msgs.confirmSubmitText}'/>";
	return confirm(varText);
}
function confirmClose() {
	varText = "<h:outputText value='#{msgs.confirmCloseText}'/>";
	return confirm(varText);
}
function confirmOpen() {
	varText = "<h:outputText value='#{msgs.confirmOpenText}'/>";
	return confirm(varText);
}
function confirmReassignRule(oldRuleName) {
	var newRuleName = document.getElementById('inboxview:newrulename').value;
	if (oldRuleName == newRuleName) {
		alert("<h:outputText value='#{msgs.selectDifferentRuleText}'/>");
		return false;
	}
	varText = "<h:outputText value='#{msgs.confirmReassignRuleText}'/>";
	return confirm(varText);
}
// -->
</script>
</f:view>
</html>
<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<f:view>
	<head>
	<link href="styles.css" rel="stylesheet" type="text/css" />
	<script type="text/javascript" src="includes/insertAtCursor.js"></script>
	<script type="text/javascript" src="includes/whizzywig.js"></script>
	<title><h:outputText value="#{msgs.composeMailingListEmailPageTitle}" /></title>
	</head>
	<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>
	<%-- body onLoad="setSelRange(document.getElementById('mlstcomp:bodytext'),0,0);" --%>
	<body onLoad="document.getElementById('mlstcomp:subject').focus();">
	<div align="center">
	<h:form id="mlstcomp">
	<h:panelGrid columns="1" styleClass="headerMenuContent">
    <c:import url="includes/gettingStartedHeader.jsp"/>
	<h:panelGroup>
		<h:messages styleClass="errors" layout="list"
			rendered="#{debug.showMessages}" />
		<h:outputText value="#{maillistcomp.actionFailure}"
			rendered="#{maillistcomp.actionFailure != null}" styleClass="errorMessage"/>
		<h:outputFormat value="#{msgs.composeForListLink}" styleClass="gridHeader">
		   <f:param value=""/>
		</h:outputFormat>
		<h:panelGrid columns="2" styleClass="commandBar" 
			columnClasses="alignLeft50, alignRight50">
			<h:panelGroup>
				<h:commandButton value="#{msgs.sendButtonText}" title="Send message"
					action="#{maillistcomp.sendMessage}"/>
				<f:verbatim>&nbsp;</f:verbatim>
				<h:commandButton value="#{msgs.backButtonText}" title="Cancel"
					immediate="true" action="#{maillistcomp.cancelSend}" />
			</h:panelGroup>
			<h:panelGroup style="text-align: right;">
				<f:verbatim>&nbsp;</f:verbatim>
				<h:selectOneMenu id="template" value="#{maillistcomp.templateId}"
					binding="#{maillistcomp.templateIdInput}">
					<f:selectItems value="#{dynacodes.emailTemplateIdItems}"/>
				</h:selectOneMenu>
				<f:verbatim>&nbsp;</f:verbatim>
				<h:commandButton value="#{msgs.copyFromTemplateButtonText}"
					title="Copy from Template"
					immediate="true" action="#{maillistcomp.copyFromTemplate}" />
			</h:panelGroup>
		</h:panelGrid>
		<f:verbatim><p/></f:verbatim>
		<h:panelGrid columns="3" styleClass="smtpHeaders" 
			columnClasses="promptColumn, inputColumn, messageColumn">
			
			<h:outputText value="#{msgs.mailingListPrompt}" />
			<h:selectOneMenu value="#{maillistcomp.listId}"
				id="listid" label="#{msgs.mailingListPrompt}" required="true">
				<f:selectItems value="#{dynacodes.mailingListIdItems}"/>
			</h:selectOneMenu>
			<h:message for="listid" styleClass="errorMessage"/>

			<h:outputText value="#{msgs.msgSubjectPrompt}" />
			<h:inputText value="#{maillistcomp.msgSubject}" 
				id="subject" label="#{msgs.msgSubjectPrompt}"
				required="true" maxlength="255" size="100"/>
			<h:message for="subject" styleClass="errorMessage"/>
		</h:panelGrid>

		<h:panelGrid columns="1" styleClass="fullWidth">
			<f:verbatim><br></f:verbatim>
			<h:commandButton value="#{msgs.attachFileButtonText}" title="Attach files"
				action="#{maillistcomp.attachFiles}"/>
			<h:dataTable value="#{maillistcomp.uploads}" var="upload"
				style="width: auto; border: none;">
				<h:column>
					<h:graphicImage value="/images/clip_1.gif" style="border: 0px"
						title="attachment"/>
					<h:outputText value="#{upload.fileName}"/>
					<f:verbatim>&nbsp;&nbsp;</f:verbatim>
					<h:outputText value="#{msgs.sizePrompt}"/>
					<h:outputText value="(#{upload.fileSize})"/>
					<h:commandLink action="#{maillistcomp.removeUploadFile}"
						style="color: darkblue; font-size: 1em; font-weight: bold;">
						<f:param name="seq" value="#{upload.sessionSeq}"/>
						<f:param name="name" value="#{upload.fileName}"/>
						<h:outputText value="[#{msgs.removeLinkText}]"/>
					</h:commandLink>
				</h:column>
			</h:dataTable>
		</h:panelGrid>
		
		<f:verbatim><br/></f:verbatim>
		<h:panelGrid columns="1" styleClass="smtpBody">
			<h:panelGrid columns="1" styleClass="commandBar">
				<h:panelGroup style="text-align: left;">
					<h:outputText value="#{msgs.variableNamePrompt}" styleClass="columnHeader"/>
					<h:selectOneMenu id="vname" required="true" >
						<f:selectItems value="#{dynacodes.emailVariableNameItems}"/>
						<f:selectItems value="#{dynacodes.globalVariableNameItems}"/>
					</h:selectOneMenu>
					<f:verbatim>
					<input type="button" value="Insert Selected Variable"
						onclick="insertFieldToBody('mlstcomp:vname');"/>
						<%--onclick="insertIntoBody('mlstcomp:bodytext', 'mlstcomp:vname');"/ --%>
					</f:verbatim>
				</h:panelGroup>
			</h:panelGrid>
			
			<h:inputTextarea value="#{maillistcomp.msgBody}"
				id="bodytext" rows="25" style="width: 100%;"/>
			<f:verbatim>
			<script type="text/javascript">
				buttonPath = "images/whizzywigbuttons/";
				makeWhizzyWig("mlstcomp:bodytext", "all");
			</script>
			</f:verbatim>
			
			<h:panelGrid columns="2" styleClass="commandBar" 
				columnClasses="eightyPercent,twentyPercent">
			<h:panelGroup style="text-align: left;">
				<h:outputText value="#{msgs.embedEmailIdPrompt}"/>
				<%--h:selectBooleanCheckbox value="#{maillistcomp.embedEmailId}"/ --%>
				<h:selectOneMenu value="#{maillistcomp.embedEmailId}">
					<f:selectItems value="#{codes.yorNItems}"/>
					<f:selectItem itemLabel="System default" itemValue=" "/>
				</h:selectOneMenu>
				<f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>
				<h:outputText value="#{msgs.deliveryOptionPrompt}"/>
				<h:selectOneMenu id="dlvropt" value="#{maillistcomp.deliveryOption}">
					<f:selectItems value="#{codes.mailingListDeliveryOptionItems}"/>
				</h:selectOneMenu>
			</h:panelGroup>
			<h:panelGroup style="text-align: right;">
				<h:outputText value="#{msgs.htmlContentPrompt}"/>
				<h:selectBooleanCheckbox value="#{maillistcomp.html}"/>
				<f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>
				<h:commandButton value="#{msgs.mailingListPreviewLink}"
					action="#{maillistcomp.previewMsgBody}"
					title="Preview Rendered Message"/>
			</h:panelGroup>
			</h:panelGrid>
		</h:panelGrid>
	</h:panelGroup>
	<c:import url="includes/gettingStartedFooter.jsp"/>
	</h:panelGrid>
	</h:form>
	</div></body>
</f:view>
</html>
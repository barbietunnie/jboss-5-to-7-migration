<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<f:view>
	<head>
	<link href="styles.css" rel="stylesheet" type="text/css" />
	<title><h:outputText value="#{msgs.sendEmailReplyPageTitle}" /></title>
	</head>
	<body onLoad="setSelRange(document.getElementById('msgsend:bodytext'),0,0);">
	<div align="center">
	<h:form id="msgsend">
	<h:panelGrid columns="1" styleClass="headerMenuContent">
	<c:import url="includes/gettingStartedHeader.jsp"/>
	<h:panelGroup>
		<h:messages styleClass="errors" layout="list"
			rendered="#{debug.showMessages}" />
		<h:panelGrid columns="2" styleClass="commandBar" 
			columnClasses="alignLeft70, alignRight30">
			<h:panelGroup>
				<h:commandButton value="#{msgs.sendButtonText}" title="Send message"
					action="#{msgfolder.sendMessage}"/>
				<f:verbatim>&nbsp;</f:verbatim>
				<h:commandButton value="#{msgs.cancelButtonText}" title="Cancel"
					immediate="true" action="#{msgfolder.cancelSend}" />
			</h:panelGroup>
			<h:panelGroup style="align: right; text-align: right;">
				<f:verbatim>&nbsp;</f:verbatim>
			</h:panelGroup>
		</h:panelGrid>
		<f:verbatim>
			<p />
		</f:verbatim>
		<h:panelGrid columns="3" styleClass="smtpHeaders" 
			columnClasses="promptColumn, inputColumn, messageColumn">
			
			<h:outputText value="#{msgs.fromAddressPrompt}" />
			<h:inputText value="#{msgfolder.replyMessageVo.composeFromAddress}"
				label="#{msgs.fromAddressPrompt}" required="true" maxlength="255"
				id="fromaddr" binding="#{msgfolder.fromAddrInput}" 
				size="100" validator="#{msgfolder.validateFromAddress}">
			</h:inputText>
			<h:message for="fromaddr" styleClass="errorMessage"/>

			<h:outputText value="#{msgs.toAddressPrompt}" />
			<h:inputText value="#{msgfolder.replyMessageVo.composeToAddress}"
				label="#{msgs.toAddressPrompt}" required="true" maxlength="255"
				id="toaddr" binding="#{msgfolder.toAddrInput}" 
				size="100" validator="#{msgfolder.validateToAddress}">
			</h:inputText>
			<h:message for="toaddr" styleClass="errorMessage"/>

			<h:outputText value="#{msgs.msgSubjectPrompt}" />
			<h:inputText value="#{msgfolder.replyMessageVo.msgSubject}" 
				id="subject" label="#{msgs.msgSubjectPrompt}"
				required="true" maxlength="255" size="100"/>
			<h:message for="subject" styleClass="errorMessage"/>
		</h:panelGrid>

		<h:panelGrid columns="1" styleClass="fullWidth"
			rendered="#{msgfolder.replyMessageVo!=null && msgfolder.replyMessageVo.isReply}">
			<f:verbatim><br></f:verbatim>
			<h:commandButton value="#{msgs.attachFileButtonText}" title="Attach files"
				action="#{msgfolder.attachFiles}"/>
			<h:dataTable value="#{msgfolder.uploads}" var="upload"
				style="width: auto; border: none;">
				<h:column>
					<h:graphicImage value="/images/clip_1.gif" style="border: 0px"
						title="attachment"/>
					<h:outputText value="#{upload.fileName}"/>
					<f:verbatim>&nbsp;&nbsp;</f:verbatim>
					<h:outputText value="#{msgs.sizePrompt}"/>
					<h:outputText value="(#{upload.fileSize})"/>
					<h:commandLink action="#{msgfolder.removeUploadFile}"
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
			<h:inputTextarea value="#{msgfolder.replyMessageVo.msgBody}"
				id="bodytext" rows="#{msgfolder.replyMessageVo.rows}" style="width: 100%;"/>
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
// set cursor position to the beginning of the textarea
function setSelRange(inputEl, selStart, selEnd) { 
 if (inputEl.setSelectionRange) {
  inputEl.focus();
  inputEl.setSelectionRange(selStart, selEnd); 
 } else if (inputEl.createTextRange) {
  var range = inputEl.createTextRange(); 
  range.collapse(true);
  range.moveEnd('character', selEnd); 
  range.moveStart('character', selStart); 
  range.select();
 }
}
// insert text at the cursor position
function insertAtCursor(myField, myValue) {
	//IE support
	if (document.selection) {
		myField.focus();
		sel = document.selection.createRange();
		sel.text = myValue;
	}
	//MOZILLA/NETSCAPE support
	else if (myField.selectionStart || myField.selectionStart == '0') {
		var startPos = myField.selectionStart;
		var endPos = myField.selectionEnd;
		myField.value = myField.value.substring(0, startPos)
			+ myValue
			+ myField.value.substring(endPos, myField.value.length);
	} else {
		myField.value += myValue;
	}
}
/*
I was looking for a solution to the problem that in Firefox 0.9, with wordpress 1.2,
the view changes to the top of the post entry field and ended up here.

The cursor does not jump, but the scrollbar at the text entry field goes right to the
top and I have to scroll down to see where I was at.

I'd appreciate a solution.

Answer:
Use save and reset the scrollTop property of the textarea object.
*/

/*
Maybe it's trivial, but I've added a bit to this snippet such that the added text
becomes highlighted / selected once inserted. :)
*/
function insertAtCursor(myField, myValue)
{
	//IE support
	if (document.selection)
	{
		myField.focus();
		sel = document.selection.createRange();
		sel.text = myValue;
		sel.moveStart('character', -myValue.length);
		sel.select();
	}
	//MOZILLA/NETSCAPE support
	else if (myField.selectionStart || myField.selectionStart == '0')
	{
		var startPos = myField.selectionStart;
		var endPos = myField.selectionEnd;
		myField.value =
			myField.value.substring(0, startPos)
			+ myValue
			+ myField.value.substring(endPos, myField.value.length);
		myField.selectionStart = startPos;
		myField.selectionEnd = startPos + myValue.length;
	}
	//Anyone else.
	else
	{
		myField.value += myValue;
	}
}

// calling the function
//insertAtCursor(document.formName.fieldName, 'this value');
// -->
</script>
</f:view>
</html>
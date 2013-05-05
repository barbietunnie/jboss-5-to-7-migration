// <!--
function confirmDelete() {
//	varText = "<h:outputText value='#{msgs.confirmDeleteText}'/>";
	varText = "Are you sure to delete selected item(s)?";
	return confirm(varText);
}
function confirmSubmit() {
//	varText = "<h:outputText value='#{msgs.confirmSubmitText}'/>";
	varText = "Are you sure to submit the changes?";
	return confirm(varText);
}
function confirmClose() {
//	varText = "<h:outputText value='#{msgs.confirmCloseText}'/>";
	varText = "Do you want to close the message?";
	return confirm(varText);
}
function confirmOpen() {
//	varText = "<h:outputText value='#{msgs.confirmOpenText}'/>";
	varText = "Do you want to open the message?";
	return confirm(varText);
}
// -->
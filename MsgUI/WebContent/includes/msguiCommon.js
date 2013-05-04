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
// -->
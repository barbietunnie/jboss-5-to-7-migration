<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<f:view>
	<head>
	<link href="styles.css" rel="stylesheet" type="text/css" />
	<title><h:outputText value="#{msgs.manageEmailCorrespondence}" /></title>
	<script type="text/javascript">
	function changeAllCheckBoxes(theCheckbox) {
		var myForm = theCheckbox.form;
		var i = 0;
		for(i = 0; i < myForm.length; i++) {
      		if(myForm[i].type == 'checkbox' && myForm[i].name.indexOf('lnchkbox') > 0) {
	  			myForm[i].checked = theCheckbox.checked;
	  		}
	  	}
	}
	</script>
	</head>
	<body><div align="center">
	<h:form id="msgform">
	<h:panelGrid columns="1" styleClass="headerMenuContent" style="border: none; padding: 0px;">
	<h:panelGrid columns="2" styleClass="headerMenuContent"
                   columnClasses="menuColumn, contentColumn">
	<f:facet name="header">
	<f:subview id="header">
        <c:import url="includes/gettingStartedHeader.jsp"/>
    </f:subview>
	</f:facet>
	
	<h:panelGroup>
		<c:import url="/includes/simpleMailTrackingMenu.jsp"/>
	</h:panelGroup>
	
	<h:panelGroup>
		<h:messages styleClass="errors" layout="list"
			rendered="#{debug.showMessages}" />
		<h:panelGrid columns="2" styleClass="commandBar" 
			columnClasses="alignLeft70, alignRight30">
			<h:panelGroup>
				<h:commandButton value="#{msgs.deleteButton}"
					title="Delete selected messages" action="#{msgfolder.deleteMessages}"
					onclick="javascript:return confirmDelete();" />
				<f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>
				<h:outputText value="#{msgs.markAsPrompt}" style="color: darkblue; font-weight: bold;"/>
				<f:verbatim>&nbsp;</f:verbatim>
				<h:commandLink value="#{msgs.markAsReadLink}"
					title="Mark as read" action="#{msgfolder.markAsRead}"/>
				<f:verbatim>&nbsp;&nbsp;</f:verbatim>
				<h:commandLink value="#{msgs.markAsUnreadLink}"
					title="Mark as unread" action="#{msgfolder.markAsUnread}"/>
				<f:verbatim>&nbsp;&nbsp;</f:verbatim>
				<h:commandLink value="#{msgs.markAsFlaggedLink}"
					title="Mark as flagged" action="#{msgfolder.markAsFlagged}"/>
				<f:verbatim>&nbsp;&nbsp;</f:verbatim>
				<h:commandLink value="#{msgs.markAsUnflaggedLink}"
					title="Mark as unflagged" action="#{msgfolder.markAsUnflagged}"/>
			</h:panelGroup>
			<h:panelGroup>
				<h:commandLink value="#{msgs.refreshLinkText}"
					title="Refresh from database" action="#{msgfolder.refreshClicked}"/>
			</h:panelGroup>
		</h:panelGrid>
		<h:dataTable id="msgrow"
			binding="#{msgfolder.dataTable}" value="#{msgfolder.all}" var="mail"
			rows="#{msgfolder.searchFieldVo.pageSize}"
			styleClass="jsfDataTable" rowClasses="oddRows, evenRows"
			headerClass="dataTableHeader" footerClass="dataTableFooter"
			columnClasses="twoPercent, twentyPercent, fiftyPercent, tenPercent, tenPercent, fivePercent, twoPercent">

			<h:column>
				<f:facet name="header" >
					<h:selectBooleanCheckbox id="checkAll" value="#{msgfolder.checkAll}"
						onclick="javascript:changeAllCheckBoxes(this)"/>
				</f:facet>
				<h:selectBooleanCheckbox value="#{mail.markedForDeletion}"
					disabled="#{not mail.editable}" id="lnchkbox"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.fromAddressHeader}"/>
				</f:facet>
				<div class="cellHeight">
				<h:graphicImage value="/images/flag.gif" style="border: 0px"
					rendered="#{mail.flaggedMsg}" title="Message Flagged"/>
				<h:graphicImage value="/images/replied.gif" style="border: 0px"
					rendered="#{mail.replyCount>0}" title="Message Replied"/>
				<h:graphicImage value="/images/forwarded.gif" style="border: 0px"
					rendered="#{mail.replyCount<=0 && mail.forwardCount>0}" 
					title="Message Forwarded"/>
				<h:outputText value="#{mail.fromDisplayName}"/>
				</div>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.msgSubjectHeader}"/>
				</f:facet>
				<div class="cellHeight">
				<h:graphicImage value="/images/clip_1.gif" style="border: 0px"
					rendered="#{mail.hasAttachments}" title="Message has attachment(s)"/>
				<h:graphicImage value="/images/space.gif" style="border: 0px"
					rendered="#{!mail.hasAttachments}" height="1" width="16"/>
				<h:commandLink action="#{msgfolder.viewMessage}">
					<h:outputText value="#{mail.msgSubject==null?'null':mail.msgSubject}"
						title="Click to View" style="font-weight: bold;"
						rendered="#{mail.readCount<=0}"/>
					<h:outputText value="#{mail.msgSubject==null?'null':mail.msgSubject}"
						title="Click to View" style="font-weight: normal;"
						rendered="#{mail.readCount>0}"/>
					<f:param name="msgId" value="#{mail.msgId}" />
				</h:commandLink>
				</div>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.ruleNameHeader}"/>
				</f:facet>
				<h:outputText value="#{mail.ruleName}" style="font-size: 0.8em; color: darkblue;"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.receivedDateHeader}"/>
				</f:facet>
				<h:outputText value="#{mail.receivedDate}">
					<f:convertDateTime pattern="MM/dd_HH:mm"/>
				</h:outputText>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.msgSizeHeader}"/>
				</f:facet>
				<h:outputText value="#{mail.size}" />
			</h:column>
			<h:column>
				<h:graphicImage value="/images/received.gif" style="border: 0px"
					rendered="#{mail.receivedMsg}" title="Received"/>
				<h:graphicImage value="/images/sent.gif" style="border: 0px"
					rendered="#{!mail.receivedMsg}" title="Sent"/>
			</h:column>
			
			<f:facet name="footer">
            <h:panelGroup>
			<h:panelGrid columns="2" styleClass="fullWidth"
				columnClasses="alignLeft50, alignRight50">
				<h:panelGroup>
				<h:outputText value="#{msgs.footerViewText} "/>
                <h:commandLink value="#{msgs.allLinkText}" action="#{msgfolder.viewAll}"/>
				<f:verbatim>&nbsp;</f:verbatim>
                <h:commandLink value="#{msgs.unreadLinkText}" action="#{msgfolder.viewUnread}"/>
                <f:verbatim>&nbsp;</f:verbatim>
                <h:commandLink value="#{msgs.readLinkText}" action="#{msgfolder.viewRead}"/>
                <f:verbatim>&nbsp;</f:verbatim>
                <h:commandLink value="#{msgs.flaggedLinkText}" action="#{msgfolder.viewFlagged}"/>
				</h:panelGroup>
				<h:panelGroup>
            	<h:outputText value="#{msgfolder.dataTable.first + 1}"
            		style="font-weight: bold;"/>
            	<h:outputText value=" - "/>
				<h:outputText value="#{msgfolder.lastPageRow}"
					style="font-weight: bold;" />
				<h:outputText value=" of #{msgfolder.dataTable.rowCount}" 
            		style="font-weight: bold;"/>
            	<f:verbatim>&nbsp;&nbsp;</f:verbatim>
                <h:commandLink value="#{msgs.firstLinkText}" action="#{msgfolder.pageFirst}"
                	actionListener="#{msgfolder.pagingActionFired}"
                    disabled="#{msgfolder.dataTable.first == 0}" id="pagefrst"/>
                <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
                <h:commandLink value="#{msgs.prevLinkText}" action="#{msgfolder.pagePrevious}"
                	actionListener="#{msgfolder.pagingActionFired}"
                    disabled="#{msgfolder.dataTable.first == 0}" id="pageprev"/>
                <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
                <h:commandLink value="#{msgs.nextLinkText}" action="#{msgfolder.pageNext}"
                	actionListener="#{msgfolder.pagingActionFired}"
                    disabled="#{msgfolder.dataTable.first + msgfolder.dataTable.rows
                        >= msgfolder.dataTable.rowCount}" id="pagenext"/>
                <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
                <h:commandLink value="#{msgs.lastLinkText}" action="#{msgfolder.pageLast}"
                	actionListener="#{msgfolder.pagingActionFired}"
                    disabled="#{msgfolder.dataTable.first + msgfolder.dataTable.rows
                        >= msgfolder.dataTable.rowCount}" id="pagelast"/>
                </h:panelGroup>
                </h:panelGrid>
            </h:panelGroup>
			</f:facet>
		</h:dataTable>
	</h:panelGroup>
	</h:panelGrid>
	<c:import url="includes/gettingStartedFooter.jsp"/>
	</h:panelGrid>
	</h:form>
	</div></body>
</f:view>
</html>

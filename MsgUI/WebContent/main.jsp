<html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<f:view>
	<head>
	<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
	<META HTTP-EQUIV="Expires" CONTENT="-1">
	<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
	<link href="/MsgUI/styles.css" rel="stylesheet" type="text/css" />
	<title><h:outputText value="#{msgs.mainPageTitle}" /></title>
	</head>
	<body>
	<div align="center">
	<jsp:useBean id="loginBean"
		class="com.legacytojava.msgui.bean.LoginBean" scope="request" />
	
	<h:form id="main">
	<h:panelGrid columns="1" styleClass="headerMenuContent">
	<c:import url="includes/gettingStartedHeader.jsp"/>
		<c:set var="linenbr" value="1"/>
		<h:panelGrid columns="2" styleClass="menuPane" columnClasses="menuPaneColumn">
		<h:panelGroup>
			<h:outputText value="#{msgs.configServerConnectionsLabel}"
				styleClass="menuGroupTitle" rendered="#{login.isAdmin}"/>
			<h:panelGrid columns="1" styleClass="menuCell" columnClasses="menuCellColumn"
				 rendered="#{login.isAdmin}">
				<h:panelGroup styleClass="menuLinkText">
					<c:if test="${loginBean.isAdmin == true}">
						<c:out value="${linenbr}"/>
						<c:set var="linenbr" value="${linenbr + 1}"/>
					</c:if>
					<h:outputLink value="admin/configureSiteProfiles.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.configureSiteProfiles}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText">
					<c:if test="${loginBean.isAdmin == true}">
						<c:out value="${linenbr}"/>
						<c:set var="linenbr" value="${linenbr + 1}"/>
					</c:if>
					<h:outputLink value="admin/configureMailboxes.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.configureMailboxes}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText">
					<c:if test="${loginBean.isAdmin == true}">
						<c:out value="${linenbr}"/>
						<c:set var="linenbr" value="${linenbr + 1}"/>
					</c:if>
					<h:outputLink value="admin/configureSmtpServers.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.configureSmtpServers}"/>
					</h:outputLink>
				</h:panelGroup>
			</h:panelGrid>
			<f:verbatim><p/></f:verbatim>
			<h:outputText value="#{msgs.configRulesAndActionsLabel}"
				styleClass="menuGroupTitle"/>
			<h:panelGrid columns="1" styleClass="menuCell" columnClasses="menuCellColumn">
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="customizeBuiltInRules.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.customizeBuiltInRules}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="configureCustomRules.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.configureCustomRules}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="maintainActionDetails.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.maintainActionDetails}"/>
					</h:outputLink>
				</h:panelGroup>
			</h:panelGrid>
			<f:verbatim><p/></f:verbatim>
			<h:outputText value="#{msgs.configMailingListAndTemplates}"
				styleClass="menuGroupTitle"/>
			<h:panelGrid columns="1" styleClass="menuCell" columnClasses="menuCellColumn">
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="configureMailingLists.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.configureMailingLists}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="upload/emailAddrAttachFile.faces" styleClass="menuLinkText">
						<f:param name="frompage" value="uploademails"/>
						<h:outputText value="#{msgs.uploadEmailAddrsToList}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="configureEmailVariables.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.configureEmailVariables}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="configureEmailTemplates.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.configureEmailTemplates}"/>
					</h:outputLink>
				</h:panelGroup>
			</h:panelGrid>
		</h:panelGroup>
		<h:panelGroup>
			<h:outputText value="#{msgs.emailManagementStudioLabel}"
				styleClass="menuGroupTitle"/>
			<h:panelGrid columns="1" styleClass="menuCell" columnClasses="menuCellColumn">
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="msgInboxList.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.manageEmailCorrespondence}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="mailingListCompose.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.composeForListLink}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="emailAddressList.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.manageEmailAddresses}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="customersList.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.manageCustomerInformation}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="broadcastsList.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.viewBroadcastMessages}"/>
					</h:outputLink>
				</h:panelGroup>
			</h:panelGrid>
			<f:verbatim><p/></f:verbatim>
			<h:outputText value="#{msgs.siteUserManagermentLabel}"
				styleClass="menuGroupTitle"/>
			<h:panelGrid columns="1" styleClass="menuCell" columnClasses="menuCellColumn">
				<h:panelGroup styleClass="menuLinkText" rendered="#{login.isAdmin}">
					<c:if test="${loginBean.isAdmin == true}">
						<c:out value="${linenbr}"/>
						<c:set var="linenbr" value="${linenbr + 1}"/>
					</c:if>
					<h:outputLink value="admin/manageUserAccounts.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.manageUserAccounts}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="changePassword.faces?frompage=main" styleClass="menuLinkText">
						<h:outputText value="#{msgs.changePassword}"/>
					</h:outputLink>
				</h:panelGroup>
				<h:panelGroup styleClass="menuLinkText"
					rendered="#{!login.isProductKeyValid}">
					<c:out value="${linenbr}"/>
					<c:set var="linenbr" value="${linenbr + 1}"/>
					<h:outputLink value="enterProductKey.faces?frompage=main" styleClass="menuLinkText"
						rendered="#{login.isTrialPeriodExpired}">
						<h:outputText value="#{msgs.trialPeriodExpiredPrompt}" style="color: red;"/>
					</h:outputLink>
					<h:outputLink value="enterProductKey.faces?frompage=main" styleClass="menuLinkText"
						rendered="#{!login.isTrialPeriodExpired}">
						<h:outputText value="#{msgs.enterProductKey}"/>
					</h:outputLink>
				</h:panelGroup>
			</h:panelGrid>
		</h:panelGroup>
		</h:panelGrid>
		<c:import url="includes/gettingStartedFooter.jsp"/>
	</h:panelGrid>
	</h:form>
	</div></body>
</f:view>
</html>
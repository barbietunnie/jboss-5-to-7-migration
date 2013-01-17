package com.legacytojava.msgui.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.legacytojava.message.dao.emailaddr.SubscriptionDao;
import com.legacytojava.message.dao.inbox.MsgInboxDao;
import com.legacytojava.message.dao.user.SessionUploadDao;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.HtmlTags;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.SessionUploadVo;
import com.legacytojava.message.vo.UserVo;
import com.legacytojava.message.vo.emailaddr.SubscriptionVo;
import com.legacytojava.msgui.bean.FileUploadForm;
import com.legacytojava.msgui.bean.MailingListComposeBean;
import com.legacytojava.msgui.bean.MsgInboxBean;
import com.legacytojava.msgui.filter.MultipartFilter;
import com.legacytojava.msgui.filter.SessionTimeoutFilter;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = -4905340132022275056L;
	static final Logger logger = Logger.getLogger(UploadServlet.class);
	
	private SessionUploadDao sessionUploadDao = null;
	private SubscriptionDao subscriptionDao = null;
	
	public void init() throws ServletException {
		ServletContext ctx = getServletContext();
		logger.info("init() - ServerInfo: " + ctx.getServerInfo() + ", Context Name: "
				+ ctx.getServletContextName());
		sessionUploadDao = (SessionUploadDao) SpringUtil.getWebAppContext(ctx).getBean(
				"sessionUploadDao");
		subscriptionDao = (SubscriptionDao) SpringUtil.getWebAppContext(ctx).getBean(
				"subscriptionDao");
		// initialize unread counts
		MsgInboxDao msgInboxDao = (MsgInboxDao) SpringUtil.getWebAppContext(ctx).getBean(
				"msgInboxDao");
		int initInboxCount = msgInboxDao.resetInboxUnreadCount();
		int initSentCount = msgInboxDao.resetSentUnreadCount();
		logger.info("init() - InboxUnreadCount = " + initInboxCount + ", SentUnreadCount = "
				+ initSentCount);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String fromPage = request.getParameter("frompage");
		
		// Do nothing, just show the form
		forward(request, response, fromPage);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("submit");
		String fromPage = request.getParameter("frompage");
		if ("Cancel".equals(action) || "Done".equals(action)) {
			// submitted from "Cancel" or "Done" button, return to calling page.
			redirect(request, response, action, fromPage);
			return;
		}
		// Prepare upload bean
		FileUploadForm uploadForm = new FileUploadForm();
		
		if ("Import From List".equals(action)) {
			importEmailsFromList(request, uploadForm);
		}
		else { // upload file(s)
			// calculate total file size
			long size = getTotalFileSize(request, uploadForm);
			if (MultipartFilter.getTotalFileSize() > 0 && size > MultipartFilter.getTotalFileSize()) {
				uploadForm.setError("size", "Total file size exceeded maximum amount of "
						+ MultipartFilter.getTotalFileSize() + " bytes.");
			}
			// Process request
			for (int i = 1; i <= 10; i++) {
				size += process(request, uploadForm, i);
			}
		}
		// Store bean in the request
		request.setAttribute("uploadForm", uploadForm);
		// Post back
		if (uploadForm.hasErrors() || uploadForm.hasMessages()) {
			// redisplay the same page
			forward(request, response, fromPage);
		}
		else { // redirect to previous page (msgInboxSend.jsp or ...)
			redirect(request, response, action, fromPage);
		}
	}
	
	private void redirect(HttpServletRequest request, HttpServletResponse response, String action,
			String fromPage) throws IOException {
		String targetPage = "/msgInboxList.faces";
		if ("msgreply".equals(fromPage)) {
			targetPage = "/msgInboxSend.faces";
		}
		else if ("mailinglist".equals(fromPage)) {
			targetPage = "/mailingListCompose.faces";
		}
		else if ("uploademails".equals(fromPage)) {
			targetPage = "/main.faces";
		}
		
		if ("uploademails".equals(fromPage) || "Cancel".equals(action)) {
			redirectOnly(request, response, targetPage);
		}
		else {
			redirectWithUpload(request, response, targetPage);
		}
	}

	private void redirectOnly(HttpServletRequest request, HttpServletResponse response,
			String targetPage) throws IOException {
		String url = request.getContextPath() + targetPage;
		response.sendRedirect(response.encodeRedirectURL(url));
	}
	
	private void redirectWithUpload(HttpServletRequest request, HttpServletResponse response,
			String targetPage) throws IOException {
		String fromPage = request.getParameter("frompage");
		FacesContext facesContext = FacesUtil.getFacesContext(request, response);
		if ("mailinglist".equals(fromPage)) { // from mailing list compose
			// 1) retrieve MailingListComposeBean instance from faces context
			MailingListComposeBean bean = (MailingListComposeBean) facesContext.getELContext()
					.getELResolver().getValue(facesContext.getELContext(), null, "maillistcomp");
			// 2) populate "uploads" list that contains uploaded files
			bean.retrieveUploadFiles();
		}
		else { // from message reply
			// 1) retrieve MsgInboxBean instance from faces context
			MsgInboxBean bean = (MsgInboxBean) facesContext.getELContext().getELResolver()
					.getValue(facesContext.getELContext(), null, "msgfolder");
			// 2) populate "uploads" list that contains uploaded files
			bean.retrieveUploadFiles();
		}
		response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + targetPage));
	}
	
	private long getTotalFileSize(HttpServletRequest request, FileUploadForm uploadForm) {
		long size = 0;
		for (int i = 0; i <= 10; i++) {
			Object fileObject = request.getAttribute("file" + i);
			if (fileObject != null && fileObject instanceof FileItem) {
				FileItem item = (FileItem) fileObject;
				size += item.getSize();
				uploadForm.setPathes("file" + i, item.getName());
			}
		}
		return size;
	}
	
	private long process(HttpServletRequest request, FileUploadForm uploadForm, int fileSeq) {
	    // Validate file
	    Object fileObject = request.getAttribute("file" + fileSeq);
	    long fileSize = 0;
	    if (fileObject == null) {
	    	if (fileSeq == 1) {
		        // No file uploaded
		        uploadForm.setError("file" + fileSeq, "Please select a file to upload.");
	    	}
	    } else if (fileObject instanceof FileUploadException) {
	        // File upload failed
	        FileUploadException fileUploadException = (FileUploadException) fileObject;
	        uploadForm.setError("file" + fileSeq, fileUploadException.getMessage());
	    }
	    // If there are no errors, proceed with writing file
	    if (!uploadForm.hasErrors() && fileObject != null) {
	        FileItem fileItem = (FileItem) fileObject;
	        fileSize = fileItem.getSize();
	        try {
	        	if ("uploademails".equals(request.getParameter("frompage"))) {
	        		// upload to mailing list subscription table
	        		uploadEmailsToList(request, uploadForm, fileSeq, fileItem);
	        	}
	        	else {
	        		uploadToSessionTable(request, uploadForm, fileSeq, fileItem);
	        	}
	        } catch (Exception e) {
	            // Can be thrown from uniqueFile() and FileItem#write()
	            uploadForm.setError("file" + fileSeq, e.getMessage());
	            logger.error("Exception caught", e);
	        }
	    }
	    return fileSize;
	}
	
	private void uploadToSessionTable(HttpServletRequest request, FileUploadForm uploadForm,
			int fileSeq, FileItem fileItem) throws IOException {
        String fileName = FilenameUtils.getName(fileItem.getName());
        String contentType = fileItem.getContentType();
    	String sessionId = request.getRequestedSessionId();
    	SessionUploadVo sessVo = new SessionUploadVo();
    	sessVo.setSessionSeq(fileSeq); // ignored by insertLast() method
    	sessVo.setSessionId(sessionId);
    	HttpSession session = request.getSession();
        UserVo userVo = (UserVo) session.getAttribute(SessionTimeoutFilter.USER_VO_ID);
        if (userVo != null) {
        	sessVo.setUserId(userVo.getUserId());
        }
        else {
        	logger.warn("process() - UserVo not found in httpSession!");
        }
    	sessVo.setFileName(fileName);
    	sessVo.setContentType(contentType);
    	InputStream is = fileItem.getInputStream();
    	sessVo.setSessionValue(IOUtils.toByteArray(is));
        // Write uploaded file to database
    	int rowsInserted = sessionUploadDao.insertLast(sessVo);
    	logger.info("process() - rows inserted: " + rowsInserted);
        uploadForm.setMessage(fileName, "File succesfully uploaded.");		
	}
	
	private void uploadEmailsToList(HttpServletRequest request, FileUploadForm uploadForm,
			int fileSeq, FileItem fileItem) throws IOException {
		String listId = request.getParameter("listid");
		if (StringUtil.isEmpty(listId)) {
			logger.error("uploadEmailsToList() - listid parameter was not valued.");
			uploadForm.setError("Contact Support!", "'listid' request parameter was not valued");
			return;
		}
		InputStream is = fileItem.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		int rowsAdded = 0;
		int rowsInvalid = 0;
		String addr = null;
		while ((addr = br.readLine()) != null) {
			if (EmailAddrUtil.isRemoteEmailAddress(addr)) {
				rowsAdded += subscriptionDao.subscribe(addr, listId);
			}
			else {
				rowsInvalid ++;
				if (HtmlTags.isHTML(addr)) {
					addr = "Address contains HTML tags, suppressed.";
				}
				uploadForm.setError("Invalid Address " + rowsInvalid, addr);
			}
		}
		uploadForm.setMessage("Number of email addresses added to list", rowsAdded + "");
		logger.info("uploadEmailsToList() - number of addrs added to " + listId + ": " + rowsAdded);
		if (rowsInvalid > 0) {
			//uploadForm.setError("Number of invalid addresses", rowsInvalid + "");
			logger.warn("uploadEmailsToList() - number of invalid addrs: " + rowsInvalid);
		}
	}
	
	private void importEmailsFromList(HttpServletRequest request, FileUploadForm uploadForm)
			throws IOException {
		String listId = request.getParameter("listid");
		if (StringUtil.isEmpty(listId)) {
			logger.error("uploadEmailsToList() - listid parameter was not valued.");
			uploadForm.setError("Contact Support!", "'listid' request parameter was not valued");
			return;
		}
		String fromListId = request.getParameter("fromlistid");
		if (StringUtil.isEmpty(fromListId)) {
			logger.error("uploadEmailsToList() - fromlistid parameter was not valued.");
			uploadForm.setError("Contact Support!", "'fromlistid' request parameter was not valued");
			return;
		}
		if (listId.equals(fromListId)) {
			logger.error("uploadEmailsToList() - fromlistid and listid are same.");
			uploadForm.setError("Import from list", "should be different from \"Import to\" list.");
			return;
		}
		int rowsSubed = 0;
		List<SubscriptionVo> fromList = subscriptionDao.getByListId(fromListId);
		for (int i = 0; i < fromList.size(); i++) {
			SubscriptionVo subed = fromList.get(i);
			rowsSubed += subscriptionDao.subscribe(subed.getEmailAddrId(), listId);
		}
		uploadForm.setMessage("Number of email addresses imported", "" + rowsSubed);
		logger.info("importEmailsFromList() - number of addresses imported from " + fromListId
				+ " to " + listId + ": " + rowsSubed);
	}
	
	private void forward(HttpServletRequest request, HttpServletResponse response, String fromPage)
			throws ServletException, IOException {
		if ("uploademails".equals(fromPage)) {
			request.getRequestDispatcher("emailAddrAttachFile.jsp").forward(request, response);
		}
		else {
			request.getRequestDispatcher("msgInboxAttachFiles.jsp").forward(request, response);
		}
	}
}

package com.es.ejb.mailinglist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMultipart;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import jpa.util.StringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.es.ejb.ws.vo.MailingListVo;
import com.es.tomee.util.TomeeCtxUtil;

@Path("/msgapi/mailinglist")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class MailingListRS {
	static final Logger logger = Logger.getLogger(MailingListRS.class);

	@javax.ejb.EJB
	private MailingListLocal maillist;
	
	public MailingListRS() {
		TomeeCtxUtil.registerBeanUtilsConverters();
	}
	
	MailingListLocal getMailingListLocal() throws NamingException {
		if (maillist == null) {
			javax.naming.Context context = TomeeCtxUtil.getLocalContext();
			maillist = (MailingListLocal) context.lookup("MailingListLocal");
		}
		return maillist;
	}

	@Path("/list")
	@GET
	public Response getAllMailingLists() {
		logger.info("Entering getAllMailingLists() method...");
		try {
			List<jpa.model.MailingList> list = getMailingListLocal().getActiveLists();
			logger.info("Number of lists: " + list.size());
			List<MailingListVo> volist = new ArrayList<MailingListVo>();
			for (jpa.model.MailingList ml : list) {
				MailingListVo vo = new MailingListVo();
				try {
					BeanUtils.copyProperties(vo, ml);
					String listaddr = ml.getAcctUserName() + "@" + ml.getSenderData().getDomainName();
					vo.setListEmailAddr(listaddr);
					volist.add(vo);
				}
				catch (Exception e) {
					throw new RuntimeException("Failed to copy properties", e);
				}
			}
			GenericEntity<List<MailingListVo>> entity = new GenericEntity<List<MailingListVo>>(volist) {};
			return Response.ok(entity).build();
		}
		catch (NamingException e) {
			throw new WebApplicationException(Response.serverError().build());
		}
	}
	
	@Path("/update/{listId}")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public Response updateList(@Context Request req, @PathParam("listId") String listId, MailingListVo vo) {
		logger.info("Entering updateList() method...  list to update: " + listId);
		logger.info("Input MailingListVo:" + StringUtil.prettyPrint(vo));
		if (!req.getMethod().equals("POST")) {
			Response.noContent().build();
		}
		Response.ResponseBuilder rb = req.evaluatePreconditions();
		if (rb != null) {
			rb.build();
        }
		try {
			jpa.model.MailingList ml = getMailingListLocal().getByListId(listId);
			vo.setListId(listId); // make sure listId is not changed
			try {
				BeanUtils.copyProperties(ml, vo);
				getMailingListLocal().update(ml);
				logger.info("MailingList updated: " + StringUtil.prettyPrint(ml, 1));
				return Response.ok("Success").build();
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to copy properties", e);
			}
		}
		catch (NamingException e) {
			return Response.serverError().build();
		}
	}

	@Path("/get1")
	@GET
	public String getWithContext1(@Context UriInfo uri) {
		MultivaluedMap<String, String> pathParams = uri.getPathParameters();
		MultivaluedMap<String, String> queryParams = uri.getQueryParameters();
		for (Iterator<String> it = pathParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Path Key: " + key + ", Values: " + pathParams.get(key));
		}
		for (Iterator<String> it = queryParams.keySet().iterator(); it
				.hasNext();) {
			String key = it.next();
			logger.info("Query Key: " + key + ", Values: " + queryParams.get(key));
		}
		return null;
	}
	
	@Path("/get2")
	@GET
	public String getWithContext2(@Context HttpHeaders hh) {
		MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
		for (Iterator<String> it = headerParams.keySet().iterator(); it
				.hasNext();) {
			String key = it.next();
			logger.info("Header Key: " + key + ", Values: " + headerParams.get(key));
		}
		Map<String, Cookie> cookieParams = hh.getCookies();
		for (Iterator<String> it = cookieParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Cookie Key: " + key + ", Values: " + cookieParams.get(key));
		}
		return null;
	}
	
	@Path("/update1")
	@POST
	@Consumes("multipart/related")
	public String doPost1(MimeMultipart mimeMultipartData) {
		return null;
	}
	
	@Path("/update2")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public String doPost2(@FormParam("sbsrId") String sbsrId) {
		return null;
	}

	@Path("/update3")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public String doPost3(MultivaluedMap<String, String> formParams) {
		for (Iterator<String> it = formParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Form Key: " + key + ", Values: " + formParams.get(key));
		}
		// store the message
		return null;
	}

	@Path("/get3")
	@GET
	@Produces("application/xml")
	public String doGet3(@Context Request req, @Context UriInfo ui) {
		ui.getRequestUri();
		if (req.getMethod().equals("GET")) {
			Response.ResponseBuilder rb = req.evaluatePreconditions();
			if (rb != null) {
                throw new WebApplicationException(rb.build());
            }
		}
		return null;
	}


}

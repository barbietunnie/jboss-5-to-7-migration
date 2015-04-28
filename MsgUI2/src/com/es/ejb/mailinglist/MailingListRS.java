package com.es.ejb.mailinglist;

import java.util.Iterator;
import java.util.Map;

import javax.mail.internet.MimeMultipart;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

@Path("/msgapi/mailinglist")
public class MailingListRS {
	static final Logger logger = Logger.getLogger(MailingListRS.class);

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

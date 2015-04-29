package com.es.ejb.subscriber;

import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import jpa.model.SubscriberData;
import jpa.model.Subscription;
import jpa.util.ExceptionUtil;
import jpa.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
import org.apache.log4j.Logger;

import com.es.jaxrs.common.ErrorResponse;
import com.es.tomee.util.TomeeCtxUtil;

@Path("/msgapi/subscription")
@Produces({"text/xml", "application/json"})
public class SubscriptionRS {
	static final Logger logger = Logger.getLogger(SubscriptionRS.class);

	@javax.ejb.EJB
	private SubscriberLocal subscriber;
	
	public SubscriptionRS() {
		TomeeCtxUtil.registerBeanUtilsConverters();
	}
	
	SubscriberLocal getSubscriberLocal() throws NamingException {
		if (subscriber == null) {
			javax.naming.Context context = TomeeCtxUtil.getLocalContext();
			subscriber = (SubscriberLocal) context.lookup("subscriberLocal");
		}
		return subscriber;
	}
	
	@Path("/getSubscriber")
	@GET
	@Produces({"application/json", "application/xml"})
	public Response getSubscriberByEmailAddress(@QueryParam("emailAddr") String emailAddr, @Context HttpHeaders hh) {
		try {
			SubscriberData sd = getSubscriberLocal().getSubscriberByEmailAddress(emailAddr);
			if (sd != null) {
				logger.info(StringUtil.prettyPrint(sd, 1));
				return Response.ok(sd).build();
			}
			else {
				boolean acceptXml = false;
				MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
				if (headerParams.containsKey("Accept")) {
					if (StringUtils.containsIgnoreCase(headerParams.getFirst("Accept"), "/xml")) {
						acceptXml = true;
					}
				}
				String rsp;
				if (acceptXml) {
					rsp = "<subscriberData>Subscriber not found</subscriberData>";
				}
				else {
					rsp = "{\"subscriberData\": \"not found\"}";
				}
				return Response.ok(rsp).build();
			}
		}
		catch (NamingException e) {
			// produce HTTP 500 Internal Server Error
			throw new WebApplicationException(Response.serverError().build());
		}
	}
	
	@Path("/subscriber/{emailAddr}")
	@GET
	@Produces("application/xml")
	public SubscriberData getSubscriberByEmailAddressAsXml(@PathParam("emailAddr") String emailAddr) {
		try {
			SubscriberData sd = getSubscriberLocal().getSubscriberByEmailAddress(emailAddr);
			if (sd != null) {
				logger.info(StringUtil.prettyPrint(sd,1));
				return sd;
			}
			else {
				throw new WebApplicationException(Response.ok("<subscriberData>Subscriber not found</subscriberData>").build());
			}
		}
		catch (NamingException e) {
			throw new WebApplicationException(Response.serverError().build());
		}
	}

	@Path("/getsbsr")
	@GET
	@Produces({"application/xml", "application/json"})
	public Response getAsXmlOrJson(@QueryParam("emailAddr") String emailAddr) {
		try {
			SubscriberData sd = getSubscriberLocal().getSubscriberByEmailAddress(emailAddr);
			if (sd != null) {
				return Response.ok(sd).build();
			}
			else {
				ErrorResponse er = new ErrorResponse();
				er.setHttpStatus(404);
				er.setErrorCode(102);
				er.setErrorMessage("Subscriber not found");
				ResponseBuilder rb = new ResponseBuilderImpl();
				rb.status(404);
				rb.entity(er);
				return rb.build();
			}
		}
		catch (NamingException e) {
			throw new WebApplicationException(Response.serverError().build());
		}

	}

	@Path("/subscribe")
    @PUT
	public Subscription subscribe(@QueryParam("emailAddr") String emailAddr, @QueryParam("listId") String listId) {
		try {
			Subscription sub = getSubscriberLocal().subscribe(emailAddr, listId);
			return sub;
		}
		catch (NamingException e) {
			throw new WebApplicationException(Response.serverError().build());
		}
		catch (Exception e) {
			Exception cause = ExceptionUtil.findRootCause(e);
			if (cause instanceof IllegalArgumentException) {
				throw new WebApplicationException(Response.ok(cause.getMessage()).build());
			}
			throw new WebApplicationException(Response.serverError().build());
		}
	}

	@Path("/unsubscribe")
	@PUT
	public Subscription unSubscribe(@QueryParam("emailAddr") String emailAddr, @QueryParam("listId") String listId) {
		try {
			Subscription sub = getSubscriberLocal().unSubscriber(emailAddr, listId);
			return sub;
		}
		catch (NamingException e) {
			throw new WebApplicationException(Response.serverError().build());
		}
		catch (Exception e) {
			Exception cause = ExceptionUtil.findRootCause(e);
			if (cause instanceof IllegalArgumentException) {
				throw new WebApplicationException(Response.ok(cause.getMessage()).build());
			}
			throw new WebApplicationException(Response.serverError().build());
		}
	}
	
	@Path("/update")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response update(@Context UriInfo ui, @Context HttpHeaders hh, MultivaluedMap<String, String> formParams) {
		// print out UriInfo
		MultivaluedMap<String, String> pathParams = ui.getPathParameters();
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		for (Iterator<String> it = pathParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Path Key: " + key + ", Values: " + pathParams.get(key));
		}
		for (Iterator<String> it = queryParams.keySet().iterator(); it
				.hasNext();) {
			String key = it.next();
			logger.info("Query Key: " + key + ", Values: " + queryParams.get(key));
		}
		
		// print out headers and cookies
		MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
		for (Iterator<String> it = headerParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Header Key: " + key + ", Values: " + headerParams.get(key));
		}
		Map<String, Cookie> cookieParams = hh.getCookies();
		for (Iterator<String> it = cookieParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Cookie Key: " + key + ", Values: " + cookieParams.get(key));
		}

		// print out Form Parameters
		for (Iterator<String> it = formParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Form Key: " + key + ", Values: " + formParams.get(key));
		}
		return Response.ok("Success").build();
	}

}

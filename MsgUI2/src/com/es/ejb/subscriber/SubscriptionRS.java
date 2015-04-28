package com.es.ejb.subscriber;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import jpa.model.SubscriberData;
import jpa.model.Subscription;
import jpa.util.StringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.es.tomee.util.TomeeCtxUtil;

@Path("/msgapi/subscription")
@Produces({"text/xml", "application/json"})
public class SubscriptionRS {
	static final Logger logger = Logger.getLogger(SubscriptionVo.class);

	@javax.ejb.EJB
	private SubscriberLocal subscriber;
	
	public SubscriptionRS() {
		TomeeCtxUtil.registerBeanUtilsConverters();
	}
	
	SubscriberLocal getSubscriberLocal() throws NamingException {
		if (subscriber == null) {
			Context context = TomeeCtxUtil.getLocalContext();
			subscriber = (SubscriberLocal) context.lookup("subscriberLocal");
		}
		return subscriber;
	}
	
	@Path("/getSubscriber")
	@GET
	@Produces("text/plain")
	public Response getSubscriberByEmailAddress(@QueryParam("emailAddr") String emailAddr) {
		try {
			SubscriberData sd = getSubscriberLocal().getSubscriberByEmailAddress(emailAddr);
			if (sd != null) {
				logger.info(StringUtil.prettyPrint(sd));
				return Response.ok(sd.getSubscriberId()).build();
				//return sd.getSubscriberId();
			}
			else {
				return Response.ok("Subscriber not found").build();
				//return "Failed to find the subscriber";
			}
		}
		catch (NamingException e) {
			//throw new RuntimeException("Failed to lookup subscriberLocal", e);
			return Response.serverError().build();
			//return "Failed to lookup";
		}
	}
	
	@Path("/subscriber/{sbsrId}")
	@GET
	@Produces("text/html")
	public String getSubscriberByEmailAddressAsXml(@PathParam("sbsrId") String sbsrId, @QueryParam("emailAddr") String emailAddr) {
		return null;
	}

	@GET
	@Produces({"application/xml", "application/json"})
	public String getAsXmlOrJson(@QueryParam("emailAddr") String emailAddr) {
		return null;
	}

	@Path("/subscribe")
    @PUT
	public SubscriptionVo subscribe(@QueryParam("emailAddr") String emailAddr, @QueryParam("listId") String listId) {
		try {
			Subscription sub = getSubscriberLocal().subscribe(emailAddr, listId);
			SubscriptionVo vo = new SubscriptionVo();
			try {
				BeanUtils.copyProperties(vo, sub);
				vo.setEmailAddrRowId(sub.getEmailAddr().getRowId());
				vo.setAddress(sub.getEmailAddr().getAddress());
				vo.setMailingListRowId(sub.getMailingList().getRowId());
				vo.setListId(sub.getMailingList().getListId());
				return vo;
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to copy properties", e);
			}
		}
		catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Path("/update")
	@POST
	@Consumes("text/plain")
	public SubscriptionVo unSubscribe(String emailAddr, String listId) {
		return null;
	}
	

}

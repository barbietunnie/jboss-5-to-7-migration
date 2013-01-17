package com.legacytojava.msgui.listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlInputSecret;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * This is a JSF Phase Listener, not a servlet listener.
 * Implement the POST-Redirect-GET pattern to turn POST request to GET request.
 */
public class PostRedirectGetListener implements PhaseListener {
	private static final long serialVersionUID = -3904347364541320388L;
	static final Logger logger = Logger.getLogger(PostRedirectGetListener.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
    private static final String ALL_FACES_MESSAGES_ID = "PostRedirectGetListener.allFacesMessages";

    /**
     * @see javax.faces.event.PhaseListener#getPhaseId()
     */
    public PhaseId getPhaseId() {
        // Only listen to the render response phase.
        return PhaseId.RENDER_RESPONSE;
    }

    /**
     * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
     */
    public void beforePhase(PhaseEvent event) {
		FacesContext facesContext = event.getFacesContext();
		HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext()
				.getRequest();
        if ("POST".equals(request.getMethod())) {
            // Save faces messages from POST request in session so that they'll
            // be available to the subsequent GET request.
            saveFacesMessages(facesContext);
            // Gather the POST request parameters
            Map<String, String> paramMap = facesContext.getExternalContext()
					.getRequestParameterMap();
			List<String> params = new ArrayList<String>();
			for (String paramKey : paramMap.keySet()) {
				UIComponent component = facesContext.getViewRoot().findComponent(paramKey);
				if (component instanceof UIInput && !(component instanceof HtmlInputSecret)) {
					// You may change this part if you want. This is done so,
					// because the requestParameterMap can contain more stuff
					// than only UIInput values, for example the invoked
					// UICommand element and the parent UIForm. Also prevent
					// values of HtmlInputSecret being passed to the GET!
					String paramname = paramKey.substring(paramKey.lastIndexOf(':') + 1);
					String paramvalue = paramMap.get(paramKey);
					params.add(paramname + "=" + paramvalue);
				}
			}
            // Build the GET request URL
            String url = facesContext.getApplication().getViewHandler()
                .getActionURL(facesContext, facesContext.getViewRoot().getViewId());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < params.size(); i++) {
                sb.append((i == 0 ? "?" : "&") + params.get(i));
            }
            url += sb.toString();
            // Redirect POST to GET
            try {
                facesContext.getExternalContext().redirect(url);
            }
            catch (IOException e) {
            	logger.error("IOException caught", e);
            }
        }
        else {
            // add saved faces messages back to the GET request
            restoreFacesMessages(facesContext);
        }
    }

    /**
     * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
     */
    public void afterPhase(PhaseEvent event) {
        // Do nothing.
    }

    /**
     * Save the faces messages from the facescontext in the session.
     */
    private void saveFacesMessages(FacesContext facesContext) {
		// Create a faces messages holder and save it to the session map.
		// The LinkedHashMap has precedence over HashMap, because in a
		// LinkedHashMap the FacesMessages will be kept in order, which
		// can be very useful for certain error and focus handling.
		Map<String, List<FacesMessage>> allFacesMessages = new LinkedHashMap<String, List<FacesMessage>>();
		facesContext.getExternalContext().getSessionMap().put(ALL_FACES_MESSAGES_ID,
				allFacesMessages);
		// Get all component ID's that has faces messages
		Iterator<String> clientIds = facesContext.getClientIdsWithMessages();
		while (clientIds.hasNext()) {
			// Get the component ID
			String clientId = clientIds.next();
			// Save client faces messages to the faces messages holder
			List<FacesMessage> clientFacesMessages = new ArrayList<FacesMessage>();
			allFacesMessages.put(clientId, clientFacesMessages);
			// Get all messages from the client and add them to the client's
			// faces message list
			Iterator<FacesMessage> facesMessages = facesContext.getMessages(clientId);
			while (facesMessages.hasNext()) {
				clientFacesMessages.add(facesMessages.next());
			}
		}
	}

    /**
	 * Restore the faces messages from the session and add them to the
	 * facescontext.
	 */
    @SuppressWarnings("unchecked")
    private void restoreFacesMessages(FacesContext facesContext) {
        // Remove the faces messages holder from the session map.
        Map<String, List<FacesMessage>> allFacesMessages = (Map<String, List<FacesMessage>>)
            facesContext.getExternalContext().getSessionMap().remove(ALL_FACES_MESSAGES_ID);
        if (allFacesMessages != null) {
            // Add the messages back to the facescontext.
            for (String clientId : allFacesMessages.keySet()) {
                List<FacesMessage> clientFacesMessages = allFacesMessages.get(clientId);
                for (FacesMessage clientFacesMessage : clientFacesMessages) {
                    facesContext.addMessage(clientId, clientFacesMessage);
                }
            }
        }
    }

}
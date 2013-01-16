package com.legacytojava.msgui.listener;

import java.io.File;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

/**
 * @author iapjkw
 *	add the following to web.xml
 <context-param>
        <param-name>imagesRoot</param-name>
        <param-value>/MsdUI/images/</param-value>
 </context-param>
 * and the following to faces-config.xml
 <lifecycle>
         <phase-listener>com.legacytojava.msgui.listener.ImagePhaseListener</phase-listener>
 </lifecycle>
 *
 * Now, if you have the image /MsgUI/images/img1.jpg you can render it via 
 * http://www.yourdomain.com/MsgUI/images/img1.jsf
 * Or access image from your page:
 * <img src="#{facesContext.externalContext.request.contextPath}/images/img1.jsf" alt="" />
 */
public class ImagePhaseListener implements PhaseListener {
	private static final long serialVersionUID = -8522140437752269661L;
	public final static String IMAGE_VIEW_ID = "images";
 
    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        String viewId = context.getViewRoot().getViewId();
 
        if (viewId.startsWith("/" + IMAGE_VIEW_ID + "/")) {
            String imageFileName = viewId.substring(viewId.lastIndexOf("/") + 1, viewId.lastIndexOf(".")) + ".jpg";
            String imagesRoot = context.getExternalContext().getInitParameter("imagesRoot");
            handleImageRequest(context, new File(imagesRoot + imageFileName));
        }
    }
 
    public void beforePhase(PhaseEvent event) {
        // Do nothing here…
    }
 
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
 
    private void handleImageRequest(FacesContext context, File imageFile) {
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        response.setContentType("image/jpeg");
        try {
            ImageIO.write(ImageIO.read(imageFile), "jpeg", response.getOutputStream());
        } catch (Exception exception) {
            // log this
        }
        context.responseComplete();
    }
 
}

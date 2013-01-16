<%-- Insert this snippet of code on top of the JSF file that receives the request --%>
<%
java.util.Enumeration<?> e = request.getParameterNames();
while (e.hasMoreElements()) {
	String n = (String) e.nextElement();
	String[] v = request.getParameterValues(n);
	for (int i=0; v!=null && i<v.length; i++) {
		java.util.logging.Logger.getLogger("com.legacytojava.jsp").info("name="+n+",value="+v[i]);
	}
}
 %>

<%-- Focus: Put it at the end of the code, straight before the </body>.
<f:verbatim>
    <script>
        // Execute on load.
        setFocus('</f:verbatim><h:outputText value="#{myBean.currentFocus}"/><f:verbatim>');
 
        // Set input focus.
        function setFocus(elId) {
            var el = document.getElementById(elId);
            if (el && el.focus) {
                el.focus();
            }
        }
    </script>
</f:verbatim>
--%>
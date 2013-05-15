package jpa.msgui.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import jpa.constant.Constants;
import jpa.dataloader.DataLoader;
import jpa.msgui.util.SpringUtil;
import jpa.service.SenderDataService;
import jpa.util.JpaUtil;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class DerbyInitServlet
 */
@WebServlet(name="DerbyInitServlet", urlPatterns="/DerbyInit/*", loadOnStartup=8)
public class DerbyInitServlet extends HttpServlet {
	private static final long serialVersionUID = 1810496150486989387L;
	static final Logger logger = Logger.getLogger(DerbyInitServlet.class);
	
    @Override
	public void init() throws ServletException {
		ServletContext ctx = getServletContext();
		logger.info("init() - ServerInfo: " + ctx.getServerInfo() + ", Context Path: "
				+ ctx.getContextPath());
		if (Constants.DB_PRODNAME_DERBY.equals(JpaUtil.getDBProductName())) {
			SenderDataService sender = (SenderDataService) SpringUtil.getWebAppContext().getBean("senderDataService");
			if (sender.getAll().isEmpty()) {
				logger.warn("Initializing Derby database and load all the tables...");
				// load initial data to tables
				DataLoader loader = new DataLoader();
				try {
					loader.loadAllTables();
				}
				catch (Exception e) {
					logger.error("Failed to load data to tables", e);
				}
			}
		}
	}
}

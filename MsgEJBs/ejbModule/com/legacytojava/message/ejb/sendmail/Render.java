package com.legacytojava.message.ejb.sendmail;

import java.text.ParseException;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bo.template.RenderBo;
import com.legacytojava.message.bo.template.RenderRequest;
import com.legacytojava.message.bo.template.RenderResponse;
import com.legacytojava.message.exception.DataValidationException;

/**
 * Session Bean implementation class Render
 */
@Stateless(mappedName = "ejb/Render")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(mappedName = "java:jboss/MessageDS", 
	name = "jdbc/msgdb_pool", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(RenderRemote.class)
@Local(RenderLocal.class)
public class Render implements RenderRemote, RenderLocal {
	protected static final Logger logger = Logger.getLogger(Render.class);
	@Resource
	SessionContext context;
	private RenderBo renderBo;
    /**
     * Default constructor. 
     */
    public Render() {
    	renderBo = (RenderBo)SpringUtil.getAppContext().getBean("renderBo");
    }

	public RenderResponse getRenderedEmail(RenderRequest req) throws AddressException,
			DataValidationException, ParseException {
		return renderBo.getRenderedEmail(req);
	}
}

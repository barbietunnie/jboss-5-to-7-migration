package jpa.service.message;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.message.MessageRendered;
import jpa.service.common.SenderDataService;
import jpa.service.common.SubscriberDataService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageRenderedService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageRenderedService implements java.io.Serializable {
	private static final long serialVersionUID = -1300601306632111600L;

	static Logger logger = Logger.getLogger(MessageRenderedService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	private MessageSourceService sourceService;
	@Autowired
	private TemplateDataService tmpltService;
	@Autowired
	private SenderDataService senderService;
	@Autowired
	private SubscriberDataService subscriberService;

	public MessageRendered getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageRendered t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			MessageRendered record = (MessageRendered) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageRendered getByPrimaryKey(int rowId) throws NoResultException {
		return getByRowId(rowId);
	}

	public MessageRendered getAllDataByPrimaryKey(int rowId) throws NoResultException {
		MessageRendered mr = getByRowId(rowId);
		try {
			mr.setMessageSource(sourceService.getByRowId(mr.getMessageSourceRowId()));
		}
		catch (NoResultException e) {}
		try {
			mr.setMessageTemplate(tmpltService.getByRowId(mr.getMessageTemplateRowId()));
		}
		catch (NoResultException e) {}
		if (mr.getSenderDataRowId()!=null) {
			try {
				mr.setSenderData(senderService.getByRowId(mr.getSenderDataRowId()));
			}
			catch (NoResultException e) {}
		}
		if (mr.getSubscriberDataRowId()!=null) {
			try {
				mr.setSubscriberData(subscriberService.getByRowId(mr.getSubscriberDataRowId()));
			}
			catch (NoResultException e) {}
		}
		//
		return mr;
	}
	
	public MessageRendered getFirstRecord() throws NoResultException {
		String sql = 
				"select t.* " +
				"from " +
					"Message_Rendered t " +
				" where t.Row_Id = (select min(t2.Row_Id) from Message_Rendered t2) ";
		try {
			Query query = em.createNativeQuery(sql, MessageRendered.MAPPING_MESSAGE_RENDERED);
			MessageRendered record = (MessageRendered) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageRendered getLastRecord() throws NoResultException {
		String sql = 
				"select t.* " +
				"from " +
					"Message_Rendered t " +
				" where t.Row_Id = (select max(t2.Row_Id) from Message_Rendered t2) ";
		try {
			Query query = em.createNativeQuery(sql, MessageRendered.MAPPING_MESSAGE_RENDERED);
			MessageRendered record = (MessageRendered) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageRendered getPrevoiusRecord(MessageRendered inbox) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageRendered t where t.rowId<:rowId order by t.rowId desc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", inbox.getRowId());
			MessageRendered record = (MessageRendered) query.setMaxResults(1).getSingleResult();
			return record;
		}
		finally {
		}
	}

	public MessageRendered getNextRecord(MessageRendered inbox) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"MessageRendered t where t.rowId>:rowId order by t.rowId asc ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", inbox.getRowId());
			MessageRendered record = (MessageRendered) query.setMaxResults(1).getSingleResult();
			return record;
		}
		finally {
		}
	}

	public void delete(MessageRendered rendered) {
		if (rendered == null) return;
		try {
			em.remove(rendered);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from MessageRendered t " +
				" where t.rowId=:rowId ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(MessageRendered rendered) {
		try {
			if (em.contains(rendered)) {
				em.persist(rendered);
			}
			else {
				em.merge(rendered);
			}
		}
		finally {
		}
	}

	public void insert(MessageRendered rendered) {
		try {
			em.persist(rendered);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

}

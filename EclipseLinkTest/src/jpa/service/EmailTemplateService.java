package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.EmailTemplate;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("emailTemplateService")
@Transactional(propagation=Propagation.REQUIRED)
public class EmailTemplateService {
	static Logger logger = Logger.getLogger(EmailTemplateService.class);
	
	@Autowired
	EntityManager em;

	public EmailTemplate getByTemplateId(String templateId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailTemplate t where t.templateId = :templateId");
			query.setParameter("templateId", templateId);
			EmailTemplate template = (EmailTemplate) query.getSingleResult();
			//em.lock(template, LockModeType.OPTIMISTIC);
			return template;
		}
		finally {
		}
	}
	
	public EmailTemplate getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from EmailTemplate t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			EmailTemplate template = (EmailTemplate) query.getSingleResult();
			//em.lock(template, LockModeType.OPTIMISTIC);
			return template;
		}
		finally {
		}
	}
	
	public List<EmailTemplate> getAll() {
		try {
			Query query = em.createQuery("select t from EmailTemplate t");
			@SuppressWarnings("unchecked")
			List<EmailTemplate> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public List<EmailTemplate> getByMailingListId(String listId) {
		try {
			Query query = em.createQuery("select t from EmailTemplate t, MailingList ml " +
					"where t.mailingList=ml and ml.listId=:listId ");
			query.setParameter("listId", listId);
			@SuppressWarnings("unchecked")
			List<EmailTemplate> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(EmailTemplate template) {
		if (template==null) return;
		try {
			em.remove(template);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from EmailTemplate t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(EmailTemplate template) {
		try {
			em.persist(template);
		}
		finally {
		}
	}
	
	public void update(EmailTemplate template) {
		try {
			if (em.contains(template)) {
				em.persist(template);
			}
			else {
				em.merge(template);
			}
		}
		finally {
		}
	}
	
}

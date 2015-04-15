package jpa.service.common;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.Constants;
import jpa.model.ReloadFlags;

@Component("reloadFlagsService")
@Transactional(propagation=Propagation.REQUIRED)
public class ReloadFlagsService implements java.io.Serializable {
	private static final long serialVersionUID = -5202558657747103262L;

	static Logger logger = Logger.getLogger(ReloadFlagsService.class);
	
	@Autowired
	EntityManager em;

	public ReloadFlags select() {
		return selectWithRepair(0);
	}
	
	private ReloadFlags selectWithRepair(int retry) {
		try {
			Query query = em.createQuery("select t from ReloadFlags t");
			@SuppressWarnings("unchecked")
			List<ReloadFlags> list = query.setMaxResults(1).getResultList();
			if (list.size() > 0) {
				return list.get(0);
			}
			else if (retry < 1) {
				repair();
				return selectWithRepair(++retry);
			}
			else {
				throw new RuntimeException("Internal error, contact programming.");
			}
		}
		finally {
		}
	}
	
	private void repair() {
		ReloadFlags record = new ReloadFlags();
		record.setUpdtUserId(Constants.DEFAULT_USER_ID);
		record.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
		insert(record);
	}

	public void insert(ReloadFlags record) {
		try {
			em.persist(record);
			em.flush();
		}
		finally {
		}
	}
	
	public void update(ReloadFlags record) {
		try {
			if (em.contains(record)) {
				em.persist(record);
			}
			else {
				em.merge(record);
			}
			em.flush();
		}
		finally {
		}
	}
	
	public void updateSenderReloadFlag() {
		ReloadFlags record = select();
		record.setSenders(record.getSenders()+1);
		update(record);
	}

	public void updateRuleReloadFlag() {
		ReloadFlags record = select();
		record.setRules(record.getRules()+1);
		update(record);
	}

	public void updateActionReloadFlag() {
		ReloadFlags record = select();
		record.setActions(record.getActions()+1);
		update(record);
	}

	public void updateTemplateReloadFlag() {
		ReloadFlags record = select();
		record.setTemplates(record.getTemplates()+1);
		update(record);
	}

	public void updateScheduleReloadFlag() {
		ReloadFlags record = select();
		record.setSchedules(record.getSchedules()+1);
		update(record);
	}

}

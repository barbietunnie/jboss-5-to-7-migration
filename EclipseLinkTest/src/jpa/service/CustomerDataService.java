package jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.CustomerData;

@Component("customerDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class CustomerDataService {
	static Logger logger = Logger.getLogger(CustomerDataService.class);
	
	@Autowired
	EntityManager em;

	public CustomerData getByCustomerId(String customerId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from CustomerData t where t.customerId = :customerId");
			query.setParameter("customerId", customerId);
			CustomerData customer = (CustomerData) query.getSingleResult();
			em.lock(customer, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return customer;
		}
		finally {
		}
	}
	
	public CustomerData getByRowId(int rowId) throws NoResultException {
		try {
			Query query = em.createQuery("select t from CustomerData t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			CustomerData customer = (CustomerData) query.getSingleResult();
			em.lock(customer, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			return customer;
		}
		finally {
		}
	}
	
	public List<CustomerData> getAll() {
		try {
			Query query = em.createQuery("select t from CustomerData t");
			@SuppressWarnings("unchecked")
			List<CustomerData> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(CustomerData customer) {
		if (customer==null) return;
		try {
			em.remove(customer);
		}
		finally {
		}
	}

	public int deleteByCustomerId(String customerId) {
		try {
			Query query = em.createQuery("delete from CustomerData t where t.customerId=:customerId");
			query.setParameter("customerId", customerId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from CustomerData t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(CustomerData customer) {
		try {
			em.persist(customer);
		}
		finally {
		}
	}
	
	public void update(CustomerData customer) {
		try {
			if (em.contains(customer)) {
				em.persist(customer);
			}
			else {
				em.merge(customer);
			}
		}
		finally {
		}
	}
	
}

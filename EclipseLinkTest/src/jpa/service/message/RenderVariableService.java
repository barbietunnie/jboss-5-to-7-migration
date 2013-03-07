package jpa.service.message;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.message.RenderVariable;
import jpa.model.message.RenderVariablePK;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("renderVariableService")
@Transactional(propagation=Propagation.REQUIRED)
public class RenderVariableService {
	static Logger logger = Logger.getLogger(RenderVariableService.class);
	
	@Autowired
	EntityManager em;

	public RenderVariable getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"RenderVariable t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			RenderVariable record = (RenderVariable) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public RenderVariable getByPrimaryKey(RenderVariablePK pk) throws NoResultException {
		if (pk.getMessageRendered()==null) {
			throw new IllegalArgumentException("A MessageRendered instance must be provided in Primary Key object.");
		}
		String sql = 
				"select t " +
				"from RenderVariable t, MessageRendered mi where " +
					" mi=t.renderVariablePK.messageRendered and mi.rowId=:renderRowId " +
					" and t.renderVariablePK.variableName=:variableName ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("renderRowId", pk.getMessageRendered().getRowId());
				query.setParameter("variableName", pk.getVariableName());
				RenderVariable record = (RenderVariable) query.getSingleResult();
				return record;
			}
			finally {
			}
	}

	public List<RenderVariable> getByRenderId(int renderId) throws NoResultException {
		String sql = 
				"select t " +
				"from RenderVariable t, MessageRendered mi where " +
					" mi=t.renderVariablePK.messageRendered and mi.rowId=:renderId ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("renderId", renderId);
				@SuppressWarnings("unchecked")
				List<RenderVariable> list = query.getResultList();
				return list;
			}
			finally {
			}
	}

	public void delete(RenderVariable variable) {
		if (variable == null) return;
		try {
			em.remove(variable);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from RenderVariable t " +
				" where t.rowId=:rowId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(RenderVariablePK pk) {
		if (pk.getMessageRendered()==null) {
			throw new IllegalArgumentException("A MessageRendered instance must be provided in Primary Key object.");
		}
		String sql = 
				"delete from Render_Variable where " +
				" MessageRenderedRowId=?1 and VariableName=?2 ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getMessageRendered().getRowId());
			query.setParameter(2, pk.getVariableName());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRenderId(int renderId) {
		String sql = 
				"delete from RenderVariable t " +
				" where t.renderVariablePK.messageRendered.rowId=:renderId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("renderId", renderId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(jpa.model.message.RenderVariable variable) {
		try {
			if (em.contains(variable)) {
				em.persist(variable);
			}
			else {
				em.merge(variable);
			}
		}
		finally {
		}
	}

	public void insert(RenderVariable variable) {
		try {
			em.persist(variable);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

}

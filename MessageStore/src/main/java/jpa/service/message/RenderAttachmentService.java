package jpa.service.message;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.model.message.RenderAttachment;
import jpa.model.message.RenderAttachmentPK;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("renderAttachmentService")
@Transactional(propagation=Propagation.REQUIRED)
public class RenderAttachmentService implements java.io.Serializable {
	private static final long serialVersionUID = -4386433389528041498L;

	static Logger logger = Logger.getLogger(RenderAttachmentService.class);
	
	@Autowired
	EntityManager em;

	public RenderAttachment getByRowId(int rowId) throws NoResultException {
		String sql = 
			"select t " +
			"from " +
				"RenderAttachment t where t.rowId=:rowId";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("rowId", rowId);
			RenderAttachment record = (RenderAttachment) query.getSingleResult();
			return record;
		}
		finally {
		}
	}

	public RenderAttachment getByPrimaryKey(RenderAttachmentPK pk) throws NoResultException {
		if (pk.getMessageRendered()==null) {
			throw new IllegalArgumentException("A MessageRendered instance must be provided in Primary Key object.");
		}
		String sql = 
				"select t " +
				"from RenderAttachment t, MessageRendered mi where " +
					" mi=t.renderAttachmentPK.messageRendered and mi.rowId=:renderRowId " +
					" and t.renderAttachmentPK.attachmentSequence=:attachmentSequence ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("renderRowId", pk.getMessageRendered().getRowId());
				query.setParameter("attachmentSequence", pk.getAttachmentSequence());
				RenderAttachment record = (RenderAttachment) query.getSingleResult();
				return record;
			}
			finally {
			}
	}

	public List<RenderAttachment> getByRenderId(int renderId) throws NoResultException {
		String sql = 
				"select t " +
				"from RenderAttachment t, MessageRendered mi where " +
					" mi=t.renderAttachmentPK.messageRendered and mi.rowId=:renderId ";
			try {
				Query query = em.createQuery(sql);
				query.setParameter("renderId", renderId);
				@SuppressWarnings("unchecked")
				List<RenderAttachment> list = query.getResultList();
				return list;
			}
			finally {
			}
	}

	public void delete(RenderAttachment attachmnt) {
		if (attachmnt == null) return;
		try {
			em.remove(attachmnt);
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		String sql = 
				"delete from RenderAttachment t " +
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

	public int deleteByPrimaryKey(RenderAttachmentPK pk) {
		if (pk.getMessageRendered()==null) {
			throw new IllegalArgumentException("A MessageRendered instance must be provided in Primary Key object.");
		}
		String sql = 
				"delete from Render_Attachment where " +
				" MessageRenderedRowId=?1 and AttachmentSequence=?2 ";
		try {
			Query query = em.createNativeQuery(sql);
			query.setParameter(1, pk.getMessageRendered().getRowId());
			query.setParameter(2, pk.getAttachmentSequence());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRenderId(int renderId) {
		String sql = 
				"delete from RenderAttachment t " +
				" where t.renderAttachmentPK.messageRendered.rowId=:renderId ";
		try {
			Query query = em.createQuery(sql);
			query.setParameter("renderId", renderId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void update(jpa.model.message.RenderAttachment attachmnt) {
		try {
			if (em.contains(attachmnt)) {
				em.persist(attachmnt);
			}
			else {
				em.merge(attachmnt);
			}
		}
		finally {
		}
	}

	public void insert(RenderAttachment attachmnt) {
		try {
			em.persist(attachmnt);
			em.flush(); // to populate the @Id field
		}
		finally {
		}
	}

}

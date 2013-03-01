package com.legacytojava.message.dao.emailaddr;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.vo.emailaddr.EmailTemplateVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class EmailTemplateTest {
	static final String LF = System.getProperty("line.separator", "\n");
	@Resource
	private EmailTemplateDao emailTemplateDao;

	@Test
	public void testEmailTemplate() throws Exception {
		try {
			List<EmailTemplateVo> lst1 =selectAll(true);
			assertTrue(lst1.size()>0);
			List<EmailTemplateVo> lst2 =selectAll(false);
			assertTrue(lst2.size()>0);
			List<EmailTemplateVo> list = selectByListId(lst2.get(0).getListId());
			assertTrue(list.size()>0);
			EmailTemplateVo vo1 = selectByTemplateId(lst1.get(0).getTemplateId());
			assertNotNull(vo1);
			EmailTemplateVo vo2 = insert(lst1.get(0).getTemplateId());
			assertNotNull(vo2);
			vo1.setTemplateId(vo2.getTemplateId());
			vo1.setOrigTemplateId(vo2.getOrigTemplateId());
			vo1.setRowId(vo2.getRowId());
			assertTrue(vo1.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertTrue(rowsUpdated==2);
			int rowsDeleted = deleteByTemplateId(vo2.getTemplateId());
			assertTrue(rowsDeleted==1);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private List<EmailTemplateVo> selectAll(boolean forTrial) {
		List<EmailTemplateVo> list = null;
		if (forTrial) {
			list = emailTemplateDao.getAllForTrial();
			System.out.println("EmailTemplateDao - getAllForTrial() - size: " + list.size());
		}
		else {
			list = emailTemplateDao.getAll();
			System.out.println("EmailTemplateDao - getAll() - size: " + list.size());
		}
		return list;
	}
	
	private EmailTemplateVo selectByTemplateId(String templateId) {
		EmailTemplateVo vo = emailTemplateDao.getByTemplateId(templateId);
		if (vo != null) {
			System.out.println("EmailTemplateDao - selectByTemplateId: " + LF + vo);
		}
		return vo;
	}
	
	private List<EmailTemplateVo> selectByListId(String listId) {
		List<EmailTemplateVo> list = emailTemplateDao.getByListId(listId);
		for (EmailTemplateVo vo : list) {
			System.out.println("EmailTemplateDao - selectByListId: " + LF + vo);
			break;
		}
		return list;
	}
	
	private int update(EmailTemplateVo emailTemplate) {
		int rows = 0;
		if (emailTemplate!=null) {
			emailTemplate.setOrigUpdtTime(new Timestamp(new Date().getTime()));
			emailTemplate.setSubject(emailTemplate.getSubject()+", test update");
			int rows1 = emailTemplateDao.update(emailTemplate);
			System.out.println("EmailTemplateDao - rows updated: "+rows1);
			rows += rows1;
			emailTemplate.setBodyText(emailTemplate.getBodyText() + LF + "... test update.");
			int rows2 = emailTemplateDao.update(emailTemplate);
			System.out.println("EmailTemplateDao - rows updated: "+rows2);
			rows += rows2;
		}
		return rows;
	}
	
	private int deleteByTemplateId(String templateId) {
		int rowsDeleted = emailTemplateDao.deleteByTemplateId(templateId);
		System.out.println("EmailTemplateDao - deleteByTemplateId: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private EmailTemplateVo insert(String templateId) {
		EmailTemplateVo emailTemplateVo = emailTemplateDao.getByTemplateId(templateId);
		if (emailTemplateVo == null) {
			List<EmailTemplateVo> lst2 =selectAll(false);
			emailTemplateVo = lst2.get(lst2.size()-1);
		}
		emailTemplateVo.setTemplateId(templateId + "_v2");
		int rows = emailTemplateDao.insert(emailTemplateVo);
		System.out.println("EmailTemplateDao - insert: rows inserted "+rows);
		return selectByTemplateId(emailTemplateVo.getTemplateId());
	}
}

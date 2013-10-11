package jpa.dao;

import static org.junit.Assert.*;

import java.util.List;

import jpa.model.EmailAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/spring-jpa-config.xml"})
@TransactionConfiguration(defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class DaoTest {
	
	@Autowired
	EmailAddressDao emailDao;

	@Test
	public void testEmailAddressDao() {
		List<EmailAddress> addrs =emailDao.getEmailByAddress("jsmith@test.com");
		assertTrue(addrs.size()>0);
	}
}

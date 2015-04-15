package jpa.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.ReloadFlags;
import jpa.service.common.ReloadFlagsService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class ReloadFlagsTest {

	@BeforeClass
	public static void ReloadFlagsPrepare() {
	}

	@Autowired
	ReloadFlagsService service;

	@Test
	public void ReloadFlagsService() throws Exception {
		ReloadFlags record = service.select();
		assertNotNull(record);
		
		ReloadFlags backup = null;
		try {
			backup = (ReloadFlags) BeanUtils.cloneBean(record);
		}
		catch (Exception e) {
			throw e;
		}
		
		record.setSenders(record.getSenders() + 1);
		service.update(record);
		assertTrue(record.getSenders()==(backup.getSenders()+1));
		
		service.updateSenderReloadFlag();
		service.updateRuleReloadFlag();
		service.updateActionReloadFlag();
		service.updateTemplateReloadFlag();
		service.updateScheduleReloadFlag();
		
		ReloadFlags record2 = service.select();

		assertTrue(record2.getSenders()==backup.getSenders()+2);
		assertTrue(record2.getRules()==backup.getRules()+1);
		assertTrue(record2.getActions()==backup.getActions()+1);
		assertTrue(record2.getTemplates()==backup.getTemplates()+1);
		assertTrue(record2.getSchedules()==backup.getSchedules()+1);
	}
}

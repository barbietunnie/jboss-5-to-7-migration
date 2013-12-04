package com.es.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.address.MobileCarrierDao;
import com.es.vo.address.MobileCarrierVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MobileCarrierTest {
	@Resource
	private MobileCarrierDao mobileCarrierDao;
	private String testCarrierId = "TestCarrier";
	
	@BeforeClass
	public static void MobileCarrierPrepare() {
	}
	
	@Test
	public void insertUpdateDelete() {
		try {
			MobileCarrierVo vo = insert();
			assertNotNull(vo);
			MobileCarrierVo vo2 = selectByCarrierId(vo.getCarrierId());
			assertNotNull(vo2);
			vo2.setUpdtTime(vo.getUpdtTime());
			assertTrue(vo.equalsTo(vo2));
			MobileCarrierVo vo3 = mobileCarrierDao.getByCarrierName(vo2.getCarrierName());
			vo3.setUpdtTime(vo2.getUpdtTime());
			assertTrue(vo2.equalsTo(vo3));
			int rowsUpdated = update(vo);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = delete(vo);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			mobileCarrierDao.deleteByCarrierId(testCarrierId);
			fail();
		}
	}
	
	private MobileCarrierVo selectByCarrierId(String carrierId) {
		MobileCarrierVo vo = mobileCarrierDao.getByCarrierId(carrierId);
		if (vo != null) {
			System.out.println("MobileCarrierDao: selectByCarrierId "+vo);
		}
		return vo;
	}

	private int update(MobileCarrierVo mcVo) {
		MobileCarrierVo vo = mobileCarrierDao.getByCarrierId(mcVo.getCarrierId());
		vo.setCarrierName(mcVo.getCarrierName() + "_v2");
		int rows = mobileCarrierDao.update(vo);
		System.out.println("MobileCarrierDao: update "+rows+"\n"+vo);
		return rows;
	}
	private MobileCarrierVo insert() {
		List<MobileCarrierVo> list = mobileCarrierDao.getAll(true);
		MobileCarrierVo vo = null;
		if (list.isEmpty()) {
			vo = new MobileCarrierVo();
			vo.setCarrierId(testCarrierId);
			vo.setCountryCode("US");
			vo.setTextAddress("test.com");
		}
		else {
			mobileCarrierDao.deleteByCarrierId(testCarrierId);
			vo = list.get(0);
			vo.setCarrierId(testCarrierId);
		}
		vo.setCarrierName("Test Carrier Name");
		mobileCarrierDao.insert(vo);
		System.out.println("MobileCarrierDao: insert "+vo);
		return vo;
	}
	private int delete(MobileCarrierVo mcVo) {
		int rowsDeleted = mobileCarrierDao.deleteByCarrierId(mcVo.getCarrierId());
		System.out.println("MobileCarrierDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
}

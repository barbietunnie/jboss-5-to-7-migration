package com.legacytojava.message.dao.socket;

import java.util.Iterator;
import java.util.List;

import org.springframework.context.ApplicationContext;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.vo.SocketServerVo;

public class SocketServerTest {
	public static void main(String[] args){
		ApplicationContext factory = SpringUtil.getDaoAppContext();
		SocketServerDao socketServerDao = (SocketServerDao)factory.getBean("socketServerDao");
		
		try {
			SocketServerTest test = new SocketServerTest();
			test.select(socketServerDao);
			test.update(socketServerDao);
			SocketServerVo socketServerVo = test.insert(socketServerDao);
			if (socketServerVo!=null)
				test.delete(socketServerDao, socketServerVo);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private void select(SocketServerDao socketServerDao) {
		List<SocketServerVo> socketServeres = socketServerDao.getAll(true);
		for (Iterator<SocketServerVo> it=socketServeres.iterator(); it.hasNext();) {
			SocketServerVo socketServerVo = it.next();
			System.out.println("SocketServerDao - select: "+socketServerVo);
			SocketServerVo vo2 = socketServerDao.getByPrimaryKey(socketServerVo.getServerName());
			if (vo2 == null)
				throw new RuntimeException("Internal error");
		}
	}
	
	private void update(SocketServerDao socketServerDao) {
		List<SocketServerVo> socketServeres = socketServerDao.getAll(false);
		if (socketServeres.size()>0) {
			SocketServerVo socketServerVo = socketServeres.get(0);
			if ("A".equals(socketServerVo.getStatusId())) {
				socketServerVo.setStatusId("A");
			}
			socketServerDao.update(socketServerVo);
			System.out.println("SocketServerDao - update: "+socketServerVo);
		}
	}
	
	private void delete(SocketServerDao socketServerDao, SocketServerVo socketServerVo) {
		try {
			int rowsDeleted = socketServerDao.deleteByPrimaryKey(socketServerVo.getServerName());
			System.out.println("SocketServerDao - delete: Rows Deleted: "+rowsDeleted);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	private SocketServerVo insert(SocketServerDao socketServerDao) {
		List<SocketServerVo> socketServeres = socketServerDao.getAll(false);
		if (socketServeres.size()>0) {
			SocketServerVo socketServerVo = socketServeres.get(0);
			socketServerVo.setServerName(socketServerVo.getServerName()+"_test");
			socketServerVo.setSocketPort(socketServerVo.getSocketPort()+10);
			socketServerDao.insert(socketServerVo);
			System.out.println("SocketServerDao - insert: "+socketServerVo);
			return socketServerVo;
		}
		return null;
	}
}

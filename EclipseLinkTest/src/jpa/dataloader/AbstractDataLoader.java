package jpa.dataloader;

import jpa.util.SpringUtil;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public abstract class AbstractDataLoader {
	public static final String LF = System.getProperty("line.separator", "\n");
	
	private PlatformTransactionManager txmgr;
	private TransactionStatus status;
	
	public abstract void loadData();

	protected void startTransaction() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("loader_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		status = txmgr.getTransaction(def);
	}
	
	protected void commitTransaction() {
		txmgr.commit(status);
	}
	
}

package jpa.dataloader;

import jpa.util.SpringUtil;

public abstract class AbstractDataLoader {
	public static final String LF = System.getProperty("line.separator", "\n");
	
	public abstract void loadData();

	protected void startTransaction() {
		SpringUtil.startTransaction();
	}
	
	protected void commitTransaction() {
		SpringUtil.commitTransaction();
	}
	
}

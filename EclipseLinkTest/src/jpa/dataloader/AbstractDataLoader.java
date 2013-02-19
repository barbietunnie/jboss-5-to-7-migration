package jpa.dataloader;

import jpa.util.EnvUtil;
import jpa.util.SpringUtil;
import jpa.variable.VarProperties;

public abstract class AbstractDataLoader {
	public static final String LF = System.getProperty("line.separator", "\n");

	protected VarProperties props;
	
	public abstract void loadData();

	protected void startTransaction() {
		SpringUtil.startTransaction();
	}
	
	protected void commitTransaction() {
		SpringUtil.commitTransaction();
	}
	
	protected String getProperty(String name) {
		if (props == null) {
			String propsFile = "META-INF/dataloader." + EnvUtil.getEnv() + ".properties";
			props = VarProperties.loadMyProperties(propsFile);
		}
		return props.getProperty(name);
	}
}

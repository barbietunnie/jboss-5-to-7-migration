package com.es.data.loader;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.es.core.util.EnvUtil;
import com.es.core.util.FileUtil;
import com.es.core.util.SpringUtil;
import com.es.variable.VarReplProperties;

public abstract class AbstractTableBase {
	protected static final String LF = System.getProperty("line.separator", "\n");

	protected VarReplProperties props;
	
	protected JdbcTemplate getJdbcTemplate() {
		DataSource ds = (DataSource) SpringUtil.getAppContext().getBean("msgDataSource");
		return new JdbcTemplate(ds);
	}

	public abstract void createTables() throws DataAccessException;
	
	public abstract void dropTables();
	
	public abstract void loadTestData() throws DataAccessException;

	protected void startTransaction() {
		SpringUtil.beginTransaction();
	}
	
	protected void commitTransaction() {
		SpringUtil.commitTransaction();
	}
	
	protected String getProperty(String name) {
		if (props == null) {
			String propsFile = "META-INF/dataloader." + EnvUtil.getEnv() + ".properties";
			props = VarReplProperties.loadMyProperties(propsFile);
		}
		return props.getProperty(name);
	}

	public void loadReleaseData() throws SQLException {
		loadTestData();
	}
	
	protected byte[] loadFromSamples(String fileName) {
		return FileUtil.loadFromFile("samples/", fileName);
	}

}

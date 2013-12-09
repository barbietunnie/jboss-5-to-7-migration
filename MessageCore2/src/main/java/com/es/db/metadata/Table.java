package com.es.db.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Table {
	private String catalog;
	private String schema;
	private String tableName;

	private Map<String, PrimaryKey> primaryKeyMap = new LinkedHashMap<String, PrimaryKey>();
	private final Map<String, Column> columnMap = new LinkedHashMap<String, Column>();
	
	public String getCatalog() {
		return catalog;
	}
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public Map<String, PrimaryKey> getPrimaryKeyMap() {
		return primaryKeyMap;
	}
	public List<PrimaryKey> getPrimaryKeyList() {
		return new ArrayList<PrimaryKey>(primaryKeyMap.values());
	}
	public Map<String, Column> getColumnMap() {
		return columnMap;
	}
	public List<Column> getColumnList() {
		return new ArrayList<Column>(columnMap.values());
	}
}

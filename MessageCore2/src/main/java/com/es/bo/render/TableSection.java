package com.es.bo.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A convenient class for building TABLE SECTION.
 * @author Jack W.
 */
public class TableSection implements java.io.Serializable {
	private static final long serialVersionUID = 5642929096936757717L;

	private final List<Map<String, RenderVariable>> collection;
	
	public TableSection() {
		collection = new ArrayList<Map<String, RenderVariable>>();
	}

	public Map<String, RenderVariable> getEmptyRow() {
		return new LinkedHashMap<String, RenderVariable>();
	}

	public void addRow(Map<String, RenderVariable> row) {
		collection.add(row);
	}
	
	public List<Map<String, RenderVariable>> getCollection() {
		return collection;
	}
}

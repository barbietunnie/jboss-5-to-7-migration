package com.es.bo.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TableVariables implements java.io.Serializable {
	private static final long serialVersionUID = -3391092571476403093L;

	private final List<Map<String, RenderVariable>> collection;
	
	public TableVariables() {
		collection = new ArrayList<Map<String, RenderVariable>>();
	}
	
	public List<RenderVariable> getEmptyRow() {
		return new ArrayList<RenderVariable>();
	}
	
	public void addRow(List<RenderVariable> row) {
		Map<String, RenderVariable> tableRow = new LinkedHashMap<String, RenderVariable>();
		for (RenderVariable var : row) {
			tableRow.put(var.getVariableName(), var);
		}
		collection.add(tableRow);
	}
	
	public List<Map<String, RenderVariable>> getCollection() {
		return collection;
	}
}

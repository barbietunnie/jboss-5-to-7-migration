package com.legacytojava.message.vo;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SqlDateAdapter extends XmlAdapter<java.util.Date, java.sql.Date> {

	@Override
	public java.sql.Date unmarshal(java.util.Date v) throws Exception {
		if(null == v) {
            return null;
        }
		return new java.sql.Date(v.getTime());
	}

	@Override
	public java.util.Date marshal(java.sql.Date v) throws Exception {
		if(null == v) {
            return null;
        }
		return new java.util.Date(v.getTime());
	}

}

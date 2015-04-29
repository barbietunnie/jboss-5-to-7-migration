package jpa.msgui.vo;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BooleanAdapter extends XmlAdapter<Integer, Boolean> {

	@Override
	public Boolean unmarshal(Integer v) throws Exception {
		return (v == null ? null : v == 1);
	}

	@Override
	public Integer marshal(Boolean v) throws Exception {
		return (v == null ? null : v ? 1 : 0);
	}

}

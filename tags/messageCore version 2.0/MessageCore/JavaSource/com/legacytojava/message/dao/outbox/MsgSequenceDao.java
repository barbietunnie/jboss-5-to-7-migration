package com.legacytojava.message.dao.outbox;

public interface MsgSequenceDao {
	public long findNextValue();
}

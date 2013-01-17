package com.legacytojava.message.bo.mailreader;

import com.legacytojava.jbatch.Processor;

public interface DuplicateCheckDao extends Processor {
	public boolean isDuplicate(String msg_id);
	public void purge(int hours);
}

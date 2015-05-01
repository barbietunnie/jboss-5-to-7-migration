package com.es.ejb.mailinglist;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;

@Local
public interface MailingListLocal {
	public List<jpa.model.MailingList> getActiveLists();
	
	public int sendMail(String toAddr, Map<String, String> variables, String templateId);

	public int broadcast(String templateId);

	public int broadcast(String templateId, String listId);
	
	public void removeFromList(int bcstTrkRowId);
}

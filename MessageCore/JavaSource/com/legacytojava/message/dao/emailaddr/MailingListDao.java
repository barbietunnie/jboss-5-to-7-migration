package com.legacytojava.message.dao.emailaddr;

import java.util.List;

import com.legacytojava.message.vo.emailaddr.MailingListVo;

public interface MailingListDao {
	public MailingListVo getByListId(String listId);
	public List<MailingListVo> getByAddress(String emailAddr);
	public List<MailingListVo> getAll(boolean onlyActive);
	public List<MailingListVo> getAllForTrial(boolean onlyActive);
	public List<MailingListVo> getSubscribedLists(long emailAddrId);
	public int update(MailingListVo mailingListVo);
	public int deleteByListId(String listId);
	public int deleteByAddress(String emailAddr);
	public int insert(MailingListVo mailingListVo);
}

package com.legacytojava.message.dao.emailaddr;

import java.util.List;

import com.legacytojava.message.vo.PagingVo;
import com.legacytojava.message.vo.emailaddr.SubscriptionVo;

public interface SubscriptionDao {
	public int subscribe(long addrId, String listId);
	public int subscribe(String addr, String listId);
	public int unsubscribe(long addrId, String listId);
	public int unsubscribe(String addr, String listId);
	public int optInRequest(String addr, String listId);
	public int optInRequest(long addrId, String listId);
	public int optInConfirm(long addrId, String listId);
	public int optInConfirm(String addr, String listId);
	public int getSubscriberCount(String listId, PagingVo vo);
	public List<SubscriptionVo> getSubscribersWithPaging(String listId, PagingVo vo);
	public List<SubscriptionVo> getSubscribers(String listId);
	public List<SubscriptionVo> getSubscribersWithCustomerRecord(String listId);
	public List<SubscriptionVo> getSubscribersWithoutCustomerRecord(String listId);
	public SubscriptionVo getByPrimaryKey(long addrId, String listId);
	public SubscriptionVo getByAddrAndListId(String addr, String listId);
	public List<SubscriptionVo> getByAddrId(long addrId);
	public List<SubscriptionVo> getByListId(String listId);
	public int update(SubscriptionVo subscriptionVo);
	public int updateSentCount(long emailAddrId, String listId);
	public int updateOpenCount(long emailAddrId, String listId);
	public int updateClickCount(long emailAddrId, String listId);
	public int deleteByPrimaryKey(long addrid, String listId);
	public int deleteByAddrId(long addrId);
	public int deleteByListId(String listId);
	public int insert(SubscriptionVo subscriptionVo);
}

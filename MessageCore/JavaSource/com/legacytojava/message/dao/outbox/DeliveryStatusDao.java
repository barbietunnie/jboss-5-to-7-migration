package com.legacytojava.message.dao.outbox;

import java.util.List;

import com.legacytojava.message.vo.outbox.DeliveryStatusVo;

public interface DeliveryStatusDao {
	public DeliveryStatusVo getByPrimaryKey(long msgId, long finalRecipientId);
	public List<DeliveryStatusVo> getByMsgId(long msgId);
	public int update(DeliveryStatusVo deliveryStatusVo);
	public int deleteByPrimaryKey(long msgId, long finalRecipientId);
	public int deleteByMsgId(long msgId);
	public int insertWithDelete(DeliveryStatusVo deliveryStatusVo);
	public int insert(DeliveryStatusVo deliveryStatusVo);
}

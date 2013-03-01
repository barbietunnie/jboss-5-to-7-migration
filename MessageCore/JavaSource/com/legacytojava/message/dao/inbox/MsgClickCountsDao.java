package com.legacytojava.message.dao.inbox;

import java.util.List;

import com.legacytojava.message.vo.PagingVo;
import com.legacytojava.message.vo.inbox.MsgClickCountsVo;

public interface MsgClickCountsDao {
	public List<MsgClickCountsVo> getAll();
	public int getMsgCountForWeb();
	public MsgClickCountsVo getByPrimaryKey(long msgId);
	public List<MsgClickCountsVo> getBroadcastsWithPaging(PagingVo vo);
	public int update(MsgClickCountsVo msgClickCountsVo);
	public int deleteByPrimaryKey(long msgId);
	public int insert(MsgClickCountsVo msgClickCountsVo);
	public int updateSentCount(long msgId, int count);
	public int updateOpenCount(long msgId, int count);
	public int updateClickCount(long msgId, int count);
	public int updateOpenCount(long msgId);
	public int updateClickCount(long msgId);
	public int updateReferalCount(long msgId, int count);
	public int updateReferalCount(long msgId);
	public int updateStartTime(long msgId);
	public int updateUnsubscribeCount(long msgId, int count);
	public int updateComplaintCount(long msgId, int count);
}

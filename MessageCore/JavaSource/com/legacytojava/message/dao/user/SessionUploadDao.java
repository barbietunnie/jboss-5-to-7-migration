package com.legacytojava.message.dao.user;

import java.util.List;

import com.legacytojava.message.vo.SessionUploadVo;

public interface SessionUploadDao {
	public SessionUploadVo getByPrimaryKey(String sessionId, int sessionSeq);
	public List<SessionUploadVo> getBySessionId(String sessionId);
	public List<SessionUploadVo> getBySessionId4Web(String sessionId);
	public List<SessionUploadVo> getByUserId(String userId);
	public int update(SessionUploadVo sessVo);
	public int deleteByPrimaryKey(String sessionId, int sessionSeq);
	public int deleteBySessionId(String sessionId);
	public int deleteByUserId(String userId);
	public int deleteExpired(int minutes);
	public int deleteAll();
	public int insert(SessionUploadVo sessVo);
	public int insertLast(SessionUploadVo sessVo);
}

package com.legacytojava.msgui.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.legacytojava.message.dao.inbox.MsgInboxDao;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.inbox.MsgInboxWebVo;

public class MessageThreadsBuilder {

	/**
	 * Build a list of threaded messages from a message list.
	 * 
	 * @param messages -
	 *            a list of messages that associated to a lead thread
	 *            (identified by LeadMsgId)
	 * @return a threaded message list
	 */
	public static List<MsgInboxWebVo> buildThreads(List<MsgInboxWebVo> messages) {
		List<MsgInboxWebVo> threads = new ArrayList<MsgInboxWebVo>();
		if (messages == null || messages.isEmpty())
			return threads;
		HashMap<Long, List<Reply>> map = buildMap(messages);
		if (map.containsKey(null)) {
			// originating message thread found
			List<Reply> root = map.get(null);
			buildTreeLevel(root, map, messages, threads, 0);
		}
		else {
			// missing originating message, look for the oldest thread
			// messages list is sorted by MsgId in ascending order
			for (int i = 0; i < messages.size(); i++) {
				MsgInboxWebVo vo = messages.get(i);
				if (map.containsKey(vo.getMsgRefId())) {
					List<Reply> root = map.get(vo.getMsgRefId());
					buildTreeLevel(root, map, messages, threads, 0);
					break;
				}
			}
		}
		// in case there were missing links due to message deletion
		for (MsgInboxWebVo vo : messages) {
			if (vo.getThreadLevel() < 0) {
				List<Reply> root = map.get(vo.getMsgRefId());
				buildTreeLevel(root, map, messages, threads, 1);
			}
		}
		return threads;
	}
	
	/**
	 * build a list of threaded messages with indentation (identified by
	 * MsgInboxWebVo.threadLevel).
	 * 
	 * @param root -
	 *            the leading thread
	 * @param map -
	 *            MsgRefId to a list of associated messages
	 * @param messages -
	 *            list of messages to be threaded
	 * @param threads -
	 *            list of threaded of messages with indentation
	 * @param level -
	 *            starting offset from left
	 */
	private static void buildTreeLevel(List<Reply> root, HashMap<Long, List<Reply>> map,
			List<MsgInboxWebVo> messages, List<MsgInboxWebVo> threads, int level) {
		if (root == null)
			return;
		for (int i = 0; i < root.size(); i++) {
			MsgInboxWebVo vo = messages.get(root.get(i).index);
			vo.setThreadLevel(level);
			threads.add(vo);
			buildTreeLevel(map.get(root.get(i).msgId), map, messages, threads, level + 1);
		}
	}
	
	/**
	 * build a map that maps each MsgRefId to a list of its associated MsgId's.
	 * 
	 * @param messages -
	 *            list of messages to be threaded
	 * @return a map that maps each MsgRefId to its associated messages
	 */
	private static HashMap<Long, List<Reply>> buildMap(List<MsgInboxWebVo> messages) {
		HashMap<Long, List<Reply>> map = new HashMap<Long, List<Reply>>();
		for (int i = 0; i < messages.size(); i++) {
			MsgInboxWebVo vo = messages.get(i);
			if (map.containsKey(vo.getMsgRefId())) {
				List<Reply> replies = map.get(vo.getMsgRefId());
				replies.add(new Reply(vo.getMsgId(), i));
			}
			else {
				List<Reply> replies = new ArrayList<Reply>();
				replies.add(new Reply(vo.getMsgId(), i));
				map.put(vo.getMsgRefId(), replies);
			}
		}
		System.out.println(map);
		return map;
	}
	
	private static class Reply {
		long msgId;
		int index;
		Reply (long msgId, int index) {
			this.msgId = msgId;
			this.index = index;
		}
		public String toString() {
			return msgId + "";
		}
	}
	
	public static void main(String[] args) {
		try {
			long threadId = 3L;
			MsgInboxDao msgInboxDao = (MsgInboxDao) com.legacytojava.jbatch.SpringUtil.getDaoAppContext().getBean("msgInboxDao");
			List<MsgInboxWebVo> list = msgInboxDao.getByLeadMsgId(threadId);
			List<MsgInboxWebVo> threads = buildThreads(list);
			for (int i = 0; i < threads.size(); i++) {
				MsgInboxWebVo vo = threads.get(i);
				System.out.println(StringUtil.getDots(vo.getThreadLevel()) + vo.getMsgId() + " - "
						+ vo.getMsgSubject());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

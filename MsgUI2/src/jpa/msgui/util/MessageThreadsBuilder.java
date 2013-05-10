package jpa.msgui.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jpa.model.message.MessageInbox;
import jpa.service.message.MessageInboxService;
import jpa.util.StringUtil;

public class MessageThreadsBuilder {

	/**
	 * Build a list of threaded messages from a message list.
	 * 
	 * @param messages -
	 *            a list of messages that associated to a lead thread
	 *            (identified by LeadMsgId)
	 * @return a threaded message list
	 */
	public static List<MessageInbox> buildThreads(List<MessageInbox> messages) {
		List<MessageInbox> threads = new ArrayList<MessageInbox>();
		if (messages == null || messages.isEmpty()) {
			return threads;
		}
		Map<Integer, List<Reply>> map = buildMap(messages);
		if (map.containsKey(null)) {
			// originating message thread found
			List<Reply> root = map.get(null);
			buildTreeLevel(root, map, messages, threads, 0);
		}
		else {
			// missing originating message, look for the oldest thread
			// messages list is sorted by MsgId in ascending order
			for (int i = 0; i < messages.size(); i++) {
				MessageInbox vo = messages.get(i);
				if (map.containsKey(vo.getReferringMessageRowId())) {
					List<Reply> root = map.get(vo.getReferringMessageRowId());
					buildTreeLevel(root, map, messages, threads, 0);
					break;
				}
			}
		}
		// in case there were missing links due to message deletion
		for (MessageInbox vo : messages) {
			if (vo.getThreadLevel() < 0) {
				List<Reply> root = map.get(vo.getReferringMessageRowId());
				buildTreeLevel(root, map, messages, threads, 1);
			}
		}
		return threads;
	}
	
	/**
	 * build a list of threaded messages with indentation (identified by
	 * MessageInbox.threadLevel).
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
	private static void buildTreeLevel(List<Reply> root, Map<Integer, List<Reply>> map,
			List<MessageInbox> messages, List<MessageInbox> threads, int level) {
		if (root == null) {
			return;
		}
		for (int i = 0; i < root.size(); i++) {
			MessageInbox vo = messages.get(root.get(i).index);
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
	private static Map<Integer, List<Reply>> buildMap(List<MessageInbox> messages) {
		Map<Integer, List<Reply>> map = new HashMap<Integer, List<Reply>>();
		for (int i = 0; i < messages.size(); i++) {
			MessageInbox vo = messages.get(i);
			if (map.containsKey(vo.getReferringMessageRowId())) {
				List<Reply> replies = map.get(vo.getReferringMessageRowId());
				replies.add(new Reply(vo.getRowId(), i));
			}
			else {
				List<Reply> replies = new ArrayList<Reply>();
				replies.add(new Reply(vo.getRowId(), i));
				map.put(vo.getReferringMessageRowId(), replies);
			}
		}
		System.out.println(map);
		return map;
	}
	
	private static class Reply {
		int msgId;
		int index;
		Reply (int msgId, int index) {
			this.msgId = msgId;
			this.index = index;
		}
		public String toString() {
			return msgId + "";
		}
	}
	
	public static void main(String[] args) {
		try {
			int threadId = 14;
			MessageInboxService msgInboxDao = (MessageInboxService) jpa.util.SpringUtil.getAppContext().getBean("messageInboxService");
			List<MessageInbox> list = msgInboxDao.getByLeadMsgId(threadId);
			List<MessageInbox> threads = buildThreads(list);
			for (int i = 0; i < threads.size(); i++) {
				MessageInbox vo = threads.get(i);
				System.out.println(StringUtil.getDots(vo.getThreadLevel()) + vo.getRowId() + " - "
						+ vo.getMsgSubject());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
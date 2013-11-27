package com.es.vo.comm;

import java.io.Serializable;
import java.util.Properties;

public class QueueReaderVo extends ServerBaseVo implements Serializable {
	private static final long serialVersionUID = 826439429623556631L;
	private String queueName = "";
	private String deliveryCountReached; // "stop_server" or "error_queue"
	
	public QueueReaderVo (Properties props) {
		setServerName(props.getProperty("server_name", "queueReader"));
		setQueueName(props.getProperty("queue_name"));
		
		int threads = 1;
		try {
			threads = Integer.parseInt(props.getProperty("threads", "1"));
		}
		catch (NumberFormatException e) {
			threads = 1;
		}
		setThreads(threads);
		
		setPriority(props.getProperty("priority", "high"));
		
		int messageCount = 0;
		try {
			messageCount = Integer.parseInt(props.getProperty("message_count", "0"));
		}
		catch (NumberFormatException e) {
			messageCount = 0;
		}
		setMessageCount(messageCount);
	
		setAllowExtraWorkers("yes".equals(props.getProperty("allow_extra_workers", "yes")));
		setDeliveryCountReached(props.getProperty("delivery_count_reached", "error_queue"));
    	setProcessorName(props.getProperty("processor_name"));
	}
	
	public String getDeliveryCountReached() {
		return deliveryCountReached;
	}

	public final void setDeliveryCountReached(String deliveryCountReached) {
		this.deliveryCountReached = deliveryCountReached;
	}
	public String getQueueName() {
		return queueName;
	}
	public final void setQueueName(String queueName) {
		this.queueName = queueName;
	}
}
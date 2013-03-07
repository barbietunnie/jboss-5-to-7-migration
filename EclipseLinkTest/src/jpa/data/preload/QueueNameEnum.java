package jpa.data.preload;

public enum QueueNameEnum {
	RMA_REQUEST_INPUT("rmaRequestInputJmsTemplate"),
	CUSTOMER_CARE_INPUT("customerCareInputJmsTemplate");
	
	private String jmstemplate;
	private QueueNameEnum(String jmstemplate) {
		this.jmstemplate = jmstemplate;
	}
	public String getJmstemplate() {
		return jmstemplate;
	}
}

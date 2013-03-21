package jpa.service.external;

public interface RuleTargetProc {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public String process();
}

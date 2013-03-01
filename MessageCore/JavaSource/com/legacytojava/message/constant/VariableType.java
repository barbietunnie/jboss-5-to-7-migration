package com.legacytojava.message.constant;

public class VariableType {
	//
	// define variable types
	//
	public final static String TEXT = "T";
	public final static String NUMERIC = "N";
	public final static String ADDRESS = "A";
	public final static String DATETIME = "D";
	public final static String X_HEADER = "X";
	public final static String LOB = "L";
	// body template only
	public final static String COLLECTION = "C";
	// a collection of <HashMap>s (for Table section)

	// enum class = list of variables
	public static enum Variable {
		ADDRESS("A"), TEXT("T"), NUMERIC("N"), DATETIME("D"), X_HEADER("X"), LOB("L"), COLLECTION("C");
		private final String value;
		private Variable(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}
}

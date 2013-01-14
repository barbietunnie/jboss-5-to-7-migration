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

	// <HashMap> = list of variables
	public static enum VARIABLE_TYPE {
		ADDRESS, TEXT, NUMERIC, DATETIME, X_HEADER, LOB, COLLECTION
	}
}

package com.es.core.util;

public class EnvUtil {

	public static String getEnv() {
		return (System.getProperty("env","dev"));
	}
}

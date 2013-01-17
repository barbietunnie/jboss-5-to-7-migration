package com.legacytojava.message.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class DaoTestRunner extends TestRunnerBase {
	static final String DaoPackageName = "com.legacytojava.message.dao";
	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(getAllDaoTestClasses(DaoPackageName));
		if (!result.getFailures().isEmpty()) {
			for (Failure failure : result.getFailures()) {
				System.err.println(failure.toString());
			}
			System.err.println("!!!!! DAO test stopped with error !!!!!");
		}
		else {
			System.out.println("########## DAO test completed ##########");
		}
	}
}

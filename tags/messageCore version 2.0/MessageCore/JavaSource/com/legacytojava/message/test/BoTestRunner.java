package com.legacytojava.message.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class BoTestRunner extends TestRunnerBase {
	static final String BoPackageName = "com.legacytojava.message.bo";
	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(getAllDaoTestClasses(BoPackageName));
		if (!result.getFailures().isEmpty()) {
			for (Failure failure : result.getFailures()) {
				System.err.println(failure.toString());
			}
			System.err.println("!!!!! BO test stopped with error !!!!!");
		}
		else {
			System.out.println("########## BO test completed ##########");
		}
	}
}

package com.legacytojava.message.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	com.legacytojava.message.dao.action.MsgActionTest.class,
	com.legacytojava.message.dao.action.MsgActionDetailTest.class
})
public class DaoTestSuite {
	//nothing
}

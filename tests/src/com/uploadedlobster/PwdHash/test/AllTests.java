package com.uploadedlobster.PwdHash.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(HashedPasswordTest.class);
		suite.addTestSuite(DomainExtractorTest.class);
		//$JUnit-END$
		return suite;
	}

}

package com.testingtech.car2x.hmi.ttmanclient;

import com.testingtech.car2x.hmi.testcase.TestCase;
import com.testingtech.car2x.hmi.testcase.TestCaseVerdict;

public interface ITestCaseRunner {

  public void setCurrentTestCase(TestCase testCase);

  public TestCaseVerdict getVerdict();

  public void closeServerConnection();

}

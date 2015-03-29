package com.testingtech.car2x.hmi.testcase;

import com.testingtech.car2x.hmi.TestRunnerActivity;

public class Utils {

  public static TestCase toTestCase(String testCaseName) {
    try {
      return TestCase.valueOf(testCaseName);
    } catch (IllegalArgumentException iae) {
        TestRunnerActivity.writeLog("UTILS:" + iae.getMessage());
      return null;
    }
  }

  public static TestCaseProgress toTestCaseProgress(int entryIndex) {
    TestCaseProgress[] entries = TestCaseProgress.values();
    if (entryIndex < 0 || entryIndex >= entries.length) {
        TestRunnerActivity.writeLog("UTILS: Invalid TestCaseProgress entry index provided [" + entryIndex + "].");
      return null;
    } else {
      return entries[entryIndex];
    }
  }

  public static TestCaseVerdict toTestCaseVerdict(String verdictLabel) {
    switch(verdictLabel) {
      case "pass":
      case "pass (1)":
        return TestCaseVerdict.PASS;
      case "error":
      case "error (4)":
        return TestCaseVerdict.ERROR;
      default:
          TestRunnerActivity.writeLog("UTILS: Verdict label [" + verdictLabel + "] unsupported.");
        return null;
    }
  }
}

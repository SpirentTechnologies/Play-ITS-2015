package com.testingtech.car2x.hmi.testcase;

import com.testingtech.car2x.hmi.Logger;

public class Utils {

  public static TestCase toTestCase(String testCaseName) {
    try {
      return TestCase.valueOf(testCaseName);
    } catch (IllegalArgumentException iae) {
        Logger.writeLog("UTILS:" + iae.getMessage());
      return null;
    }
  }

  public static TestCaseProgress toTestCaseProgress(int entryIndex) {
    TestCaseProgress[] entries = TestCaseProgress.values();
    if (entryIndex < 0 || entryIndex >= entries.length) {
        Logger.writeLog("UTILS: Invalid TestCaseProgress entry index provided [" + entryIndex + "].");
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
          Logger.writeLog("UTILS: Verdict label [" + verdictLabel + "] unsupported.");
        return null;
    }
  }
}

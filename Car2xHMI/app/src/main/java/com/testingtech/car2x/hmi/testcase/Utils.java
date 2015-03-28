package com.testingtech.car2x.hmi.testcase;

import android.util.Log;

import java.util.logging.Level;

public class Utils {

  public static TestCase toTestCase(String testCaseName) {
    try {
      return TestCase.valueOf(testCaseName);
    } catch (IllegalArgumentException iae) {
      Log.w("UTILS", iae.getMessage());
      return null;
    }
  }

  public static TestCaseProgress toTestCaseProgress(int entryIndex) {
    TestCaseProgress[] entries = TestCaseProgress.values();
    if (entryIndex < 0 || entryIndex >= entries.length) {
      Log.w("UTILS", "Invalid TestCaseProgress entry index provided [" + entryIndex + "].");
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
      case "error (4)":
        return TestCaseVerdict.ERROR;
      default:
        Log.e("UTILS", "Verdict label [" + verdictLabel + "] unsupported.");
        return null;
    }
  }
}

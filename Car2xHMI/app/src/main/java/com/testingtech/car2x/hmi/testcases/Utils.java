package com.testingtech.car2x.hmi.testcases;

import com.testingtech.car2x.hmi.Logger;

public class Utils {

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
        switch (verdictLabel) {
            case "pass":
                return TestCaseVerdict.PASS;
            case "inconc":
                return TestCaseVerdict.INCONCLUSIVE;
            case "fail":
                return TestCaseVerdict.FAIL;
            case "error":
            case "none":
            case "userError":
                return TestCaseVerdict.ERROR;
            default:
                Logger.writeLog("UTILS: Verdict label [" + verdictLabel + "] unsupported.");
                return null;
        }
    }
}

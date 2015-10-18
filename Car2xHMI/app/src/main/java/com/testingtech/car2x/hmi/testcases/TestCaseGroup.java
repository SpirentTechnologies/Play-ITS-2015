package com.testingtech.car2x.hmi.testcases;

import java.util.ArrayList;
import java.util.List;

public class TestCaseGroup {

    private final String name;
    private final List<TestCase> testCases;

    public TestCaseGroup(String name) {
        this.name = name;
        this.testCases = new ArrayList();
    }

    public List<String> getTestCaseTitles() {
        List<String> titles = new ArrayList<String>();
        for (TestCase testCase : testCases) {
            titles.add(testCase.getTitle());
        }
        return titles;
    }

    public void addTestCase(TestCase testCase) {
        testCases.add(testCase);
    }

    public String getName() {
        return name;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }
}

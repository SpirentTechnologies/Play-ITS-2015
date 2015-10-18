package com.testingtech.car2x.hmi.testcases;

public class TestCaseStage {

    private final int id;
    private final String label;

    public TestCaseStage(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}

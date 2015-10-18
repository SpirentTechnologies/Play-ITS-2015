package com.testingtech.car2x.hmi.testcases;

import java.util.ArrayList;
import java.util.List;

public class TestCase {

    private final String id;
    private String title;
    private final List<TestCaseStage> stages;

    public TestCase(String id) {
        this.id = id;
        this.stages = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<TestCaseStage> getStages() {
        return stages;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addStage(TestCaseStage stage) {
        this.stages.add(stage);
    }

    public String[] getStagesAsArray() {
        String[] stageArray = new String[stages.size()];
        for (int stage = 0; stage < stages.size(); stage++) {
            stageArray[stage] = stages.get(stage).getLabel();
        }
        return stageArray;
    }
}

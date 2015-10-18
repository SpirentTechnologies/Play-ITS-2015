package com.testingtech.car2x.hmi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;

import com.testingtech.car2x.hmi.testcases.TestCase;
import com.testingtech.car2x.hmi.testcases.TestCaseGroup;
import com.testingtech.car2x.hmi.testcases.XmlLoader;
import com.testingtech.car2x.hmi.ttmanclient.Driver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestSelectorActivity extends AppCompatActivity {

    public final static String TEST_ID = "id";
    public final static String TEST_TITLE = "title";
    public final static String TEST_STAGES = "stages";
    private Map<String, List<String>> collection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_selector);

        createCollection();
        ExpandableListView expListView = (ExpandableListView) findViewById(R.id.listView);
        final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(
                this, XmlLoader.getTestCaseGroupNames(), collection);
        expListView.setAdapter(expListAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                startTestRunnerActivity(XmlLoader.getTestCaseId(groupPosition, childPosition));
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Driver.getInstance().isConnected()) {
            finish();
        }
    }

    private void createCollection() {
        collection = new LinkedHashMap<String, List<String>>();
        List<TestCaseGroup> groups = XmlLoader.getTestCaseGroups();
        for (TestCaseGroup group : groups) {
            collection.put(group.getName(), group.getTestCaseTitles());
        }
    }

    public void startTestRunnerActivity(String testCaseId) {
        TestCase testCase = XmlLoader.getTestCaseById(testCaseId);
        Intent intent = new Intent(this, TestRunnerActivity.class);
        intent.putExtra(TEST_ID, testCaseId);
        intent.putExtra(TEST_TITLE, testCase.getTitle());
        intent.putExtra(TEST_STAGES, testCase.getStagesAsArray());
        startActivity(intent);
    }
}

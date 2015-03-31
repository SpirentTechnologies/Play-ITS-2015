package com.testingtech.car2x.hmi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.testingtech.car2x.hmi.testcases.TestCase;
import com.testingtech.car2x.hmi.testcases.TestCaseGroup;
import com.testingtech.car2x.hmi.testcases.XmlLoader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestSelectorActivity extends ActionBarActivity {

    public final static String TEST_ID = "id";
    public final static String TEST_TITLE = "title";
    public final static String TEST_STAGES = "stages";
    private Map<String, List<String>> collection;
    private ExpandableListView expListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_selector);

        createCollection();
        expListView = (ExpandableListView) findViewById(R.id.listView);
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

    private void createCollection(){
        collection = new LinkedHashMap<String, List<String>>();
        List<TestCaseGroup> groups = XmlLoader.getTestCaseGroups();
        for (TestCaseGroup group : groups){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test_selector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.testingtech.car2x.hmi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestSelectorActivity extends ActionBarActivity {

    public final static String TEST_NUMBER = "number";
    public final static String TEST_TITLE = "name";
    public final static String TEST_STAGES = "stages";
    List<String> groupList;
    ArrayList<String>[] itemLists;
    Map<String, List<String>> collection;
    ExpandableListView expListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_selector);

        createGroups();
        createItems();
        createCollection();
        expListView = (ExpandableListView) findViewById(R.id.listView);
        final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(
                this, groupList, collection);
        expListView.setAdapter(expListAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                int testNo = 0;
                String[] stages = {};
                switch(groupPosition){
                    case 0:
                        switch(childPosition){
                            case 0:
                                testNo = 0;
                                stages = getResources().getStringArray(R.array.speed_test1_stages);
                                break;
                            case 1:
                                testNo = 1;
                                stages = getResources().getStringArray(R.array.speed_test2_stages);
                                break;
                            case 2:
                                testNo = 2;
                                stages = getResources().getStringArray(R.array.speed_test3_stages);
                                break;
                        }
                        break;
                    case 1:
                        switch(childPosition) {
                            case 0:
                                testNo = 3;
                                stages = getResources().getStringArray(R.array.door_test1_stages);
                                break;
                        }
                    case 2:
                        switch(childPosition) {
                            case 0:
                                testNo = 4;
                                stages = getResources().getStringArray(R.array.brake_test1_stages);
                                break;
                            case 1:
                                testNo = 5;
                                stages = getResources().getStringArray(R.array.brake_test2_stages);
                                break;
                            case 2:
                                testNo = 6;
                                stages = getResources().getStringArray(R.array.brake_test3_stages);
                                break;
                        }
                    case 3:
                        switch(childPosition) {
                            case 0:
                                testNo = 7;
                                stages = getResources().getStringArray(R.array.light_test1_stages);
                                break;
                        }
                    case 4:
                        switch(childPosition) {
                            case 0:
                                testNo = 8;
                                stages = getResources().getStringArray(R.array.engine_test1_stages);
                                break;
                        }
                    case 5:
                        switch(childPosition) {
                            case 0:
                                testNo = 9;
                                stages = getResources().getStringArray(R.array.steering_test1_stages);
                                break;
                        }
                }
                startTestRunnerActivity(testNo, itemLists[groupPosition].get(childPosition), stages);
                return true;
            }
        });
    }

    private void createGroups(){
        groupList = new ArrayList<>();
        groupList.add(getString(R.string.speed_group));
        groupList.add(getString(R.string.door_group));
        groupList.add(getString(R.string.brake_group));
        groupList.add(getString(R.string.light_group));
        groupList.add(getString(R.string.engine_group));
        groupList.add(getString(R.string.steering_group));
    }

    private void createItems(){
        itemLists = new ArrayList[6];
        itemLists[0] = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.speed_tests)));
        itemLists[1] = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.door_tests)));
        itemLists[2] = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.brake_tests)));
        itemLists[3] = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.light_tests)));
        itemLists[4] = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.engine_tests)));
        itemLists[5] = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.steering_tests)));
    }

    private void createCollection(){
        collection = new LinkedHashMap<String, List<String>>();
        for(int i = 0; i < 6; i++){
            collection.put(groupList.get(i), itemLists[i]);
        }
    }

    public void startTestRunnerActivity(int number, String title, String[] stages) {
        Intent intent = new Intent(this, TestRunnerActivity.class);
        intent.putExtra(TEST_NUMBER, number);
        intent.putExtra(TEST_TITLE, title);
        intent.putExtra(TEST_STAGES, stages);
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

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

    public final static String TEST_NAME = "drive";
    ArrayList<String> groupList;
    ArrayList<String> speedItems, doorItems, brakeItems, lightItems, engineItems, wheelItems;
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
                startDriveTest();
                return true;
            }
        });
    }

    private void createGroups() {
        groupList = new ArrayList<String>();
        groupList.add("Speed Tests");
        groupList.add("Door Tests");
        groupList.add("Break Tests");
        groupList.add("Light Tests");
        groupList.add("Engine Tests");
        groupList.add("Steering Wheel Tests");
    }

    private void createItems() {
        speedItems = new ArrayList<>(Arrays.asList("Speed up to 50 & slow down to 30 km/h",
                "Speed up to 30 & stop", "Drive in reverse"));
        doorItems = new ArrayList<>(Arrays.asList("Test open door with speed over 50 km/h"));
        brakeItems = new ArrayList<>(Arrays.asList("Test full brake with speed over 60 km/h",
                "Activate hand brake", "Test Brakes' efficiency"));
        lightItems = new ArrayList<>(Arrays.asList("Test headlamp status"));
        engineItems = new ArrayList<>(Arrays.asList("Test engine rotation speed"));
        wheelItems = new ArrayList<>(Arrays.asList("Test steering wheel angle"));
    }

    private void createCollection(){
        collection = new LinkedHashMap<String, List<String>>();
        collection.put(groupList.get(0), speedItems);
        collection.put(groupList.get(1), doorItems);
        collection.put(groupList.get(2), brakeItems);
        collection.put(groupList.get(3), lightItems);
        collection.put(groupList.get(4), engineItems);
        collection.put(groupList.get(5), wheelItems);
    }

    public void startDriveTest(){
        startTestRunnerActivity("drive");
    }

    public void startDoorTest(View view){
        startTestRunnerActivity("door");
    }

    public void startBreakTest(View view){
        startTestRunnerActivity("break");
    }

    public void startTestRunnerActivity(String test) {
        Intent intent = new Intent(this, TestRunnerActivity.class);
        intent.putExtra(TEST_NAME, test);
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

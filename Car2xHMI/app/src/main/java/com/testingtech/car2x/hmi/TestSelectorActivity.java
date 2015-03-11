package com.testingtech.car2x.hmi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.testingtech.car2x.R;


public class TestSelectorActivity extends ActionBarActivity {

    public final static String TEST_NAME = "drive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_selector);
    }

    public void startDriveTest(View view){
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

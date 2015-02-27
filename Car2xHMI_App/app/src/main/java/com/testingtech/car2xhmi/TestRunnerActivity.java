package com.testingtech.car2xhmi;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class TestRunnerActivity extends ActionBarActivity {

    private AnimationDrawable logoAnimation;
    private String testName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_runner);

        Intent intent = getIntent();
        testName = intent.getStringExtra(TestSelectorActivity.TEST_NAME);
        switch(testName){
            case "drive":
                setTitle(getString(R.string.title_activity_drive_test));
                break;
            case "door":
                setTitle(getString(R.string.title_activity_door_test));
                break;
            case "break":
                setTitle(getString(R.string.title_activity_break_test));
                break;
            default:
                setTitle("Test");
        }

        ImageView logoImage = (ImageView) findViewById(R.id.status_animation);
        logoImage.setBackgroundResource(R.drawable.animation_logo);
        logoAnimation = (AnimationDrawable) logoImage.getBackground();
    }

    public void startTest(View view) {
        TextView status = (TextView) findViewById(R.id.status_text);
        status.setText(getString(R.string.textview_running));
        logoAnimation.start();

        TextView results = (TextView) findViewById(R.id.results);
        TextView commands = (TextView) findViewById(R.id.commands);
        switch(testName){
            case "drive":
                // TODO: get speed and replace the 0
                results.setText(String.format(getString(R.string.textview_drive_results), 0));
                // TODO: if greater, if less: faster, slower
                commands.setText(String.format(getString(R.string.textview_drive_commands), "beschleunigen"));
                break;
            case "door":
                // TODO
                results.setText(String.format(getString(R.string.textview_door_results), "offen"));
                // TODO
                commands.setText(String.format(getString(R.string.textview_door_commands), "schließen"));
                break;
            case "break":
                // TODO
                results.setText(String.format(getString(R.string.textview_break_results), "angezogen"));
                // TODO
                commands.setText(String.format(getString(R.string.textview_break_commands), "lösen"));
                break;
            default:
                setTitle("Test");
        }
    }

    public void stopTest(View view) {
        TextView status = (TextView) findViewById(R.id.status_text);
        status.setText(getString(R.string.textview_not_running));
        TextView results = (TextView) findViewById(R.id.results);
        results.setText("");
        TextView commands = (TextView) findViewById(R.id.commands);
        commands.setText("");
        logoAnimation.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test_runner, menu);
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

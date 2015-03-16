package com.testingtech.car2x.hmi;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class TestRunnerActivity extends ActionBarActivity {

    private AnimationDrawable logoAnimation;
    private SocketClient socketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_runner);

        Intent intent = getIntent();
        String testName = intent.getStringExtra(TestSelectorActivity.TEST_NAME);
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
        TextView statusText = (TextView) findViewById(R.id.status_text);
        TextView socketConn = (TextView) findViewById(R.id.socket);
        ScrollView progress = (ScrollView) findViewById(R.id.progress);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar);
        Button btnStart = (Button) findViewById(R.id.button_start);
        Button btnStop = (Button) findViewById(R.id.button_stop);
        socketClient = new SocketClient(this, socketConn, progress, progressBar, logoAnimation,
                statusText, btnStart, btnStop);
        socketClient.execute();
    }

    public void stopTest(View view) {
        socketClient.cancel(true);
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

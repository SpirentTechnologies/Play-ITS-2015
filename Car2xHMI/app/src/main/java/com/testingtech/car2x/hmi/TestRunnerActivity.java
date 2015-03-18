package com.testingtech.car2x.hmi;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

public class TestRunnerActivity extends ActionBarActivity {

    private TextView statusText;
    private TextView socketConn;
    private ScrollView progress;
    private ProgressBar progressBar;
    private Button btnStart;
    private Button btnStop;
    private AnimationDrawable logoAnimation;
    private SocketClient socketClient;
    private TextToSpeech ttobj;

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

        ttobj = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    ttobj.setLanguage(Locale.UK);
                }
            }
        });
        statusText = (TextView) findViewById(R.id.status_text);
        socketConn = (TextView) findViewById(R.id.socket);
        progress = (ScrollView) findViewById(R.id.progress);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        btnStart = (Button) findViewById(R.id.button_start);
        btnStop = (Button) findViewById(R.id.button_stop);
    }

    public void startTest(View view) {
        socketClient = new SocketClient(this, socketConn, progress, progressBar, logoAnimation,
                statusText, btnStart, btnStop, ttobj);
        socketClient.execute();
    }

    public void stopTest(View view) {
        socketClient.cancel(true);
        socketClient.closeSocket();
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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(ttobj != null)
            ttobj.shutdown();
    }
}

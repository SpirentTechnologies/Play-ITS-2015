package com.testingtech.car2x.hmi;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.testingtech.car2x.hmi.driver.Driver;

import java.util.Locale;

public class TestRunnerActivity extends ActionBarActivity {

    private TextView statusText;
    private TextView noticeText;
    private ScrollView progress;
    private ProgressBar progressBar;
    private Button btnStart;
    private Button btnStop;
    private AnimationDrawable logoAnimation;
    private TextToSpeech speech;
    private int stageCount, testNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_runner);

        Globals.view = this;

        Intent intent = getIntent();
        testNumber = intent.getIntExtra(TestSelectorActivity.TEST_NUMBER, 0);
        String testTitle = intent.getStringExtra(TestSelectorActivity.TEST_TITLE);
        String[] testStages = intent.getStringArrayExtra(TestSelectorActivity.TEST_STAGES);

        setTitle(testTitle);
        TableLayout table = (TableLayout) findViewById(R.id.tableStages);
        for(int stage = 0; stage < testStages.length; stage++){
            TextView stageText = new TextView(this);
            stageText.setGravity(Gravity.CENTER);
            stageText.setTextSize(28);
            stageText.setText(testStages[stage]);
            table.addView(stageText);
        }

        ImageView logoImage = (ImageView) findViewById(R.id.status_animation);
        logoImage.setBackgroundResource(R.drawable.animation_logo);
        logoAnimation = (AnimationDrawable) logoImage.getBackground();

        speech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    speech.setLanguage(Locale.UK);
                }
            }
        });
        statusText = (TextView) findViewById(R.id.status_text);
        noticeText = (TextView) findViewById(R.id.notification);
        progress = (ScrollView) findViewById(R.id.progress);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        btnStart = (Button) findViewById(R.id.button_start);
        btnStop = (Button) findViewById(R.id.button_stop);
        stageCount = testStages.length;
    }

    /**
     * Start the test. Create a new AsyncThread and run it.
     * @param view Not used.
     */
    public void startTest(View view) {
        /*socketClient = new SocketClient(this, noticeText, progress, progressBar, logoAnimation,
                statusText, btnStart, btnStop, speech, stageCount, testNumber);
        socketClient.execute();*/

        new Thread( new Driver()).start();
        //new Thread(new TestConn()).start();
        Button b = (Button) findViewById(R.id.button_start);
        b.setEnabled(false);
        Button b2 = (Button) findViewById(R.id.button_stop);
        b2.setEnabled(true);
    }

    /**
     * Stop the running test. Cancel the AsyncThread and close the Socket.
     * @param view Not used.
     */
    public void stopTest(View view) {
        // TODO interrupt threads
        Button b = (Button) findViewById(R.id.button_start);
        b.setEnabled(true);
        Button b2 = (Button) findViewById(R.id.button_stop);
        b2.setEnabled(false);
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
        if(speech != null)
            speech.shutdown();
    }
}

package com.testingtech.car2x.hmi;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.testingtech.car2x.hmi.ttmanclient.Driver;

import java.util.Locale;

public class TestRunnerActivity extends AppCompatActivity {

    private String testId;
    private static TableLayout table;
    private static TextToSpeech speech;
    public static GuiUpdater guiUpdater;
    public static AsyncTimer timer;
    public Button btnStop;
    public TextView noticeText;
    private static Driver driver = Driver.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_runner);
        // get data from the parent activity
        Intent intent = getIntent();
        testId = intent.getStringExtra(TestSelectorActivity.TEST_ID);
        String testTitle = intent.getStringExtra(TestSelectorActivity.TEST_TITLE);
        String[] testStages = intent.getStringArrayExtra(TestSelectorActivity.TEST_STAGES);
        int stageCount = testStages.length;

        createGuiUpdater(stageCount);
        Globals.runnerActivity = this;
        // set and create GUI elements
        setTitle(testTitle);
        table = (TableLayout) findViewById(R.id.tableStages);
        for (String stage : testStages) {
            TextView stageText = new TextView(this);
            stageText.setGravity(Gravity.CENTER);
            stageText.setTextSize(28);
            stageText.setText(stage);
            table.addView(stageText);
        }
        // init text to speech component
        speech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    speech.setLanguage(Locale.UK);
                }
            }
        });
    }

    private void createGuiUpdater(int stageCount) {
        // get components from current activity
        TextView statusRunningText = (TextView) findViewById(R.id.status_text);
        ScrollView stages = (ScrollView) findViewById(R.id.progress);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar);
        Button btnStart = (Button) findViewById(R.id.button_start);
        ImageView logoImage = (ImageView) findViewById(R.id.status_animation);
        logoImage.setBackgroundResource(R.drawable.animation_logo);
        AnimationDrawable logoAnimation = (AnimationDrawable) logoImage.getBackground();
        noticeText = (TextView) findViewById(R.id.notification);
        btnStop = (Button) findViewById(R.id.button_stop);
        table = (TableLayout) findViewById(R.id.tableStages);
        // start the gui updater
        guiUpdater = new GuiUpdater(progressBar, logoAnimation, btnStart, btnStop,
                noticeText, statusRunningText, stages, table, stageCount);
    }

    /**
     * Start the test. Create a new AsyncThread and run it.
     *
     * @param view The parent runnerActivity.
     */
    public void startTest(View view) {
        if (driver.isConnected()) {
            Globals.currentTestCase = testId;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    driver.startTestCase();
                }
            }).start();
            guiUpdater.enableStartButton(false);
            guiUpdater.animateLogo(true);
            guiUpdater.setStatusText(getString(R.string.textview_running));
        }
        else {
            Toast.makeText(this, "Lost connection to TTman Server", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Stop the running test. Cancel the AsyncThread and close the Socket.
     *
     * @param view The parent runnerActivity.
     */
    public void stopTest(View view) {
        finishTestCase();
    }

    public static void finishTestCase() {
        driver.stopExecution();
        guiUpdater.enableStartButton(true);
        guiUpdater.animateLogo(false);
        guiUpdater.setStatusText("Test is not running.");

    }

    @SuppressWarnings("deprecation")
    public static void speakStageText(int stageNum) {
        TextView text = (TextView) table.getChildAt(stageNum);
        String toSpeak = text.getText().toString();
        if (Build.VERSION.SDK_INT < 21) {
            speech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            speech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "speak");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speech != null)
            speech.shutdown();
    }

}

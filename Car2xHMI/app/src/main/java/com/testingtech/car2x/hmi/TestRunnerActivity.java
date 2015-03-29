package com.testingtech.car2x.hmi;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
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
import com.testingtech.car2x.hmi.testcase.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class TestRunnerActivity extends ActionBarActivity {

    private  int testNumber;
    private static TableLayout table;
    private static TextToSpeech speech;
    public static PrintWriter writer;
    public static GuiUpdater guiUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_runner);
        // get data from the parent activity
        Intent intent = getIntent();
        testNumber = intent.getIntExtra(TestSelectorActivity.TEST_NUMBER, 0);
        String testTitle = intent.getStringExtra(TestSelectorActivity.TEST_TITLE);
        String[] testStages = intent.getStringArrayExtra(TestSelectorActivity.TEST_STAGES);
        int stageCount = testStages.length;

        createGuiUpdater(stageCount);

        Globals.view = this;
        // set and create GUI elements
        setTitle(testTitle);
        table = (TableLayout) findViewById(R.id.tableStages);
        for(String stage : testStages){
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
        // init log
        initLogFile();
    }

    private void createGuiUpdater(int stageCount){
        // get components from current activity
        TextView statusRunningText = (TextView) findViewById(R.id.status_text);
        TextView noticeText = (TextView) findViewById(R.id.notification);
        ScrollView stages = (ScrollView) findViewById(R.id.progress);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar);
        Button btnStart = (Button) findViewById(R.id.button_start);
        Button btnStop = (Button) findViewById(R.id.button_stop);
        ImageView logoImage = (ImageView) findViewById(R.id.status_animation);
        logoImage.setBackgroundResource(R.drawable.animation_logo);
        AnimationDrawable logoAnimation = (AnimationDrawable) logoImage.getBackground();
        // start the gui updater
        guiUpdater = new GuiUpdater(progressBar, logoAnimation, btnStart, btnStop,
                noticeText, statusRunningText, stages, table, stageCount);
    }

    private void initLogFile(){
        File path = getExternalFilesDir(null);//new File(Environment.getExternalStorageDirectory() + "TestingTech");
        File file = new File(path, "Log.txt");
        try {
            // Make sure the directory exists
            //path.mkdirs();
            //file.createNewFile();
            writer = new PrintWriter(file);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    /**
     * Start the test. Create a new AsyncThread and run it.
     * @param view The parent view.
     */
    public void startTest(View view) {
        new Thread(new Driver(TestCase.values()[testNumber])).start();
        guiUpdater.enableStartButton(false);
        guiUpdater.animateLogo(true);
        guiUpdater.setStatusText(getString(R.string.textview_running));
    }

    /**
     * Stop the running test. Cancel the AsyncThread and close the Socket.
     * @param view The parent view.
     */
    public void stopTest(View view) {
        // TODO interrupt threads, send stop message
        guiUpdater.enableStartButton(true);
        guiUpdater.animateLogo(false);
        guiUpdater.setStatusText(getString(R.string.textview_not_running));
    }

    @SuppressWarnings("deprecation")
    public static void speakStageText(int stageNum){
        TextView text = (TextView) table.getChildAt(stageNum);
        String toSpeak = text.getText().toString();
        if(Build.VERSION.SDK_INT < 21){
            speech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        } else{
            speech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "speak");
        }
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
        writer.close();
    }

    public static void writeLog(String msg){
        writer.println(msg);
        writer.flush();
    }

}

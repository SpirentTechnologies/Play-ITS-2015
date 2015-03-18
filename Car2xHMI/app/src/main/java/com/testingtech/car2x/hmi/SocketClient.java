package com.testingtech.car2x.hmi;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.testingtech.car2x.hmi.messages.ProgressMessage;
import com.testingtech.car2x.hmi.messages.TestCaseCommand;
import com.testingtech.car2x.hmi.messages.ControlMessage;
import com.testingtech.car2x.hmi.messages.Message;
import com.testingtech.car2x.hmi.messages.TestCase;
import com.testingtech.car2x.hmi.messages.VerdictMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

public class SocketClient extends AsyncTask<Void, Message, Message> {

    private Context context;
    private TextView debugText, statusRunning;
    private ScrollView scrollview;
    private ProgressBar progressBar;
    private AnimationDrawable logoAnimation;
    private Button btnStart, btnStop;
    private TextToSpeech ttobj;
    private int stageNum = 0;
    private Socket mySocket = null;

    public SocketClient(Context con, TextView tv, ScrollView sv, ProgressBar pb,
                        AnimationDrawable ad, TextView sr, Button start,
                        Button stop, TextToSpeech tts) {
        this.context = con;
        this.debugText = tv;
        this.scrollview = sv;
        this.logoAnimation = ad;
        this.statusRunning = sr;
        this.progressBar = pb;
        this.btnStart = start;
        this.btnStop = stop;
        this.ttobj = tts;
    }

    @Override
    protected void onPreExecute(){
        statusRunning.setText(context.getString(R.string.textview_loading));
        logoAnimation.start();
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
    }

    @Override
    protected Message doInBackground(Void... params) {
        ControlMessage controlMessage;
        Message message = null;
        try {
            mySocket = new Socket();
            mySocket.connect(new InetSocketAddress("10.0.2.2", 30000), 2000);
            statusRunning.setText(context.getString(R.string.textview_running));

            ObjectOutputStream oos = new ObjectOutputStream(mySocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(mySocket.getInputStream());

            controlMessage = new ControlMessage(
                    TestCase.TC_VEHICLE_SPEED_OVER_50,
                    new Date(),
                    TestCaseCommand.START
            );
            oos.writeObject(controlMessage);
            oos.flush();

            message = (Message) ois.readObject();
            while(!(message instanceof VerdictMessage) && !isCancelled()) {
                publishProgress(message);
                message = (Message) ois.readObject();
            }
            if(isCancelled()){
                controlMessage = new ControlMessage(
                        TestCase.TC_VEHICLE_SPEED_OVER_50,
                        new Date(),
                        TestCaseCommand.STOP
                );
                oos.writeObject(controlMessage);
                oos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mySocket.close();
            }catch (IOException | NullPointerException ioe){
                ioe.printStackTrace();
            }
        }
        return message;
    }

    @Override
    protected void onProgressUpdate(Message... progress) {
        super.onProgressUpdate(progress);
        String status = "";
        if(progress[0] instanceof ProgressMessage) {
            status = ((ProgressMessage) progress[0]).progress.toString();
        } else if(progress[0] instanceof ControlMessage) {
            status = ((ControlMessage) progress[0]).command.toString();
        } else if(progress[0] instanceof VerdictMessage) {
            status = ((VerdictMessage) progress[0]).verdict.toString();
        }
        debugText.setText(status);
        String toSpeak;
        switch(stageNum){
            case 0:
                toSpeak = context.getString(R.string.stage_start_engine);
                break;
            case 1:
                toSpeak = context.getString(R.string.stage_drive_50);
                break;
            case 2:
                toSpeak = context.getString(R.string.stage_down_to_30);
                break;
            case 3:
                toSpeak = context.getString(R.string.stage_roll_halt);
                break;
            default:
                toSpeak = "";
        }
        if(Build.VERSION.SDK_INT < 21){
            ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        } else{
            ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "speak");
        }
        // get the table as child of the scrollview
        TableLayout table = (TableLayout) scrollview.getChildAt(0);
        if(stageNum > 0) {
            // get the textview from the position above as child of the table
            TextView oldText = (TextView) table.getChildAt(stageNum - 1);
            // change color back to white
            oldText.setBackgroundColor(Color.TRANSPARENT);
        }
        // get the current textview as child of the table
        TextView text = (TextView) table.getChildAt(stageNum);
        // change color to red
        text.setBackgroundResource(R.drawable.rectangle_border_red);
        // scroll to current textview
        scrollview.smoothScrollTo(0, text.getTop());
        // update progress bar
        progressBar.setProgress(((stageNum + 1) * 100) / 4);
        // next stage
        if(progress[0] instanceof ProgressMessage) {
            stageNum = ((ProgressMessage)progress[0]).progress.ordinal() + 1;
        }
    }

    @Override
    protected void onPostExecute(Message result) {
        if(result != null) {
            if (result instanceof VerdictMessage)
                debugText.setText(((VerdictMessage) result).verdict.toString());
            else
                debugText.setText(result.toString());
        }
        finish();
    }

    @Override
    protected void onCancelled(){
        finish();
    }

    private void finish(){
        // get the table as child of the scrollview
        TableLayout table = (TableLayout) scrollview.getChildAt(0);
        if(stageNum > 0) {
            // get the last textview as child of the table
            TextView oldText = (TextView) table.getChildAt(stageNum - 1);
            // change color back to white
            oldText.setBackgroundColor(Color.TRANSPARENT);
        }
        statusRunning.setText(context.getString(R.string.textview_not_running));
        logoAnimation.stop();
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        closeSocket();
    }

    public void closeSocket(){
        try {
            if(mySocket != null)
                mySocket.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
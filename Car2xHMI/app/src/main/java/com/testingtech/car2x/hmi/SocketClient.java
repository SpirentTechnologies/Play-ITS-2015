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
import java.net.SocketTimeoutException;
import java.util.Date;

public class SocketClient extends AsyncTask<Void, Message, Message> {

    private Context context;
    private TextView debugText, statusRunningText;
    private ScrollView scrollview;
    private ProgressBar progressBar;
    private AnimationDrawable logoAnimation;
    private Button btnStart, btnStop;
    private TextToSpeech speech;
    private int stageNum = 0;
    private Socket mySocket = null;

    public SocketClient(Context con, TextView tv, ScrollView sv, ProgressBar pb,
                        AnimationDrawable ad, TextView sr, Button start,
                        Button stop, TextToSpeech tts) {
        this.context = con;
        this.debugText = tv;
        this.scrollview = sv;
        this.logoAnimation = ad;
        this.statusRunningText = sr;
        this.progressBar = pb;
        this.btnStart = start;
        this.btnStop = stop;
        this.speech = tts;
    }

    /**
     * Initialize the test: reset the debug text, set the status text to loading, animate logo,
     * disable start button, enable stop button.
     */
    @Override
    protected void onPreExecute(){
        debugText.setText("");
        debugText.setTextColor(Color.TRANSPARENT);
        statusRunningText.setText(context.getString(R.string.textview_loading));
        logoAnimation.start();
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
    }

    /**
     * Start the test: Connect a Socket, send a start message
     * @param params Contains nothing.
     * @return The last received message from the socket.
     */
    @Override
    protected Message doInBackground(Void... params) {
        ControlMessage controlMessage;
        Message socketMessage;
        // create a socket and try to connect it with a timeout of 2 seconds
        mySocket = new Socket();
        ObjectOutputStream oos;
        ObjectInputStream ois;
        try {
            mySocket.connect(new InetSocketAddress("192.168.87.148", 30000), 2000);
            oos = new ObjectOutputStream(mySocket.getOutputStream());
            ois = new ObjectInputStream(mySocket.getInputStream());
        }catch(SocketTimeoutException ste) {
            handleError("Connection failed after timeout. Try again.");
            return null;
        }catch (IOException ioe) {
            handleError("Connecting failed: " + ioe.getMessage());
            return null;
        }

        statusRunningText.setText(context.getString(R.string.textview_running));

        // define and send the start message
        controlMessage = new ControlMessage(
                    TestCase.TC_VEHICLE_SPEED_OVER_50,      // TODO send the right test case
                    new Date(),
                    TestCaseCommand.START
        );
        try {
            oos.writeObject(controlMessage);
            oos.flush();
        }catch (IOException ioe) {
            handleError("Sending the start message failed.");
            return null;
        }
        try {
            socketMessage = (Message) ois.readObject();
            while (!(socketMessage instanceof VerdictMessage) && !isCancelled()) {
                publishProgress(socketMessage);
                socketMessage = (Message) ois.readObject();
            }
        }catch (IOException ioe) {
            handleError("Receiving messages failed.");
            return null;
        }catch (ClassNotFoundException cnf) {
            handleError("Message format was wrong.");
            return null;
        }
        try{
            if(isCancelled()){
                controlMessage = new ControlMessage(
                        TestCase.TC_VEHICLE_SPEED_OVER_50,      // TODO send the right test case
                        new Date(),
                        TestCaseCommand.STOP
                );
                oos.writeObject(controlMessage);
                oos.flush();
            }
        } catch (IOException ioe){
            handleError("Sending the stop message failed.");
        }
        return socketMessage;
    }

    /**
     * Report an error message by displaying it on the GUI. Cancel the Thread.
     * @param message The message which is displayed.
     */
    private void handleError(String message){
        // TODO change the asyncthread to a runnable and use runonuithread  OR  put this in onprogresupdate
        /*
        debugText.setText(message);
        debugText.setTextColor(Color.RED);
        */
        System.out.println(message);
        cancel(true);
    }

    /**
     * Is called by doInBackground for updating the GUI.
     * @param progress The current received message.
     */
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
            speech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        } else{
            speech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "speak");
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

    /**
     * Is called after doInBackground if the Thread was not cancelled.
     * Shows the content of the last received message on the GUI.
     * @param result The last received message from the socket.
     */
    @Override
    protected void onPostExecute(Message result) {
        if(result != null) {
            if (result instanceof VerdictMessage)
                debugText.setText("Verdict: " + ((VerdictMessage) result).verdict.toString());
            else
                debugText.setText(result.toString());
        }
        finish();
    }

    /**
     * Is called after runInBackground if this Thread was cancelled. Calls the finish method.
     */
    @Override
    protected void onCancelled(){
        finish();
    }

    /**
     * Finishes all operations, closes the socket and resets the GUI texts.
     */
    private void finish(){
        // get the table as child of the scrollview
        TableLayout table = (TableLayout) scrollview.getChildAt(0);
        if(stageNum > 0) {
            // get the last textview as child of the table
            TextView oldText = (TextView) table.getChildAt(stageNum - 1);
            // change color back to white
            oldText.setBackgroundColor(Color.TRANSPARENT);
        }
        statusRunningText.setText(context.getString(R.string.textview_not_running));
        logoAnimation.stop();
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        closeSocket();
    }

    /**
     * Calls shutdown and close on the socket (if not null).
     */
    public void closeSocket(){
        try {
            if(mySocket != null) {
                mySocket.shutdownInput();
                mySocket.shutdownOutput();
                mySocket.close();
            }
        }catch(IOException ioe){
            System.out.println("Socket cannot be closed: " + ioe.getMessage());
        }
    }
}
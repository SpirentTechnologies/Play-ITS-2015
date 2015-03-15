package com.testingtech.car2x.hmi;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.testingtech.car2x.R;
import com.testingtech.car2x.hmi.messages.ProgressMessage;
import com.testingtech.car2x.hmi.messages.TestCaseCommand;
import com.testingtech.car2x.hmi.messages.ControlMessage;
import com.testingtech.car2x.hmi.messages.Message;
import com.testingtech.car2x.hmi.messages.TestCase;
import com.testingtech.car2x.hmi.messages.VerdictMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

public class SocketClient extends AsyncTask<Void, Message, Message> {

    private TextView textview, statusRunning;
    private ScrollView scrollview;
    private AnimationDrawable logoAnimation;
    private int stageNum = 0;

    public SocketClient(TextView tv, ScrollView sv, AnimationDrawable ad, TextView sr) {
        this.textview = tv;
        this.scrollview = sv;
        this.logoAnimation = ad;
        this.statusRunning = sr;
    }

    @Override
    protected Message doInBackground(Void... params) {
        ControlMessage controlMessage;
        Message message = null;
        try {
            Socket mySocket = new Socket("10.0.2.2", 30000);

            ObjectOutputStream oos = new ObjectOutputStream(mySocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(mySocket.getInputStream());

            controlMessage = new ControlMessage(
                    TestCase.TC_VEHICLE_SPEED_OVER_50,
                    new Date(),
                    TestCaseCommand.START
            );
            oos.writeObject(controlMessage);
            oos.flush();
            publishProgress(controlMessage);

            Thread.sleep(2000);
            message = (ProgressMessage) ois.readObject();
            publishProgress(message);

            message = (ProgressMessage) ois.readObject();
            publishProgress(message);

            message = (ProgressMessage) ois.readObject();
            publishProgress(message);

            message = (ProgressMessage) ois.readObject();
            publishProgress(message);

            message = (ProgressMessage) ois.readObject();
            publishProgress(message);

            Thread.sleep(2000);
            mySocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    @Override
    protected void onProgressUpdate(Message... progress) {
        super.onProgressUpdate(progress);
        if(progress[0] instanceof ProgressMessage) {
            textview.setText(((ProgressMessage) progress[0]).progress.toString());
        } else if(progress[0] instanceof ControlMessage) {
            textview.setText(((ControlMessage) progress[0]).command.toString());
        } else if(progress[0] instanceof VerdictMessage) {
            textview.setText(((VerdictMessage) progress[0]).verdict.toString());
            return;
        }
        // get the table as child of the scrollview
        TableLayout table = (TableLayout) scrollview.getChildAt(0);
        if(stageNum > 0) {
            // get the textview from the position above as child of the table
            TextView oldText = (TextView) table.getChildAt(stageNum - 1);
            // change color back to white
            oldText.setBackgroundColor(Color.WHITE);
        }
        // get the current textview as child of the table
        TextView text = (TextView) table.getChildAt(stageNum);
        // change color to red
        text.setBackgroundResource(R.drawable.rectangle_border_red);
        // scroll to current textview
        scrollview.smoothScrollTo(0, text.getTop());
        // next stage
        if(progress[0] instanceof ProgressMessage) {
            stageNum = ((ProgressMessage)progress[0]).progress.ordinal() + 1;
        }
    }

    @Override
    protected void onPostExecute(Message result) {
        if(result instanceof VerdictMessage)
            textview.setText(((VerdictMessage) result).verdict.toString());
        else
            textview.setText(result.toString());
        // get the table as child of the scrollview
        TableLayout table = (TableLayout) scrollview.getChildAt(0);
        // get the last textview as child of the table
        TextView oldText = (TextView) table.getChildAt(table.getChildCount() - 1);
        // change color back to white
        oldText.setBackgroundColor(Color.WHITE);
        statusRunning.setText("Test is not running.");
        logoAnimation.stop();
    }
}
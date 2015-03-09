package com.testingtech.car2xhmi;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.testingtech.car2xhmi.messages.ControlMessage;
import com.testingtech.car2xhmi.messages.StatusMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

public class SocketClient extends AsyncTask<Void, String, String> {

    private TextView textview;
    private ScrollView scrollview;
    private AnimationDrawable logoAnimation;
    private int stageNum = 0;

    public SocketClient(TextView tv, ScrollView sv, AnimationDrawable ad) {
        textview = tv;
        scrollview = sv;
        logoAnimation = ad;
    }

    @Override
    protected String doInBackground(Void... params) {
        ControlMessage controlMessage;
        StatusMessage statusMessage = new StatusMessage("init", new Date(), "init", 0);
        try {
            Socket mySocket = new Socket("10.0.2.2", 30000);

            ObjectOutputStream oos = new ObjectOutputStream(mySocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(mySocket.getInputStream());

            controlMessage = new ControlMessage(
                    "TC_VEHICLE_SPEED_OVER_50",
                    new Date(),
                    "start"
            );
            oos.writeObject(controlMessage);
            oos.flush();
            publishProgress("[Client] sending to server: " + controlMessage);

            Thread.sleep(2000);
            statusMessage = (StatusMessage) ois.readObject();
            publishProgress("[Client] received from server: " + statusMessage + "]");

            statusMessage = (StatusMessage) ois.readObject();
            publishProgress("[Client] received from server: " + statusMessage + "]");

            statusMessage = (StatusMessage) ois.readObject();
            publishProgress("[Client] received from server: " + statusMessage + "]");

            statusMessage = (StatusMessage) ois.readObject();
            publishProgress("[Client] received from server: " + statusMessage + "]");

            Thread.sleep(2000);
            mySocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "[Client] closing: [" + statusMessage + "].";
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        super.onProgressUpdate(progress);
        textview.setText(progress[0]);
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
        if(progress[0].contains("received"))    // TODO read stage from message
            stageNum += 1;
    }

    @Override
    protected void onPostExecute(String results) {
        textview.setText(results);
        // get the table as child of the scrollview
        TableLayout table = (TableLayout) scrollview.getChildAt(0);
        // get the last textview as child of the table
        TextView oldText = (TextView) table.getChildAt(table.getChildCount() - 1);
        // change color back to white
        oldText.setBackgroundColor(Color.WHITE);
        logoAnimation.stop();
    }
}
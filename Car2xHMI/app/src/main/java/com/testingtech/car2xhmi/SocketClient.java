package com.testingtech.car2xhmi;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class SocketClient extends AsyncTask<Void, Integer, String> {

    TextView textview;
    ScrollView scrollview;
    Context context;

    public SocketClient(TextView tv, ScrollView sv) {
        textview = tv;
        scrollview = sv;
    }

    @Override
    protected String doInBackground(Void... params) {
        int request = 115;
        int response = 0;
        try {
            Socket mySocket = new Socket("10.0.2.2", 30000);
            DataInputStream dis = new DataInputStream(mySocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(mySocket.getOutputStream());

            dos.writeInt(request);

            int i = 0;
            do{
                response = dis.readInt();
                publishProgress(response, i);
                i++;
            } while (response != 57);

            mySocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "[Client] received code [" + response + "]. Closing connection.";
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        textview.setText("[Client] received code [" + progress[0] + "]");
        TableLayout table = (TableLayout) scrollview.getChildAt(0);
        if(progress[1] > 0) {
            TextView oldText = (TextView) table.getChildAt(progress[1] - 1);
            oldText.setBackgroundColor(Color.WHITE);
        }
        TextView text = (TextView) table.getChildAt(progress[1]);
        text.setBackgroundResource(R.drawable.rectangle_border_red);
        scrollview.smoothScrollTo(0, text.getTop());
    }

    @Override
    protected void onPostExecute(String results) {
        textview.setText(results);
        TableLayout table = (TableLayout) scrollview.getChildAt(0);
        TextView oldText = (TextView) table.getChildAt(table.getChildCount() - 1);
        oldText.setBackgroundColor(Color.WHITE);
    }
}
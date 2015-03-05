package com.testingtech.car2xhmi;

import android.os.AsyncTask;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class SocketClient extends AsyncTask<Void, String, String> {

    TextView textview;

    public SocketClient(TextView tv) {
        textview = tv;
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
            publishProgress("[Client] sent request code [" + request + "]");

            do{
                response = dis.readInt();
                publishProgress("[Client] received code [" + response + "]");
            } while (response != 57);

            mySocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "[Client] received code [" + response + "]. Closing connection.";
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        super.onProgressUpdate(progress);
        textview.setText(progress[0]);
    }

    @Override
    protected void onPostExecute(String results) {
        textview.setText(results);
    }
}
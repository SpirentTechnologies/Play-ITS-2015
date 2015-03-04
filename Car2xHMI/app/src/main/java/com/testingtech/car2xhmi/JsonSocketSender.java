package com.testingtech.car2xhmi;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class JsonSocketSender extends AsyncTask<Void, Void, String> {

    @Override
    protected String doInBackground(Void... params) {
        sendJson("hi");
        return "hi";
    }

    @Override
    protected void onPostExecute(String results) {

    }

    public void sendJson(String content) {
        JSONObject json = new JSONObject();
        try {
            json.put("speed", 30);
            json.put("action", "start");
            Socket s = new Socket("192.168.0.100", 7777);
            OutputStreamWriter out = new OutputStreamWriter(s.getOutputStream());
            out.write(json.toString());
        } catch (IOException ioe) {
            // TODO
        } catch (JSONException je) {
            // TODO
        }
    }
}
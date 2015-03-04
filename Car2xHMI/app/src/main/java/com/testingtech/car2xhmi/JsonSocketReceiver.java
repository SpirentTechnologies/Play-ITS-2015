package com.testingtech.car2xhmi;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class JsonSocketReceiver extends AsyncTask<Void, Void, String> {

    private boolean testRunning = true;

    @Override
    protected String doInBackground(Void... params) {
        receiveJson(30000);
        return "hi";
    }

    @Override
    protected void onPostExecute(String results) {

    }

    public String receiveJson(int port) {
        String jsonString = "";
        ServerSocket server = null;
        Socket socket = null;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            // TODO
        }
        while (testRunning) {
            try {
                socket = server.accept();
            } catch (IOException | NullPointerException e) {
                // TODO
            }
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine = null;
                while ((inputLine = in.readLine()) != null) {
                    jsonString = jsonString.concat(inputLine);
                }
            } catch (IOException ioe) {
                // TODO
            }
            try {
                JSONObject json = new JSONObject(jsonString);
                // examples
                boolean breakActivated = json.getBoolean("breakActivated");
                int doorsOpen = json.getInt("doorsOpen");
                double speed = json.getDouble("speed");
                String brand = json.getString("brand");
            } catch (JSONException je) {
                // TODO
            }
        }
        return jsonString;
    }
}

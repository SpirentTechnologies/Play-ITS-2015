package com.testingtech.car2x.hmi;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.TextView;

public class AsyncTimer extends AsyncTask<Void, Integer, Void> {

    private TextView noticeText;
    private int time;

    public AsyncTimer(TextView text, int t){
        this.noticeText = text;
        this.time = t;
    }

    @Override
    protected Void doInBackground(Void... params) {
        for (int t = time; t > 0; t--) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace(Logger.writer);
            }
            if (isCancelled())
                return null;
            publishProgress(t);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... result) {
        super.onProgressUpdate(result);
        noticeText.setTextColor(Color.YELLOW);
        noticeText.setText("Time left: " + time);
    }

    @Override
    protected void onCancelled(){
        // test succeeded in time
        noticeText.setTextColor(Color.TRANSPARENT);
        noticeText.setText("");
    }

    @Override
    protected void onPostExecute(Void result) {
        // test time expired
        noticeText.setTextColor(Color.RED);
        noticeText.setText("The time expired. The test failed.");
    }
}

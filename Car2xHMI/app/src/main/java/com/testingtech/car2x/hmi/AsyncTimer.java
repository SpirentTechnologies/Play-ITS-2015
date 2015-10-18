package com.testingtech.car2x.hmi;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;

public class AsyncTimer extends AsyncTask<Void, Integer, Void> {

    private TextView noticeText;
    private int time;
    private Button btnStop;

    public AsyncTimer(Button btnStop, TextView text, int t) {
        this.btnStop = btnStop;
        this.noticeText = text;
        this.time = t - 1;      // - 1 for delay
    }

    @Override
    protected Void doInBackground(Void... params) {
        for (int t = time; t >= 0; t--) {
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
        noticeText.setBackgroundColor(Color.YELLOW);
        noticeText.setText(" Time left: " + result[0] + " ");
    }

    @Override
    protected void onCancelled() {
        // test succeeded in time
        noticeText.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onPostExecute(Void result) {
        // test time expired
        noticeText.setBackgroundColor(Color.RED);
        noticeText.setText(" The time expired. The test failed. ");
        btnStop.callOnClick();
    }
}

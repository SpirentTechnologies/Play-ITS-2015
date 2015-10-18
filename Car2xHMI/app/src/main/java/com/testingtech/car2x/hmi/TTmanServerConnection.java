package com.testingtech.car2x.hmi;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.testingtech.car2x.hmi.ttmanclient.Driver;

public class TTmanServerConnection extends AsyncTask<Void, Void, Boolean> {

    private ProgressDialog progress;
    private MainActivity mainActivity;
    private Driver driver = Driver.getInstance();

    public TTmanServerConnection(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onPreExecute() {
        progress = createProgressDialog();
        progress.show();
        super.onPreExecute();
    }

    private ProgressDialog createProgressDialog() {
        ProgressDialog progress = new ProgressDialog(mainActivity);
        progress.setMessage("Connecting to " + Globals.serverIp);
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(true);
        progress.setIndeterminate(true);
        progress.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel(true);
                    }
                });
        return progress;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return driver.connect();
    }

    @Override
    protected void onPostExecute(Boolean isConnected) {
        if (progress != null) {
            progress.dismiss();
        }
        if (!isConnected) {
            showConnectionError();
        } else {
            Toast.makeText(mainActivity, "Successfully connected to TTman server", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(mainActivity, TestSelectorActivity.class);
            mainActivity.startActivity(intent);
        }
    }

    private void showConnectionError() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainActivity);
        alertDialog.setTitle("Connection Error");
        alertDialog.setMessage("Cannot connect to TTman server at " + Globals.serverIp);
        alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}

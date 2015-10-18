package com.testingtech.car2x.hmi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.testingtech.car2x.hmi.testcases.XmlLoader;
import com.testingtech.car2x.hmi.ttmanclient.Driver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    IpAddressTextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Globals.mainActivity = this;
        saveDefaultResourceFiles();

        Logger.getInstance();
        XmlLoader.getInstance();
        PropertyReader.loadPropertyFile();
        Globals.serverPort = Integer.parseInt(PropertyReader.readProperty("ttman.server.port"));

        textView = (IpAddressTextView) findViewById(R.id.ip);
        textView.loadPrefs();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Driver driver = Driver.getInstance();
        if (driver.isConnected()) {
            driver.closeServerConnection();
            Toast.makeText(this, "Connection closed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDefaultResourceFiles() {
        File path = getExternalFilesDir(null);
//        File path = Globals.mainActivity.getFilesDir();
        addDefaultResourceFile(new File(path, "config.properties"), R.raw.config);
        addDefaultResourceFile(new File(path, "schema.xsd"), R.raw.schema);
        addDefaultResourceFile(new File(path, "source.xml"), R.raw.source);
    }

    private void addDefaultResourceFile(File resourceFile, int rawResourceId) {
        if (!resourceFile.exists()) {
            InputStream inputStream = getResources().openRawResource(rawResourceId);
            copyInputStreamToFile(inputStream, resourceFile);
        }
    }

    private void copyInputStreamToFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        AutoCompleteTextView ipText = (AutoCompleteTextView) findViewById(R.id.ip);
        outState.putString("ttManServerIP", ipText.getText().toString());
    }

    public void startActivityTest(View view) {
        AutoCompleteTextView ipText = (AutoCompleteTextView) findViewById(R.id.ip);
        Globals.serverIp = ipText.getText().toString();
        textView.addSearchInput(Globals.serverIp);
        new TTmanServerConnection(this).execute();
    }

    @Override
    protected void onStop() {
        textView.savePrefs();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        textView.savePrefs();
        Driver.getInstance().closeServerConnection();
        super.onDestroy();
    }
}

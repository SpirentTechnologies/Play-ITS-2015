package com.testingtech.car2x.hmi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.testingtech.car2x.hmi.testcases.XmlLoader;
import com.testingtech.car2x.hmi.ttmanclient.Driver;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Globals.mainActivity = this;
        Logger.getInstance();
        XmlLoader.getInstance();
        PropertyReader.loadPropertyFile();
        Globals.serverPort = Integer.parseInt(PropertyReader.readProperty("ttman.server.port"));
        Globals.clientPort = Integer.parseInt(PropertyReader.readProperty("ttman.client.port"));
    }

    public void startActivityTest(View view) {
        EditText ipText = (EditText) findViewById(R.id.ip);
        Globals.serverIp = ipText.getText().toString();

        Intent intent = new Intent(this, TestSelectorActivity.class);
        startActivity(intent);
    }
}

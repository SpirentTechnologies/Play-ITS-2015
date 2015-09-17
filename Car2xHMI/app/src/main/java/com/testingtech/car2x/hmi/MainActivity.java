package com.testingtech.car2x.hmi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.testingtech.car2x.hmi.testcases.XmlLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Globals.mainActivity = this;

        Logger.getInstance();
        XmlLoader.getInstance();
        PropertyReader.loadPropertyFile();
        Globals.serverPort = Integer.parseInt(PropertyReader.readProperty("ttman.server.port"));
    }

    public void startActivityTest(View view) {
        EditText ipText = (EditText) findViewById(R.id.ip);
        Globals.serverIp = ipText.getText().toString();

        Intent intent = new Intent(this, TestSelectorActivity.class);
        startActivity(intent);
    }
}

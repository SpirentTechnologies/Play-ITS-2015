package com.testingtech.car2x.hmi;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by bko on 22.09.15.
 */
public class IpAddressTextView extends AutoCompleteTextView {

    public static final String PREFS_IPADDRESSES = "IPAddresses";
    public static final String PREFS_IPADDRESS_HISTORY = "IPAddressHistory";
    private SharedPreferences settings;
    private Set<String> history;

    public IpAddressTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void loadPrefs() {
        settings = Globals.mainActivity.getSharedPreferences(PREFS_IPADDRESSES, 0);
        history = settings.getStringSet(PREFS_IPADDRESS_HISTORY, new HashSet<String>());
        udpdateAdapter();
    }


    public void savePrefs() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(PREFS_IPADDRESS_HISTORY, history);
        editor.apply();
    }


    private void udpdateAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                Globals.mainActivity, android.R.layout.simple_list_item_1, history.toArray(new String[history.size()]));
        setAdapter(adapter);
    }

    void addSearchInput(String input) {
        if (!history.contains(input)) {
            history.add(input);
            udpdateAdapter();
        }
    }
}

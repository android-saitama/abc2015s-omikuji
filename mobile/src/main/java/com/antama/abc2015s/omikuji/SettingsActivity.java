package com.antama.abc2015s.omikuji;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    public static final String PREF_PRINTER_IP_ADDRESS = "printer_ip_address";

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        EditTextPreference edittextPreference = (EditTextPreference) getPreferenceScreen().findPreference(PREF_PRINTER_IP_ADDRESS);
        edittextPreference.setSummary(edittextPreference.getText());

        final Cursor c = getContentResolver().query(ProviderMap.getContentUri(ProviderMap.ORACLE), null, null, null, null);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Log.d(TAG, TextUtils.join(", ", Arrays.asList(
                    c.getString(OracleProvider.COL_TOTAL),
                    c.getString(OracleProvider.COL_NUMBER),
                    c.getString(OracleProvider.COL_COLOR_NAME),
                    c.getString(OracleProvider.COL_COLOR_RGB),
                    c.getString(OracleProvider.COL_AREA),
                    c.getString(OracleProvider.COL_TITLE),
                    c.getString(OracleProvider.COL_DESCRIPTION))));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        EditTextPreference edittextPreference = (EditTextPreference) getPreferenceScreen().findPreference("printer_ip_address");
        edittextPreference.setSummary(edittextPreference.getText());
    }
}

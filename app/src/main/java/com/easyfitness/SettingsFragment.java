package com.easyfitness;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat {

    Toolbar top_toolbar = null;
    MainActivity mActivity = null;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static SettingsFragment newInstance(String name, int id) {
        SettingsFragment f = new SettingsFragment();



        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (MainActivity) getActivity();













        Preference myPref2 = findPreference("defaultUnit");
        myPref2.setOnPreferenceChangeListener((preference, newValue) -> {
            ListPreference listPreference = (ListPreference) preference;
            if (newValue instanceof String) {
                String boolVal = (String) newValue;


                updateSummary(listPreference, boolVal);
            }

            return true;
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String param) {

        setPreferencesFromResource(R.xml.settings2, param);

        SharedPreferences sharedPreferences;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ListPreference myPref2 = (ListPreference) findPreference("defaultUnit");
        String boolVal = sharedPreferences.getString("defaultUnit", "0");
        updateSummary(myPref2, boolVal);
    }

    private void updateSummary(ListPreference pref, String val) {
        int prefIndex = pref.findIndexOfValue(val);
        if (prefIndex >= 0) {

            pref.setSummary(getString(R.string.pref_preferredUnitSummary) + pref.getEntries()[prefIndex]);
        }
    }
}

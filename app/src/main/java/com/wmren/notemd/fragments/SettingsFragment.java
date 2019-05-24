package com.wmren.notemd.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;


import com.wmren.notemd.R;
import com.wmren.notemd.utilities.NoteAdapter;

public class SettingsFragment extends PreferenceFragment {

    public Preference colorMode;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // Load the Preferences from the XML file
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        colorMode = findPreference("color_mode");
        colorMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                NoteAdapter.colorMode = (boolean) newValue;
                return true;
            }
        });

    }

}

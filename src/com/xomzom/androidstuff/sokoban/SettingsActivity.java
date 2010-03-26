package com.xomzom.androidstuff.sokoban;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * The timer settings dialog activity.
 * @author dedi
 */
public class SettingsActivity extends PreferenceActivity
{
    /**
     * Activity was created. Set the view according to the XML settings state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}

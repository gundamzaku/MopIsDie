package com.tantanwen.mopisdie;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by gundamzaku on 2015/7/16.
 */
public class Prefs extends AppCompatActivity {

    private static final String OPT_MUSIC = "music";
    private static final boolean OPT_MUSIC_DEF = true;
    private static final String OPT_HINTS = "hints";
    private static final boolean OPT_HINTS_DEF = true;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);
        //addPreferencesFromResource(R.xml.settings);
        //getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragement()).commit();
        getFragmentManager().beginTransaction().replace(R.id.id_prefs, new PrefsFragement()).commit();
        //不再推荐直接让PreferenceActivity
    }


    public static class PrefsFragement extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,Preference preference) {
            System.out.println(preference.getKey());
            if("load_images_nowifi".equals(preference.getKey())) {

                CheckBoxPreference checkBoxPreference = (CheckBoxPreference)findPreference("load_images_nowifi");
                System.out.println(checkBoxPreference.isChecked());
                //EditTextPreference editTextPreference = (EditTextPreference)findPreference("individual_name");
                //让editTextPreference和checkBoxPreference的状态保持一致
                //editTextPreference.setEnabled(checkBoxPreference.isChecked());
            }
            // TODO Auto-generated method stub
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
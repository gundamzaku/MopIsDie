package com.tantanwen.mopisdie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private static Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);
        mContext = this;
        //addPreferencesFromResource(R.xml.settings);
        //getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragement()).commit();
        getFragmentManager().beginTransaction().replace(R.id.id_prefs, new PrefsFragement()).commit();
        //不再推荐直接让PreferenceActivity
    }


    public static class PrefsFragement extends PreferenceFragment {

        private SharedPreferences sp;
        private CheckBoxPreference checkBoxPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            sp = mContext.getSharedPreferences("mop_config",MODE_PRIVATE);
            checkBoxPreference = (CheckBoxPreference)findPreference("load_images_nowifi");
            Integer loadImagesNoWifi = sp.getInt("load_images_nowifi", 0);
            if(loadImagesNoWifi == 1){
                checkBoxPreference.setChecked(true);
            }else {
                checkBoxPreference.setChecked(false);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,Preference preference) {
            System.out.println(preference.getKey());
            if("load_images_nowifi".equals(preference.getKey())) {


                //把这个状态记下来。
                SharedPreferences.Editor editor = sp.edit();

                if(checkBoxPreference.isChecked() == true){
                    editor.putInt("load_images_nowifi", 1);
                    //1表示在没有wifi的情况下可以显示图片

                }else{
                    editor.putInt("load_images_nowifi", 0);
                    //0表示在没有wifi的情况下不能显示图片

                }
                editor.commit();
                //EditTextPreference editTextPreference = (EditTextPreference)findPreference("individual_name");
                //让editTextPreference和checkBoxPreference的状态保持一致
                //editTextPreference.setEnabled(checkBoxPreference.isChecked());
            }
            // TODO Auto-generated method stub
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
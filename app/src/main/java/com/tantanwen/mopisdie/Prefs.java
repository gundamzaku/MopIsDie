package com.tantanwen.mopisdie;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;

import com.tantanwen.mopisdie.utils.Sp;

/**
 * Created by gundamzaku on 2015/7/16.
 */
public class Prefs extends AppCompatActivity {

    /*
    private static final String OPT_MUSIC = "music";
    private static final boolean OPT_MUSIC_DEF = true;
    private static final String OPT_HINTS = "hints";
    private static final boolean OPT_HINTS_DEF = true;
    */
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

        private Sp sp;
        private CheckBoxPreference checkBoxPreference;
        private CheckBoxPreference checkBoxRefreshForceForum;
        private CheckBoxPreference checkBoxRefreshForceViewtopic;
        private ListPreference listSelectedFontStyle;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            //sp = mContext.getSharedPreferences("mop_config",MODE_PRIVATE);
            sp = Sp.getInstance(mContext).getTable("mop_config");
            //wifi
            checkBoxPreference = (CheckBoxPreference)findPreference("load_images_nowifi");
            Integer loadImagesNoWifi = sp.getInt("load_images_nowifi", 0);
            if(loadImagesNoWifi == 1){
                checkBoxPreference.setChecked(true);
            }else {
                checkBoxPreference.setChecked(false);
            }
            //refresh 1 use,0 dont use
            checkBoxRefreshForceForum = (CheckBoxPreference)findPreference("refresh_force_forum");
            Integer refreshForceForum = sp.getInt("refresh_force_forum", 0);
            if(refreshForceForum == 1){
                checkBoxRefreshForceForum.setChecked(true);
            }else {
                checkBoxRefreshForceForum.setChecked(false);
            }

            //refresh 1 use,0 dont use
            checkBoxRefreshForceViewtopic = (CheckBoxPreference)findPreference("refresh_force_viewtopic");
            Integer refreshForceViewtopic = sp.getInt("refresh_force_viewtopic", 0);
            if(refreshForceForum == 1){
                checkBoxRefreshForceViewtopic.setChecked(true);
            }else {
                checkBoxRefreshForceViewtopic.setChecked(false);
            }

            listSelectedFontStyle = (ListPreference)findPreference("selected_font_style");
            Integer selectedFontStyle = sp.getInt("selected_font_style", 0);
            listSelectedFontStyle.setValueIndex(selectedFontStyle);
            /*
            listSelectedFontStyle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Integer selectedFontStyle = sp.getInt("selected_font_style", 0);
                    System.out.println("output:" + selectedFontStyle);
                    preference.setDefaultValue(selectedFontStyle);

                    return false;
                }
            });*/
            listSelectedFontStyle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences.Editor editor = sp.edit();
                    System.out.println("input:" + Integer.parseInt(newValue.toString()));
                    Integer value = Integer.parseInt(newValue.toString());
                    editor.putInt("selected_font_style", value);
                    editor.commit();
                    listSelectedFontStyle.setValueIndex(value);
                    return false;
                }
            });
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,Preference preference) {

            SharedPreferences.Editor editor = sp.edit();

            if("load_images_nowifi".equals(preference.getKey())) {
                if(checkBoxPreference.isChecked() == true){
                    editor.putInt("load_images_nowifi", 1);
                    //1表示在没有wifi的情况下可以显示图片
                }else{
                    editor.putInt("load_images_nowifi", 0);
                    //0表示在没有wifi的情况下不能显示图片
                }
                //EditTextPreference editTextPreference = (EditTextPreference)findPreference("individual_name");
                //让editTextPreference和checkBoxPreference的状态保持一致
                //editTextPreference.setEnabled(checkBoxPreference.isChecked());
            }else if("refresh_force_viewtopic".equals(preference.getKey())) {

                if(checkBoxRefreshForceViewtopic.isChecked() == true){
                    editor.putInt("refresh_force_viewtopic", 1);
                }else{
                    editor.putInt("refresh_force_viewtopic", 0);
                }

            }else if("refresh_force_forum".equals(preference.getKey())) {

                if(checkBoxRefreshForceForum.isChecked() == true){
                    editor.putInt("refresh_force_forum", 1);
                }else{
                    editor.putInt("refresh_force_forum", 0);
                }

            }
            editor.commit();
            // TODO Auto-generated method stub
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
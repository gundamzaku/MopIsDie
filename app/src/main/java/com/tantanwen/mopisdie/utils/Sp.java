package com.tantanwen.mopisdie.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.WebView;

import com.tantanwen.mopisdie.R;

/**
 * Created by gundamzaku on 2015/8/13.
 */
public class Sp {

    private static Sp sp;
    private Context mContext;
    private SharedPreferences share;
    private String table;

    public static final Sp getInstance(Context mContext){

        sp = new Sp(mContext);
        return sp;
    }

    public Sp(Context mContext){
        this.mContext = mContext;
    }

    public Sp getTable(String table){
        this.table = table;
        share = this.mContext.getSharedPreferences(this.table,Context.MODE_PRIVATE);
        return sp;
    }

    public void setStyle(){
        Integer fontStyle = getInt("selected_font_style",0);
        if(fontStyle == 1){
            this.mContext.setTheme(R.style.AppTheme_medium);
        }else if(fontStyle == 2) {
            this.mContext.setTheme(R.style.AppTheme_big);
        }
    }

    public void setWebViewStyle(WebView webView){
        Integer fontStyle = getInt("selected_font_style",0);

        Integer size;
        if(fontStyle == 1){
            size = mContext.getResources().getInteger(R.integer.font_size_medium);
            webView.getSettings().setDefaultFontSize(size);
        }else if(fontStyle == 2) {
            size = mContext.getResources().getInteger(R.integer.font_size_big);
            webView.getSettings().setDefaultFontSize(size);
        }

    }

    public Integer getInt(String key,int defValue){
        return share.getInt(key,defValue);
    }

    public SharedPreferences.Editor edit(){
        return share.edit();
    }
}

package com.tantanwen.mopisdie.thread;

import android.os.Handler;

import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;

/**
 * Created by gundamzaku on 2015/8/4.
 */
public class PmThread implements Runnable{

    private final Handler mHandler;
    private String username;
    private String message;
    private String posttime;
    private String string;

    public PmThread(Handler mHandler){
        this.mHandler = mHandler;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setPosttime(String posttime){
        this.posttime = posttime;
    }

    @Override
    public void run() {
        Url.getInstance().setUrl(Config.SEND_PM_URL);

        Url.getInstance().addParameter("username", this.username);
        Url.getInstance().addParameter("message", this.message);
        Url.getInstance().addParameter("posttime", this.posttime);
        //Url.getInstance().addParameter("btnsend", "发送");
        string = Url.getInstance().doPost();
        System.out.println(string);
        //<div class="tips_header"><h1>提示信息</h1></div><div class="tips_content">传呼发送成功。<p>
        int offset = string.indexOf("<div class=\"tips_header\"><h1>提示信息</h1></div><div class=\"tips_content\">传呼发送成功。<p>");
        if(offset>0){
            this.mHandler.obtainMessage(Config.SUCCESS).sendToTarget();
        }else{
            //发送失败
            this.mHandler.obtainMessage(Config.FAILURE).sendToTarget();
        }

    }
}

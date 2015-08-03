package com.tantanwen.mopisdie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.AESCoder;
import com.tantanwen.mopisdie.utils.Config;

import org.apache.http.cookie.Cookie;
import java.util.Map;

public class Main extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button loginButton;
    private CheckBox saveLogin;
    private Context mContext;
    private LinearLayout readyLayout;
    private LinearLayout loginLayout;
    private SharedPreferences sp;
    private String usernameText;
    private String passwordText;
    private String deviceId;
    private Map cookies;
    private int limitLoginTimes = 0;
    private Thread td1;
    private boolean autoLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        loginButton = (Button)findViewById(R.id.login_button);
        saveLogin = (CheckBox)findViewById(R.id.save_login);

        initToolBar();

        //绑定事件
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        sp = getSharedPreferences("login_info",MODE_PRIVATE);
        usernameText = sp.getString("username", "");
        String passwordTextEncrypt = sp.getString("password","");
        if(usernameText.length()>0 && passwordTextEncrypt.length()>0){
            //开始解密码密码
            try {
                String outputData = AESCoder.decrypt(deviceId,passwordTextEncrypt);
                if(outputData.length()>0){
                    passwordText = outputData;
                    username.setText(usernameText);
                    password.setText(passwordText);
                    saveLogin.setChecked(true);
                    autoLogin = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(!cookiesGo()) {
            if(autoLogin == true && limitLoginTimes == 0){
                login();
            }
        }
    }

    private boolean cookiesGo(){
        //有cookie就直接跳走。
        cookies = Url.getInstance().getCookieStore();

        Cookie sid = (Cookie) cookies.get("LeemZfsid");
        Cookie fuc = (Cookie) cookies.get("LeemZfuc");
        if(sid != null && fuc != null) {
            if (sid.getValue().length() > 0 && fuc.getValue().length() > 0) {
                //走一个
                toForum();
                return true;
            }
        }
        return false;
        //System.out.println(cookies.get("LeemZfsid"));//LeemZfsid//LeemZfuc
    }
    @Override
    protected void onPause(){
        super.onPause();
        if(td1!=null){
            td1.interrupt();
        }
    }

    private void initToolBar(){

        Toolbar toolbar = (Toolbar)findViewById(R.id.id_toolbar);
        toolbar.setTitle(R.string.app_slogan);
        setSupportActionBar(toolbar);
        //toolbar.setNavigationIcon(R.drawable.logo);
        toolbar.setLogo(R.drawable.ic_favorite_outline_white_24dp);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;

        //设置控件的长度，为屏幕宽度的70%吧
        int widthScale = (int) Math.round(width * 0.7);
        username.setWidth(widthScale);
        password.setWidth(widthScale);
        loginButton.setWidth(widthScale);

    }

    private void login(){

        limitLoginTimes = 1;

        if(username == null || username.getText().length() <=0){
            Toast.makeText(getApplicationContext(), getResources().getString
                    (R.string.empty_username), Toast.LENGTH_SHORT).show();
            return;
        }
        if(password == null || password.getText().length() <=0){
            Toast.makeText(getApplicationContext(), getResources().getString
                    (R.string.empty_password), Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater flater = LayoutInflater.from(mContext);
        View layout = flater.inflate(R.layout.array_load_login, null);
        readyLayout = (LinearLayout)findViewById(R.id.ready_layout);
        readyLayout.setVisibility(View.VISIBLE);
        readyLayout.addView(layout);

        loginLayout = (LinearLayout)findViewById(R.id.login_layout);
        loginLayout.setVisibility(View.GONE);

        //启动线程
        MyThread myThread = new MyThread();
        td1 = new Thread(myThread);
        td1.start();

    }

    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行
            //msg.what = Config.FAILURE_NET_ERROR;
            switch(msg.what) {
                case Config.SUCCESS:
                    toForum();
                    break;
                case Config.FAILURE_LOGIN_INFO:
                    initLoginInfo();
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.login_info_error), Toast.LENGTH_LONG).show();
                    break;
                case Config.FAILURE_NET_ERROR:
                    initLoginInfo();
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.net_error), Toast.LENGTH_LONG).show();
                    break;
                case Config.FAILURE:
                    initLoginInfo();
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.login_error), Toast.LENGTH_LONG).show();
                    break;
                case Config.NOTHING:
                    break;
            }
        }
    };

    private void initLoginInfo(){
        Url.clearInstance();
        if(usernameText.length()>0 && passwordText.length()>0){
            username.setText(usernameText);
            password.setText(passwordText);
        }
        readyLayout.removeAllViews();
        readyLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.VISIBLE);
    }

    class MyThread implements Runnable{

        public void run(){

            Url.getInstance().setUrl(Config.LOGIN_URL);
            Url.getInstance().addParameter("username", String.valueOf(username.getText()));
            Url.getInstance().addParameter("password", String.valueOf(password.getText()));
            String string = Url.getInstance().doPost();
            if(td1.isInterrupted()){
                mHandler.obtainMessage(Config.NOTHING).sendToTarget();
                return;
            }
            if(string == "net_error"){
                mHandler.obtainMessage(Config.FAILURE_NET_ERROR).sendToTarget();
                return;
            }
            int position;
            position = string.indexOf("type=\"text/javascript\">top.location.href='index.asp';</script>");
            if(td1.isInterrupted()){
                mHandler.obtainMessage(Config.NOTHING).sendToTarget();
                return;
            }
            if(position>0) {

                //设置CookieStore
                Url.getInstance().setCookieStore();
                //需要解析是否是登录成功
                SharedPreferences.Editor editor = sp.edit();

                if(saveLogin.isChecked()==true){

                    //记住密码
                    try {
                        passwordText = AESCoder.encrypt(deviceId,String.valueOf(password.getText()));
                        //System.out.println("加密后:\t" + String.valueOf(password.getText()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //用putString的方法保存数据
                    if(passwordText.length()>0) {
                        usernameText = String.valueOf(username.getText());
                        editor.putString("username", usernameText);
                        editor.putString("password", passwordText);
                        editor.commit();
                    }
                }else{
                    editor.remove("username");
                    editor.remove("password");
                    editor.commit();
                }
                mHandler.obtainMessage(Config.SUCCESS).sendToTarget();
            }else{
                position = string.indexOf("该用户已被占用或者密码输入错误");
                if(position>0){
                    mHandler.obtainMessage(Config.FAILURE_LOGIN_INFO).sendToTarget();
                }else {
                    mHandler.obtainMessage(Config.FAILURE).sendToTarget();
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void toForum(){
        Intent list = new Intent(mContext,Forum.class);
        startActivity(list);
        finish();
    }
}

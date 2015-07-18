package com.tantanwen.mopisdie;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;

public class Main extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button loginButton;
    private Context mContext;
    private LinearLayout readyLayout;
    private LinearLayout loginLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        loginButton = (Button)findViewById(R.id.login_button);


        initToolBar();

        //绑定事件
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
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
        Thread td1 = new Thread(myThread);
        td1.start();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行

            switch(msg.what) {
                case 1001:
                    Intent list = new Intent(mContext,Forum.class);
                    startActivity(list);
                    finish();
                    break;
                case 1002:
                    initLoginInfo();
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.login_info_error), Toast.LENGTH_LONG).show();
                    break;
                case 1003:
                    initLoginInfo();
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.net_error), Toast.LENGTH_LONG).show();
                    break;
                case 9999:
                    initLoginInfo();
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.login_error), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void initLoginInfo(){
        readyLayout.removeAllViews();
        readyLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.VISIBLE);
        username.setText(null);
        password.setText(null);
    }

    class MyThread implements Runnable{

        public void run(){

            Url.getInstance().setUrl(Config.LOGIN_URL);
            System.out.println(String.valueOf(username.getText()));
            System.out.println(String.valueOf(password.getText()));
            Url.getInstance().addParameter("username", String.valueOf(username.getText()));
            Url.getInstance().addParameter("password", String.valueOf(password.getText()));
            String string = Url.getInstance().doPost();
            if(string == "net_error"){
                mHandler.obtainMessage(Config.FAILURE_NET_ERROR).sendToTarget();
                return;
            }
            int position;
            position = string.indexOf("type=\"text/javascript\">top.location.href='index.asp';</script>");
            if(position>0) {
                //需要解析是否是登录成功
                mHandler.obtainMessage(Config.SUCCESS).sendToTarget();
            }else{
                position = string.indexOf("该用户已被占用或者密码输入错误");
                if(position>0){
                    mHandler.obtainMessage(Config.FAILURE_LOGIN_INFO).sendToTarget();
                }else {
                    mHandler.obtainMessage(Config.FAILURE).sendToTarget();
                }
            }
            Log.d(Config.TAG, "" + string);
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
}

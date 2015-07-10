package com.tantanwen.mopisdie;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;

public class main extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button login_button;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        login_button = (Button)findViewById(R.id.login_button);

        initToolBar();

        //绑定事件
        login_button.setOnClickListener(new View.OnClickListener() {
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
    }

    private void login(){

        Log.d(Config.TAG,"点到了");
        Log.d(Config.TAG,"用户名："+username.getText());
        Log.d(Config.TAG,"用密码："+password.getText());

        if(username == null || username.getText().length() <=0){
            //Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_username), Toast.LENGTH_SHORT).show();
        }
        if(password == null || password.getText().length() <=0){
            //Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_password), Toast.LENGTH_SHORT).show();
        }

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
            }
        }
    };
    class MyThread implements Runnable{

        public void run(){

            Url.getInstance().setUrl(Config.LOGIN_URL);
            Url.getInstance().addParameter("username", "z钢弹");
            Url.getInstance().addParameter("password", "gundamzaku");
            String string = Url.getInstance().doPost();
            //需要解析是否是登录成功
            mHandler.obtainMessage(Config.SUCCESS).sendToTarget();
            //Log.d(Config.TAG,""+string);
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

package com.tantanwen.mopisdie;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tantanwen.mopisdie.adapter.ForumAdapter;
import com.tantanwen.mopisdie.thread.PmThread;
import com.tantanwen.mopisdie.utils.Config;
import com.tantanwen.mopisdie.utils.Utils;
import com.tantanwen.mopisdie.widget.ScrollListView;

import java.util.ArrayList;

public class SendPm extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText username;
    private EditText message;
    private EditText posttime;
    private Button btnsend;
    private LinearLayout mainLayout;
    private LinearLayout loadLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_pm);

        initToolBar();

        //定义变量
        username = (EditText)findViewById(R.id.username);
        message = (EditText)findViewById(R.id.message);
        posttime = (EditText)findViewById(R.id.posttime);
        btnsend = (Button)findViewById(R.id.btnsend);
        //系统时间
        posttime.setText(Utils.getCurrentTime());

        mainLayout = (LinearLayout)findViewById(R.id.main_layout);
        loadLayout = (LinearLayout)findViewById(R.id.load_layout);

        loadLayout.setVisibility(View.GONE);

        bindButton(btnsend);
    }

    //绑定事件
    private void bindButton(final Button button){
        if(button.getId() ==R.id.btnsend){
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    校验数据
                     */
                    if(username.getText().length()<=0){
                        Toast.makeText(getApplicationContext(), getResources().getString
                                (R.string.empty_message), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(message.getText().length()<=0){
                        Toast.makeText(getApplicationContext(), getResources().getString
                                (R.string.empty_message), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(posttime.getText().length()<=0){
                        Toast.makeText(getApplicationContext(), getResources().getString
                                (R.string.empty_message), Toast.LENGTH_LONG).show();
                        return;
                    }

                    mainLayout.setVisibility(View.GONE);
                    loadLayout.setVisibility(View.VISIBLE);
                    btnsend.setClickable(false);
                    PmThread pmThread = new PmThread(mHandler);
                    pmThread.setUsername(String.valueOf(username.getText()));
                    pmThread.setMessage(String.valueOf(message.getText()));
                    pmThread.setPosttime(String.valueOf(posttime.getText()));
                    Thread td1 = new Thread(pmThread);
                    td1.start();
                }
            });
        }
    }
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {//此方法在ui线程运行
            switch (msg.what) {
                case Config.SUCCESS:
                    //发送成功
                    username.setText("");
                    message.setText("");
                    posttime.setText(Utils.getCurrentTime());
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.send_done), Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.send_failed_retry), Toast.LENGTH_LONG).show();
                    break;
            }
            mainLayout.setVisibility(View.VISIBLE);
            loadLayout.setVisibility(View.GONE);
            btnsend.setClickable(true);
        }
    };

    private void initToolBar(){

        toolbar = (Toolbar)findViewById(R.id.id_toolbar);
        toolbar.setTitle(R.string.send_pm_message);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_pm, menu);
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

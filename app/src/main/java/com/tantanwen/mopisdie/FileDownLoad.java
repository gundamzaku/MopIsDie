package com.tantanwen.mopisdie;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.tantanwen.mopisdie.receiver.DownLoadReceiver;

public class FileDownLoad extends AppCompatActivity {

    private DownLoadReceiver dlr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_down_load);

        Button downLoadStop = (Button)findViewById(R.id.download_stop);

        downLoadStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("开始点击");
                Intent myIntent = new Intent();//创建Intent对象
                myIntent.setAction("com.tantanwen.mopisdie.DownLoadService");
                myIntent.putExtra("cmd", DownLoadReceiver.CMD_STOP_SERVICE);
                sendBroadcast(myIntent);//发送广播
            }
        });
    }

    @Override
    protected void onStart() {//重写onStart方法

        System.out.println("receiver is on start!");

        dlr = new DownLoadReceiver();
        IntentFilter filter = new IntentFilter();//创建IntentFilter对象
        filter.addAction("com.tantanwen.mopisdie.FileDownLoad");
        registerReceiver(dlr, filter);//注册Broadcast Receiver
        super.onStart();

    }

    @Override
    protected void onStop() {//重写onStop方法
        unregisterReceiver(dlr);//取消注册Broadcast Receiver
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_down_load, menu);
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

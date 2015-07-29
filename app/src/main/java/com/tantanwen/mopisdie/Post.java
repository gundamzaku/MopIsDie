package com.tantanwen.mopisdie;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static java.lang.Thread.sleep;

public class Post extends AppCompatActivity {

    private String[] f = {"0","1","2","3","4","5","6","7","8","9"};
    private Spinner face1,face2,face3;
    private CheckBox sig;
    private EditText message;
    private EditText aboutlink;
    private EditText imglink;
    private CheckBox ifanonymity;
    private CheckBox disableUpdate;
    private EditText sendcredits;
    private SharedPreferences sp;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private boolean isPost = false;
    private EditText title;
    private Spinner types;
    private Spinner price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        sp = getSharedPreferences("reply_info",MODE_PRIVATE);
        String messageTmpValue = sp.getString("message_post","");

        //获得所有的控件
        title           = (EditText)findViewById(R.id.title);
        types           = (Spinner)findViewById(R.id.types);
        message         = (EditText)findViewById(R.id.message);
        aboutlink       = (EditText)findViewById(R.id.aboutlink);
        imglink         = (EditText)findViewById(R.id.imglink);
        face1           = (Spinner) findViewById(R.id.face1);
        face2           = (Spinner) findViewById(R.id.face2);
        face3           = (Spinner) findViewById(R.id.face3);
        ifanonymity     = (CheckBox)findViewById(R.id.ifanonymity);
        disableUpdate   = (CheckBox)findViewById(R.id.disable_update);
        sig             = (CheckBox)findViewById(R.id.sig);    //是否签名
        price           = (Spinner)findViewById(R.id.price);

        if(messageTmpValue.length()>0){
            message.setText(messageTmpValue);
        }

        initToolBar();
        initFaceSelect();
        initCheckBox();
    }
    private void initToolBar(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setTitle(R.string.title_activity_post);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                System.out.println(title);
                if(title.getText().length()<=0){
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.empty_message), Toast.LENGTH_LONG).show();
                }else if(message.getText().length()<=0){
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.empty_message), Toast.LENGTH_LONG).show();
                }else {

                    if(isPost == true){
                        Toast.makeText(getApplicationContext(), getResources().getString
                                (R.string.page_submit_now), Toast.LENGTH_LONG).show();
                    }else {
                        isPost = true;
                        //启动线程
                        MyThread myThread = new MyThread();
                        Thread td1 = new Thread(myThread);
                        td1.start();
                        Post.this.finish();
                    }
                }

                return false;
            }
        });
    }

    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行

            switch(msg.what) {
                case Config.SUCCESS:
                    SharedPreferences.Editor editor = sp.edit();
                    editor.clear();
                    editor.commit();
                    break;
                case Config.FAILURE_MESSAGE_EMPTY:
                    break;
                case Config.FAILURE_LOGIN_INFO:
                    break;
                default:
                    break;
            }
            mNotificationManager.cancel(0);
            isPost = false;
        }
    };
    class MyThread implements Runnable{

        public void run(){
            notificationInit();
            monitorProgress();
            postData();
        }
    };
    private void successMessage(){
        Log.d(Config.TAG, "send done");
        mBuilder.build().contentView.setTextViewText(R.id.content_view_text1,"发送完成");
        mBuilder.setSmallIcon(R.drawable.ok);
        mBuilder.setTicker("帖子发送完成");
        monitorProgress();
    }
    private void failureMessage(){
        Log.d(Config.TAG,"send done");
        mBuilder.build().contentView.setTextViewText(R.id.content_view_text1,"发送失败");
        mBuilder.setSmallIcon(R.drawable.notok);
        mBuilder.setTicker("帖子发送失败");
        monitorProgress();
        //将数据写入临时文件中去
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("message_post", String.valueOf(message.getText()));
        editor.commit();
    }

    private void monitorProgress(){
        //mBuilder.build().contentView.setProgressBar(R.id.content_view_progress, 100, progress, false);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private void notificationInit(){
        //通知栏内显示下载进度条
        Intent intent=new Intent(this,Reply.class);//点击进度条，进入程序
        //PendingIntent pIntent=PendingIntent.getActivity(this, 0, intent, 0);
        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.status_bar_message_topic);//通知栏中进度布局
        mNotificationManager=(NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(Post.this);
        mBuilder.setSmallIcon(R.drawable.send);
        mBuilder.setTicker("正在发帖中……");
        mBuilder.setContentTitle("hello world");
        mBuilder.setContentText("no,you are stupid");
        mBuilder.setContent(remoteViews);
        /*
   				.setContentText("测试内容")
				.setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL))
				.setNumber(number)//显示数量
				.setTicker("测试通知来啦")//通知首次出现在通知栏，带上升动画效果的
				.setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示
				.setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
				.setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
				.setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
				.setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
				//Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
				.setSmallIcon(R.drawable.ic_launcher);
				*/
        //mBuilder.setContentIntent(pIntent);
    }
    private void postData(){

        Url.getInstance().setUrl(Config.POST_NEW_URL);

        Url.getInstance().addParameter("fid", "1");

        Url.getInstance().addParameter("title", String.valueOf(title.getText()));
        Url.getInstance().addParameter("types", String.valueOf(types.getSelectedItemPosition()));
        Url.getInstance().addParameter("message", String.valueOf(message.getText()));
        Url.getInstance().addParameter("aboutlink", String.valueOf(aboutlink.getText()));
        Url.getInstance().addParameter("imglink", String.valueOf(imglink.getText()));
        Url.getInstance().addParameter("face1", String.valueOf(f[face1.getSelectedItemPosition()]));
        Url.getInstance().addParameter("face2", String.valueOf(f[face2.getSelectedItemPosition()]));
        Url.getInstance().addParameter("face3", String.valueOf(f[face3.getSelectedItemPosition()]));
        if(ifanonymity.isChecked() == true){
            Url.getInstance().addParameter("ifanonymity", "1");
        }
        if(disableUpdate.isChecked() == true){
            Url.getInstance().addParameter("disableUpdate", "1");
        }
        if(sig.isChecked() == true){
            Url.getInstance().addParameter("sig", "1");
        }
        Url.getInstance().addParameter("price", String.valueOf(price.getSelectedItem().toString()));

        System.out.println(Url.getInstance().getParameter());

        String string = Url.getInstance().doPost();
        //System.out.println(string);

        Integer offset;
        offset = string.indexOf("<div class=\"tips_content\">您的帖子已经发布，现在将进入帖子。<p>");
        if(offset>0){
            //发送成功
            successMessage();
            sleepObtain(3000);
            mHandler.obtainMessage(Config.SUCCESS).sendToTarget();
        }else{
            offset = string.indexOf("<div class=\"tips_content\"><span class=\"pink\">请填写好帖子标题。</span><p>");
            if(offset>0){
                //没填内容
                failureMessage();
                sleepObtain(3000);
                mHandler.obtainMessage(Config.FAILURE_MESSAGE_EMPTY).sendToTarget();
            }else{
                //发送失败，请重新尝试
                failureMessage();
                sleepObtain(3000);
                mHandler.obtainMessage(Config.FAILURE_LOGIN_INFO).sendToTarget();
                //将发送的数据保存下来
            }
        }
    }
    private void sleepObtain(int time){
        try {
            sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void initCheckBox(){
        sig.setChecked(true);
    }

    private void initFaceSelect(){

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, f);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        face1.setAdapter(adapter1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, f);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        face2.setAdapter(adapter2);

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, f);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        face3.setAdapter(adapter3);

        face1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bindFaceSelect();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        face2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bindFaceSelect();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        face3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bindFaceSelect();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
    }

    private void bindFaceSelect() {
        int face1Pos = face1.getSelectedItemPosition();
        int face2Pos = face2.getSelectedItemPosition();
        int face3Pos = face3.getSelectedItemPosition();

        GifImageView imageGifView = (GifImageView) findViewById(R.id.imageGifView);
        if (face1Pos == 0 && face2Pos == 0 && face3Pos == 0) {
            //没有图片
            imageGifView.setImageResource(0);
            imageGifView.setMinimumHeight(0);
        } else {
            try {
                GifDrawable gifFromAssets = new GifDrawable(getAssets(), "mop/" + f[face1Pos] + f[face2Pos] + f[face3Pos] + ".gif");
                imageGifView.setMinimumWidth(gifFromAssets.getMinimumWidth() * 2);
                imageGifView.setMinimumHeight(gifFromAssets.getMinimumHeight() * 2);
                imageGifView.setImageDrawable(gifFromAssets);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //拼合而成图片
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post, menu);
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

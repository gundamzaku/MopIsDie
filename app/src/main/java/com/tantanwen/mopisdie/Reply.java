package com.tantanwen.mopisdie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.AESCoder;
import com.tantanwen.mopisdie.utils.Config;

import org.apache.http.NameValuePair;

import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class Reply extends AppCompatActivity {

    private String[] f = {"0","1","2","3","4","5","6","7","8","9"};
    private Spinner face1,face2,face3;
    private CheckBox sig;
    private String quote;
    private WebView webQuote;
    private Button clearQuoteButton;
    private EditText message;
    private EditText aboutlink;
    private EditText imglink;
    private CheckBox ifanonymity;
    private CheckBox disableUpdate;
    private EditText sendcredits;
    private boolean isPost = false;
    private String tid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        Intent intent = getIntent();
        quote   = intent.getStringExtra("quote");
        tid     = intent.getStringExtra("tid");

        if(tid == null){
            Toast.makeText(getApplicationContext(), getResources().getString
                    (R.string.parameter_error), Toast.LENGTH_LONG).show();
            finish();
        }
        System.out.println(quote);

        //获得所有的控件
        message         = (EditText)findViewById(R.id.message);
        aboutlink       = (EditText)findViewById(R.id.aboutlink);
        imglink         = (EditText)findViewById(R.id.imglink);
        face1           = (Spinner) findViewById(R.id.face1);
        face2           = (Spinner) findViewById(R.id.face2);
        face3           = (Spinner) findViewById(R.id.face3);
        ifanonymity     = (CheckBox)findViewById(R.id.ifanonymity);
        disableUpdate   = (CheckBox)findViewById(R.id.disable_update);
        sig             = (CheckBox)findViewById(R.id.sig);    //是否签名
        sendcredits     = (EditText)findViewById(R.id.sendcredits);
        webQuote        = (WebView)findViewById(R.id.webQuote);

        if(quote != null){
            webQuote.loadData(quote,"text/html; charset=UTF-8",null);
        }
        initToolBar();
        initFaceSelect();
        initCheckBox();
        initClearQuote();

        //检查
    }
    private void initClearQuote(){
        clearQuoteButton = (Button)findViewById(R.id.clearQuoteButton);
        clearQuoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(quote != null){
                    webQuote.loadData("",null,null);
                    quote = null;
                }
            }
        });
    }
    private void initToolBar(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setTitle(R.string.title_activity_reply);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

           if(message.getText().length()<=0){
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
               }
           }
            return false;
            }
        });
    }

    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行

            switch(msg.what) {
                case 1001:
                    break;
                default:
                    break;
            }

            isPost = false;
        }
    };

    class MyThread implements Runnable{

        public void run(){
            postData();
        }
    };
    private void postData(){

        Url.getInstance().setUrl(Config.POST_REPLY_URL);
        Url.getInstance().addParameter("tid", tid);
        Url.getInstance().addParameter("fid", "1");
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
        Url.getInstance().addParameter("sendcredits", String.valueOf(sendcredits.getText()));
        Url.getInstance().addParameter("quot_message", quote);

        String string = Url.getInstance().doPost();
        System.out.println(string);
        //<div class="tips_content">您的回复已经发布，现在将进入帖子。<p>
        //<div class="tips_content"><span class="pink">请填写好回复内容。</span><p>
        Integer offset;
        offset = string.indexOf("<div class=\"tips_content\">您的回复已经发布，现在将进入帖子。<p>");
        if(offset>0){
            //发送成功
            //要将现在的内容清空掉
        }else{
            offset = string.indexOf("<div class=\"tips_content\"><span class=\"pink\">请填写好回复内容。</span><p>");
            if(offset>0){
                //没填内容
            }else{
                //发送失败，请重新尝试
                //将发送的数据保存下来
            }
        }
        mHandler.obtainMessage(Config.FAILURE_LOGIN_INFO).sendToTarget();
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

    private void bindFaceSelect(){
        int face1Pos = face1.getSelectedItemPosition();
        int face2Pos = face2.getSelectedItemPosition();
        int face3Pos = face3.getSelectedItemPosition();

        GifImageView imageGifView = (GifImageView)findViewById(R.id.imageGifView);
        if(face1Pos==0 && face2Pos == 0 && face3Pos == 0){
            //没有图片
            imageGifView.setImageResource(0);
            imageGifView.setMinimumHeight(0);
        }else{

            try {
                GifDrawable gifFromAssets = new GifDrawable( getAssets(), "mop/"+f[face1Pos]+f[face2Pos]+f[face3Pos]+".gif" );
                imageGifView.setMinimumWidth(gifFromAssets.getMinimumWidth()*2);
                imageGifView.setMinimumHeight(gifFromAssets.getMinimumHeight()*2);
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
        getMenuInflater().inflate(R.menu.menu_reply, menu);
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

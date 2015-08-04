package com.tantanwen.mopisdie;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;
import com.tantanwen.mopisdie.utils.HTMLSpirit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyTopic extends AppCompatActivity {

    private Toolbar toolbar;
    private String pid;
    private String string;
    private EditText message;
    private Button buttonDelete;
    private Button buttonUpdate;
    private TextView loadingText;
    private EditText title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_topic);

        initToolBar();

        //得到上个页面传来的值
        Intent intent = getIntent();
        pid = intent.getStringExtra("pid");

        loadData();

    }

    private void loadData(){

        message = (EditText)findViewById(R.id.message);
        title = (EditText)findViewById(R.id.title);
        buttonDelete = (Button)findViewById(R.id.button_delete);
        buttonUpdate = (Button)findViewById(R.id.button_update);
        LoadingMyTopic = (LinearLayout)findViewById(R.id.loading_my_topic);
        loadingText = (TextView)findViewById(R.id.loading_text);
        if(pid.length()>0){
            //开启线程
            MyThread myThread = new MyThread();
            Thread td1 = new Thread(myThread);
            td1.start();
        }
    }

    private Pattern p;
    private Matcher m;
    private LinearLayout LoadingMyTopic;
    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行

            switch(msg.what) {
                case Config.SUCCESS:
                    //<input type="text" name="title" size="50" class="inputgrey" value="(*)" />
                    p = Pattern.compile("<input type=\"text\" name=\"title\" size=\"50\" class=\"inputgrey\" value=\"([\\s\\S]*?)\" />");
                    m = p.matcher(string);
                    if(m.find() == true){
                        String titleValue = m.group(1);
                        title.setText(titleValue);
                    }else {
                        title.setVisibility(View.GONE);
                    }
                    p = Pattern.compile("<input type=\"hidden\" id=\"message\" name=\"message\" value=\"([\\s\\S]*?)\" style=\"display:hidden\" />");
                    m = p.matcher(string);
                    if(m.find() == true){
                        String quote = m.group(1);

                        message.setText(HTMLSpirit.htmlDecode(quote));
                    }

                    LoadingMyTopic.setVisibility(View.GONE);

                    buttonDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog();
                        }
                    });

                    buttonUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            LoadingMyTopic.setVisibility(View.VISIBLE);
                            loadingText.setText("正在修改中……");

                            ModifyThread modifyThread = new ModifyThread();
                            Thread td1 = new Thread(modifyThread);
                            td1.start();
                        }
                    });
                    break;
                case Config.FAILURE_REGIST_NOOPEN:

                    loadingText.setText("登记资料……这种蛋痛的功能请转去开电脑来搞……");
                    break;
                case Config.FAILURE_ANONYMOUS:
                    loadingText.setText("这个人匿名了，你想照他的话去开电脑用道具吧。");
                    break;
                case Config.FAILURE:

                    loadingText.setText("加载失败了，请关掉吧。如果确认是你发的帖，关掉重开下，网站的网速有时候会比较慢。");
                    break;
                default:
                    break;
            }
        }
    };

    private Handler mHandlerModify = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行

            switch(msg.what) {
                case Config.SUCCESS:
                    loadingText.setText("修改完成，您可以关闭窗口并刷新");
                    break;
                case Config.FAILURE_MESSAGE_EMPTY:
                    LoadingMyTopic.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.modify_error), Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    class ModifyThread implements Runnable{

        public void run(){
            //?action=submitmodify
            Url.getInstance().setUrl(Config.POST_TOPIC_MODIFY_URL);
            if(title.getText().length()>0){
                Url.getInstance().addParameter("title", String.valueOf(title.getText()));
            }
            Url.getInstance().addParameter("fid","1");
            Url.getInstance().addParameter("pid", pid);
            Url.getInstance().addParameter("disable_autowap", "1");
            Url.getInstance().addParameter("btnsubmit", "提交修改");
            String messageTrans = String.valueOf(message.getText());

            //messageTrans = messageTrans.replaceAll("(\r\n|\r|\n|\n\r)", "<br>");
            messageTrans = messageTrans.replaceAll("\n", "<br>");
            Url.getInstance().addParameter("message", messageTrans);

            String string = Url.getInstance().doPost();
            System.out.println(string);
            Integer offset;

            offset = string.indexOf("<div class=\"tips_header\"><h1>提示信息</h1></div><div class=\"tips_content\">编辑完毕。");
            if(offset>0){
                //发送失败
                mHandlerModify.obtainMessage(Config.SUCCESS).sendToTarget();
            }else{
                //发送成功？
                offset = string.indexOf("<div class=\"tips_content\"><span class=\"pink\">回复不存在或者已经被删除。</span>");
                if(offset>0){
                    mHandlerModify.obtainMessage(Config.FAILURE_MESSAGE_EMPTY).sendToTarget();
                }else{
                    mHandlerModify.obtainMessage(Config.FAILURE_MESSAGE_EMPTY).sendToTarget();
                }
            }
        }
    };

    class MyThread implements Runnable{

        public void run(){
            Url.getInstance().setUrl(Config.POST_TOPICEDIT_URL+pid);
            Url.getInstance().addParameter("pid", String.valueOf(pid));
            string = Url.getInstance().doGet();

            Integer offset;

            offset = string.indexOf("<form name=\"tmodify\" method=\"post\" action=\"?action=submitmodify\" onKeyDown=\"fastpost('btnsubmit', event);\">");
            if(offset>0) {
                mHandler.obtainMessage(Config.SUCCESS).sendToTarget();
            }else{
                offset = string.indexOf("<a href=\"?action=searchpanel\">组合查询</a> <a href=\"?action=profilepanel\" style=\"background-color: #0F0\">登记资料</a>");
                if (offset > 0) {
                    //发送失败
                    mHandler.obtainMessage(Config.FAILURE_REGIST_NOOPEN).sendToTarget();
                } else {
                    //<td colspan="3">使用道具：</td>
                    offset = string.indexOf("<td colspan=\"3\">使用道具：</td>");
                    //if(string != "net_error") {
                    if (offset > 0) {
                        mHandler.obtainMessage(Config.FAILURE_ANONYMOUS).sendToTarget();
                    } else {
                        mHandler.obtainMessage(Config.FAILURE).sendToTarget();
                    }
                }
            }
        }
    };

    private Handler mHandlerDelete = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行

            switch(msg.what) {
                case Config.SUCCESS:
                    loadingText.setText("删除完成，您可以关闭窗口并刷新");
                    break;
                case Config.FAILURE_MESSAGE_EMPTY:
                    LoadingMyTopic.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.delete_failed), Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    class DeleteThread implements Runnable{

        public void run(){
            //?action=submitmodify

            Url.getInstance().setUrl(Config.POST_TOPIC_MODIFY_URL);
            Url.getInstance().addParameter("fid","1");
            Url.getInstance().addParameter("pid", pid);
            Url.getInstance().addParameter("btndelete", "删除回复");

            String string = Url.getInstance().doPost();

            Integer offset;

            offset = string.indexOf("<div class=\"tips_header\"><h1>提示信息</h1></div><div class=\"tips_content\">您的回复已经成功删除。<p>");
            if(offset>0){
                //发送失败
                mHandlerDelete.obtainMessage(Config.SUCCESS).sendToTarget();
            }else{
                //发送成功？
                offset = string.indexOf("<div class=\"tips_content\"><span class=\"pink\">回复不存在或者已经被删除。</span>");
                if(offset>0){
                    mHandlerDelete.obtainMessage(Config.FAILURE_MESSAGE_EMPTY).sendToTarget();
                }else{
                    mHandlerDelete.obtainMessage(Config.FAILURE_MESSAGE_EMPTY).sendToTarget();
                }
            }
        }
    };

    private void dialog(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("是否确认删除?"); //设置内容
        builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {

                LoadingMyTopic.setVisibility(View.VISIBLE);
                loadingText.setText("正在删除中……");

                DeleteThread deleteThread = new DeleteThread();
                Thread td1 = new Thread(deleteThread);
                td1.start();
                dialog.dismiss(); //关闭dialog

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }

    private void initToolBar(){

        toolbar = (Toolbar)findViewById(R.id.id_toolbar);
        toolbar.setTitle(R.string.title_activity_forum);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_topic, menu);
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

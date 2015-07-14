package com.tantanwen.mopisdie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;
import com.tantanwen.mopisdie.utils.HTMLSpirit;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewTopic extends AppCompatActivity {

    private String tid;
    private String string;
    private Toolbar toolbar;
    private Context mContext;
    private String fromJs_pid,fromJs_f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_topic);
        Intent intent = getIntent();
        tid = intent.getStringExtra("tid");
        //System.out.println("到这个页面我的tid是："+tid);
        mContext = this;

        initToolBar();
        initDrawer();
        ListView mMenuListView = (ListView) findViewById(R.id.menu_list);
        String[] mMenuTitles = getResources().getStringArray(R.array.array_left_menu_topic);
        mMenuListView.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mMenuTitles));
        mMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position){
                    case 0:
                        Intent list = new Intent(mContext,Reply.class);
                        mContext.startActivity(list);
                        //LayoutInflater inflater = getLayoutInflater();
                        //View layout = inflater.inflate(R.layout.activity_reply,(ViewGroup) findViewById(R.id.dialog));
                        //new AlertDialog.Builder(mContext).setTitle("我要回复").setView(layout).setPositiveButton("确定", null).setNegativeButton("取消", null).show();
                        break;
                    default:
                        break;
                }
            }
        });
        //启动线程
        MyThread myThread = new MyThread();
        Thread td1 = new Thread(myThread);
        td1.start();
    }
    private void initToolBar(){

        toolbar = (Toolbar)findViewById(R.id.id_toolbar);
        toolbar.setTitle(R.string.title_activity_forum);
        setSupportActionBar(toolbar);
    }
    private void initDrawer(){

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);//设置监听器
    }
    private Handler mHandler = new Handler() {
        @JavascriptInterface
        public void handleMessage (Message msg) {//此方法在ui线程运行

            Pattern p;
            Matcher m;

            switch(msg.what) {
                case 1001:
                    //System.out.println(string);
                    //启动正则，过滤数据
                    //拿标题
                    p = Pattern.compile("<a href=\"managetopic.asp\\?action=manageposts&fid=1&(.*?)\">(.*?)</a>");
                    m = p.matcher(string);
                    //System.out.println(m.groupCount());
                    //System.out.println(m.find());
                    //System.out.println(m.group(2));
                    if(m.find() == true){
                        toolbar.setTitle(HTMLSpirit.delHTMLTag(m.group(2)));
                    }

                    p = Pattern.compile("<hr color=\"black\" />([\\s\\S]*?)<hr color=\"black\" />");
                    m = p.matcher(string);
                    if(m.find() == true){
                        //"<style type=\"text/css\">a:link, a:visited { color: #000; text-decoration: none; }a:hover { background-color: #eff9d0; text-decoration: none; }</style>"
                        string = "<link rel=\"stylesheet\" href=\"file:///android_asset/common.css\" type=\"text/css\" />"+m.group(1);//
                    }else{
                        string = "";
                    }
                    //return shows3
                    string = string.replaceAll("return shows3","return injectedObject.shows3");
                    string = string.replaceAll("return shows","return injectedObject.shows");
                    string = string.replaceAll("onclick=\"showquot","onclick=\"injectedObject.showquot");
                    WebView webView = (WebView)findViewById(R.id.webView);
                    webView.setBackgroundColor(0); // 设置背景色
                    webView.getSettings().setJavaScriptEnabled(true);
                    //webView.requestFocus(View.FOCUS_DOWN);

                    webView.addJavascriptInterface(new JsObject(), "injectedObject");

                    webView.loadDataWithBaseURL("",string, "text/html; charset=UTF-8", null,null);
                    //webView.loadUrl("javascript:alert(injectedObject.toString())");
                    //Log.d(Config.TAG,"结束");
                    break;
                case 2001:
                    //System.out.println(string);
                    //<script type="text/javascript">$('quot_message').value=''</script>
                    p = Pattern.compile("<script type=\"text/javascript\">\\$\\('quot_message'\\).value='([\\s\\S]*?)'</script>");
                    m = p.matcher(string);
                    if(m.find() == true){
                        String quote = m.group(1);
                        //页面跳转，并把值带过去
                        Intent list = new Intent(mContext,Reply.class);
                        list.putExtra("quote",quote);
                        mContext.startActivity(list);
                    }
                    break;
            }
        }
    };

    class JsObject {
        @JavascriptInterface
        public boolean shows3(Objects href) {

            Toast.makeText(getApplicationContext(), "这有啥好看的？看自己小鸡鸡去。", Toast.LENGTH_SHORT).show();
            return false;
        }
        @JavascriptInterface
        public boolean shows(Objects href) {

            Toast.makeText(getApplicationContext(), "发送短消息", Toast.LENGTH_SHORT).show();
            return false;
        }
        @JavascriptInterface
        public void showquot(String pid,String f) {

            fromJs_pid = pid;
            fromJs_f = f;
            //启动线程
            CpThread cpThread = new CpThread();
            Thread td1 = new Thread(cpThread);
            td1.start();
            //组织成url
            //http://fuyuncun.com/topiccp.asp?action=ajaxquot&pid=129518&f=1
           //Toast.makeText(getApplicationContext(), "引用", Toast.LENGTH_SHORT).show();

        }
    }

    class MyThread implements Runnable{

        public void run(){

            Url.getInstance().setUrl(Config.VIEW_TOPIC_URL+tid);
            string = Url.getInstance().doGet();
            //需要解析是否是登录成功
            mHandler.obtainMessage(Config.SUCCESS).sendToTarget();
            //Log.d(Config.TAG,""+string);
        }
    };

    class CpThread implements Runnable{

        public void run(){

            Url.getInstance().setUrl(Config.VIEW_TOPIC_CP_URL+"&pid="+fromJs_pid+"&f="+fromJs_f);
            string = Url.getInstance().doGet();
            //需要解析是否是登录成功
            mHandler.obtainMessage(Config.SUCCESS_02).sendToTarget();
            //Log.d(Config.TAG,""+string);
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_topic, menu);
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

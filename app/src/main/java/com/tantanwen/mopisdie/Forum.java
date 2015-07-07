package com.tantanwen.mopisdie;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.tantanwen.mopisdie.adapter.ForumAdapter;
import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Forum extends Activity {

    private ArrayList<String> strs = new ArrayList<String>();
    private ListView forumList;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        mContext = this;
        //启动线程
        MyThread myThread = new MyThread();
        Thread td1 = new Thread(myThread);
        td1.start();
        //httpRequest.setHeader("Cookie", "JSESSIONID=" + COOKIE);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行
            switch(msg.what) {
                case 1001:
                    //ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(mContext,R.layout.array_forum,strs);
                    ForumAdapter adapter = new ForumAdapter(mContext);
                    adapter.setItems(strs);
                    //拿到数据
                    forumList = (ListView)findViewById(R.id.forum_list);
                    forumList.setAdapter(adapter);
            }
        }
    };
    class MyThread implements Runnable{

        public void run(){

            Url.getInstance().setUrl(Config.FORUM_URL);
            String string = Url.getInstance().doGet();
            //Pattern p = Pattern.compile("<a href=\"viewtopic.asp/?fid=1&tid=(.*?)\" title='(.*?)'>(.*?)></a><br />");
            Pattern p = Pattern.compile("<a href=\"viewtopic.asp\\?fid=1&tid=(.*?)\" title='(.*?)'>(.*?)</a><br />");
            Matcher m = p.matcher(string);

            Log.d(Config.TAG, String.valueOf(m));
            while (m.find()) {
                strs.add(m.group(3));
            }
            mHandler.obtainMessage(Config.SUCCESS).sendToTarget();
            /*
            for (String s : strs){
                System.out.println(s);
            }*/
            //<a href="viewtopic.asp?fid=1&tid=5406" title='【2015-7-7 10:35:09 猫晓扑】'>【法律相关】关于土地纠纷 (19/778)</a><br />
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forum, menu);
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

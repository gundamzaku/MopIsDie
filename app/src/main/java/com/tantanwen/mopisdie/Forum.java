package com.tantanwen.mopisdie;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tantanwen.mopisdie.adapter.ForumAdapter;
import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Forum extends AppCompatActivity {

    private ArrayList<String[]> strs = new ArrayList<String[]>();
    private ListView forumList;
    private Context mContext;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        mContext = this;

        initToolBar();
        initDrawer();

        ListView mMenuListView = (ListView) findViewById(R.id.menu_list);
        String[] mMenuTitles = getResources().getStringArray(R.array.array_left_menu_forum);
        mMenuListView.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mMenuTitles));
        mMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(position);
                switch (position){
                    case 0:
                        Intent list = new Intent(mContext,Post.class);
                        mContext.startActivity(list);
                        break;
                    case 1:
                        System.out.println("传呼");
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
        //httpRequest.setHeader("Cookie", "JSESSIONID=" + COOKIE);
    }

    private void initToolBar(){

        toolbar = (Toolbar)findViewById(R.id.id_toolbar);
        toolbar.setTitle(R.string.title_activity_forum);
        setSupportActionBar(toolbar);
        //toolbar.setNavigationIcon(R.drawable.logo);
        //toolbar.setLogo(R.drawable.ic_favorite_outline_white_24dp);
    }
    private void initDrawer(){

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);//设置监听器
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

            //Log.d(Config.TAG, String.valueOf(m));
            while (m.find()) {
                strs.add(new String[]{m.group(1), m.group(3)});
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

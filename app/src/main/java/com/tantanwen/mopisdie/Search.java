package com.tantanwen.mopisdie;

import android.content.Context;
import android.content.res.Resources;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.tantanwen.mopisdie.adapter.ForumAdapter;
import com.tantanwen.mopisdie.thread.SearchThread;
import com.tantanwen.mopisdie.utils.Config;

import java.util.ArrayList;

public class Search extends AppCompatActivity {

    public static boolean isLock = false; //锁
    private Button buttonSearch;
    private Button buttonMySend;
    private Button buttonMyReply;
    private EditText keyword;
    private Spinner searchtype;
    private SearchThread search;
    private ArrayList strs;
    private Context mContext;
    private ListView forumList;
    private LinearLayout smoothLayout;
    private LinearLayout searchLayout;
    private LinearLayout forumLayout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mContext = this;
        initToolBar();
        //收取控件
        forumList = (ListView)findViewById(R.id.forum_list);
        forumLayout = (LinearLayout)findViewById(R.id.forum_layout);
        smoothLayout = (LinearLayout)findViewById(R.id.smooth_layout);
        searchLayout = (LinearLayout)findViewById(R.id.search_layout);

        buttonSearch = (Button)findViewById(R.id.button_search);
        buttonMySend = (Button)findViewById(R.id.button_my_send);
        buttonMyReply = (Button)findViewById(R.id.button_my_reply);

        keyword = (EditText)findViewById(R.id.keyword);
        searchtype = (Spinner)findViewById(R.id.searchtype);
        search = new SearchThread(mHandler);
        //绑定查询
        bindSearch();
        //绑定自发
        bindMySend();
        //绑定自回
        bindMyReply();
    }

    private void initToolBar(){

        toolbar = (Toolbar)findViewById(R.id.id_toolbar);
        toolbar.setTitle(R.string.title_activity_search);
        setSupportActionBar(toolbar);
    }

    private void bindSearch(){
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch();
                if (isLock == true){
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.repeat_submit), Toast.LENGTH_SHORT).show();
                    return;
                }
                //启动线程
                search.setType(1);
                search.setKeyword(String.valueOf(keyword.getText()));
                Resources res =getResources();
                String[] searchTypes = res.getStringArray(R.array.search_types);
                search.setSearchType(searchTypes[searchtype.getSelectedItemPosition()]);
                Thread td = new Thread(search);
                td.start();
            }
        });
    }

    private void bindMyReply(){
        //查我自己的回复
        buttonMyReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch();
                if (isLock == true) {
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.repeat_submit), Toast.LENGTH_SHORT).show();
                    return;
                }
                //启动线程
                search.setType(2);
                Thread td = new Thread(search);
                td.start();
            }
        });
    }
    private void bindMySend(){
        //查我自己的帖子
        buttonMySend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch();
                if (isLock == true){
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.repeat_submit), Toast.LENGTH_SHORT).show();
                    return;
                }
                //启动线程
                search.setType(3);
                Thread td = new Thread(search);
                td.start();
            }
        });
    }

    private void startSearch(){
        //隐藏查询框
        forumLayout.setVisibility(View.GONE);
        searchLayout.setVisibility(View.GONE);
        //显示进度条
        smoothLayout.setVisibility(View.VISIBLE);
    }

    private void endSearch(){
        //隐藏查询框
        forumLayout.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.VISIBLE);
        //显示进度条
        smoothLayout.setVisibility(View.GONE);
    }


    private ForumAdapter adapter;
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {//此方法在ui线程运行

            search.unLockStatus();
            switch (msg.what){
                case Config.SUCCESS:
                    strs = search.getData();//得到数据

                    if(adapter == null) {
                        adapter = new ForumAdapter(mContext);
                    }
                    adapter.setItems(strs);
                    forumList.setAdapter(adapter);
                    //forumList.deferNotifyDataSetChanged();//重新加载数据
                    break;
                default:
                    break;
            }
            endSearch();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
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

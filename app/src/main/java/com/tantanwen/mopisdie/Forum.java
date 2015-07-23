package com.tantanwen.mopisdie;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tantanwen.mopisdie.adapter.ForumAdapter;
import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;
import com.tantanwen.mopisdie.widget.ScrollListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Forum extends AppCompatActivity implements ScrollListView.OnRefreshListener, ScrollListView.OnLoadListener {

    private ArrayList<String[]> strs = new ArrayList<>();
    private ScrollListView forumList;
    private Context mContext;
    private Toolbar toolbar;
    private int what;
    private int page = 1;
    //只让你翻20次
    private int pageMax = 2;
    private DrawerLayout mDrawerLayout;
    private LinearLayout forumLayout;
    private Button button_reload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        mContext = this;

        initToolBar();
        initDrawer();

        forumList = (ScrollListView)findViewById(R.id.forum_list);

        ListView mMenuListView = (ListView) findViewById(R.id.menu_list);
        String[] mMenuTitles = getResources().getStringArray(R.array.array_left_menu_forum);
        mMenuListView.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mMenuTitles));
        mMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(position);
                Intent list;
                switch (position){
                    case 0:
                        list = new Intent(mContext,Post.class);
                        mContext.startActivity(list);
                        break;
                    case 1:
                        list = new Intent(mContext,Pm.class);
                        mContext.startActivity(list);
                        break;
                    case 2: //设置
                        //加载PrefFragment
                        list = new Intent(mContext,Prefs.class);
                        mContext.startActivity(list);
                        /*
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        PrefFragment prefFragment = new PrefFragment();
                        transaction.add(android.R.id.content, prefFragment);
                        //getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragement()).commit();
                        transaction.commit();
                        */
                        break;
                    case 3:
                        loadData(ScrollListView.LOADFIRST);
                        initReloadView();
                        button_reload.setVisibility(View.GONE);
                        tipReload.setText(R.string.start_reload);
                        refreshingReload.setVisibility(View.VISIBLE);

                        //隐藏列表
                        forumList.setVisibility(View.GONE);
                        mDrawerLayout.closeDrawers();
                        break;
                    case 4:
                        finish();
                        break;
                    case 5:
                        finish();
                        break;
                    default:
                        break;
                }
            }
        });

        initReloadView();
        button_reload.setVisibility(View.GONE);
        tipReload.setText(R.string.start_reload);
        refreshingReload.setVisibility(View.VISIBLE);

        loadData(ScrollListView.LOADFIRST);
    }

    private void loadData(int what){
        //启动线程
        strs.clear();
        this.what = what;
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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);//设置监听器

    }

    public void initReloadView(){

        //调出加载页面
        forumLayout = (LinearLayout)findViewById(R.id.forum_layout);

        View failureReloadHeader = forumLayout.findViewById(R.id.failure_reload_header);

        forumList.setVisibility(View.GONE);

        if(failureReloadHeader == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            reloadHeader = inflater.inflate(R.layout.failure_reload_header, forumLayout);
        }else{
            reloadHeader = failureReloadHeader;
        }

        button_reload = (Button)reloadHeader.findViewById(R.id.button_reload);
        tipReload = (TextView)reloadHeader.findViewById(R.id.tip_reload);
        refreshingReload = (ProgressBar)reloadHeader.findViewById(R.id.refreshing_reload);
    }

    private ForumAdapter adapter;
    private TextView tipReload;
    private ProgressBar refreshingReload;
    private ArrayList<String[]> dataItems = new ArrayList<>();
    private View reloadHeader;

    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行
            switch(msg.what) {
                case Config.SUCCESS:

                    //如果我是第一次加载或是刷新
                    if(what != ScrollListView.LOAD){
                        adapter = null;
                        if(dataItems.size()>0) {
                            dataItems.clear();
                        }
                        System.out.println(dataItems);
                    }
                    dataItems.addAll((ArrayList<String[]>) strs.clone());

                    //ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(mContext,R.layout.array_forum,strs);
                    if(adapter == null) {
                        adapter = new ForumAdapter(mContext);
                    }

                    //清除
                    forumList.setVisibility(View.VISIBLE);

                    //LinearLayout forumLayout = (LinearLayout)findViewById(R.id.forum_layout);
                    if(what == ScrollListView.REFRESH){
                        //刷新,先清空
                        //forumLayout.removeAllViews();//这个把控件也给清了
                        forumList.onRefreshComplete();
                    }
                    if(what == ScrollListView.LOAD){
                        forumList.onLoadComplete();
                    }

                    System.out.println("dataItems的长度"+dataItems.size());
                    //拿到数据
                    if(what!=ScrollListView.LOAD) {
                        adapter.setItems(dataItems);
                        forumList.setAdapter(adapter);
                    }else{
                        forumList.deferNotifyDataSetChanged();//重新加载数据
                    }
                    if(what == ScrollListView.LOADFIRST) {
                        //设置刷新事件
                        forumList.setOnRefreshListener((ScrollListView.OnRefreshListener) mContext);
                        //设置加载事件
                        forumList.setOnLoadListener((ScrollListView.OnLoadListener) mContext);
                    }
                    System.out.println("reloadHeader:"+reloadHeader);
                    if(reloadHeader!=null){
                        LinearLayout forumLayout = (LinearLayout)findViewById(R.id.forum_layout);
                        forumLayout.removeView(reloadHeader);
                    }
                    break;
                case Config.SUCCESS_FULL_PAGE:
                    //最后一页
                    Toast.makeText(getApplicationContext(), getResources().getString
                            (R.string.page_full), Toast.LENGTH_SHORT).show();
                    break;
                case Config.FAILURE_NET_ERROR:
                    //加载失败，请重新尝试
                    initReloadView();
                    //init
                    button_reload.setVisibility(View.VISIBLE);
                    tipReload.setText(R.string.page_load_failure);
                    refreshingReload.setVisibility(View.GONE);

                    button_reload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.setVisibility(View.GONE);
                            tipReload.setText(R.string.start_reload);
                            refreshingReload.setVisibility(View.VISIBLE);
                            loadData(ScrollListView.LOADFIRST);
                        }
                    });
                    break;
            }
        }
    };
    class MyThread implements Runnable{

        public void run(){
            //if(strs.size()>0){
              //  strs.clear();
            //}
            String url = Config.FORUM_URL;
            if(what == ScrollListView.LOAD) {
                //加载分页的数据
                page++;
                if (page>pageMax){
                    //不用操作了
                    forumList.onLoadComplete();
                    mHandler.obtainMessage(Config.SUCCESS_FULL_PAGE).sendToTarget();
                    return;
                }
                url += "&page="+page;
            }

            Url.getInstance().setUrl(url);
            String string = Url.getInstance().doGet();
            //String string = "net_error";
            Log.d(Config.TAG,"url is "+url);
            //Log.d(Config.TAG,"string is "+string);

            //if(page == 1) {
            //    string = "net_error";
            //    page++;
            //}
            //Log.d(Config.TAG,string);
            if(string == "net_error"){
                mHandler.obtainMessage(Config.FAILURE_NET_ERROR).sendToTarget();
                return;
            }
            //Pattern p = Pattern.compile("<a href=\"viewtopic.asp/?fid=1&tid=(.*?)\" title='(.*?)'>(.*?)></a><br />");
            Pattern p = Pattern.compile("<a href=\"viewtopic.asp\\?fid=1&tid=(.*?)\" title='(.*?)'>(.*?)</a><br />");
            Matcher m = p.matcher(string);
            Log.d(Config.TAG,"开始加载");
            //Log.d(Config.TAG, String.valueOf(m));
            while (m.find()) {
                strs.add(new String[]{m.group(1), m.group(3)});
            }
            System.out.println("strs="+strs);
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

    @Override
    public void onRefresh(){
        loadData(ScrollListView.REFRESH);
        //forumList.onRefreshComplete();
    }
    @Override
    public void onLoad(){
        loadData(ScrollListView.LOAD);
    }
}

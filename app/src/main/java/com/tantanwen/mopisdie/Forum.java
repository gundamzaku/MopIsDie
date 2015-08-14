package com.tantanwen.mopisdie;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.AndroidCharacter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tantanwen.mopisdie.adapter.ForumAdapter;
import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;
import com.tantanwen.mopisdie.utils.Download;
import com.tantanwen.mopisdie.utils.FilesCache;
import com.tantanwen.mopisdie.utils.Sp;
import com.tantanwen.mopisdie.widget.ScrollListView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Forum extends AppCompatActivity implements ScrollListView.OnRefreshListener, ScrollListView.OnLoadListener {

    private ArrayList<String[]> strs = new ArrayList<>();
    private ScrollListView forumList;
    private Context mContext;
    private Toolbar toolbar;
    private int what;
    private int page = 1;
    private boolean isLoad = false; //加个锁锁住
    //只让你翻20次
    private int pageMax = 20;
    private DrawerLayout mDrawerLayout;
    private LinearLayout forumLayout;
    private Button button_reload;
    private FilesCache fileCache;
    private String stream;
    private String downloadUrl;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private ProgressBar load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Sp.getInstance(this).getTable("mop_config").setStyle();

        setContentView(R.layout.activity_forum);
        mContext = this;

        initToolBar();
        initDrawer();

        forumList = (ScrollListView)findViewById(R.id.forum_list);

        initReloadView();
        button_reload.setVisibility(View.GONE);
        tipReload.setText(R.string.start_reload);
        refreshingReload.setVisibility(View.VISIBLE);

        setMenuList();

        fileCache = new FilesCache(mContext);
        fileCache.setFileName("forumStream.cache");
        stream = fileCache.load();
        //这个用法还真是奇怪。。不明确
        ArrayList<String[]> strsCahce = JSON.parseObject(stream, new TypeReference<ArrayList<String[]>>() {
        });
        //在这里解开来
        int offset;
        try {
            offset = strsCahce.size();
        }catch (Exception e){
            offset = 0;
        }

        if(offset>0){
            strs = strsCahce;
            loadDataCache();
        }else {
            loadData(ScrollListView.LOADFIRST);
        }

        //检查版本丢线程出去，不影响主线程
        CheckThread checkThread = new CheckThread();
        Thread td1 = new Thread(checkThread);
        td1.start();
    }

    private void setMenuList(){

        ListView mMenuListView = (ListView) findViewById(R.id.menu_list);
        String[] mMenuTitles = getResources().getStringArray(R.array.array_left_menu_forum);
        mMenuListView.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mMenuTitles));
        mMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.d(Config.TAG,"position="+position);
                //Log.d(Config.TAG,"isLoad="+isLoad);
                if(position <=3){
                    if(isLoad == true){
                        Toast.makeText(getApplicationContext(), getResources().getString
                                (R.string.page_load), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
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
                    case 2:
                        //查询
                        //进入一个新的查询页面去
                        list = new Intent(mContext,Search.class);
                        mContext.startActivity(list);
                        break;
                    case 3: //设置
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
                    case 4:
                        loadData(ScrollListView.LOADFIRST);
                        initReloadView();
                        button_reload.setVisibility(View.GONE);
                        tipReload.setText(R.string.start_reload);
                        refreshingReload.setVisibility(View.VISIBLE);

                        //隐藏列表
                        forumList.setVisibility(View.GONE);
                        mDrawerLayout.closeDrawers();
                        break;
                    case 5:
                        //将帐号信息全消除
                        SharedPreferences sp = getSharedPreferences("login_info", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.remove("username");
                        editor.remove("password");
                        editor.commit();
                        Url.clearInstance();
                        finish();
                        break;
                    case 6:
                        finish();
                        break;
                    case 7:
                        list = new Intent(mContext,About.class);
                        mContext.startActivity(list);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /*
        开启线程，读取数据
     */
    private void loadData(int what){

        if(isLoad == true){
            return;
        }
        //开始加载
        isLoad = true;

        //启动线程
        if(strs!=null)strs.clear();

        this.what = what;
        MyThread myThread = new MyThread();
        Thread td1 = new Thread(myThread);
        td1.start();
        //httpRequest.setHeader("Cookie", "JSESSIONID=" + COOKIE);
    }

    private void loadDataCache(){

        dataItems.addAll(strs);
        adapter = new ForumAdapter(mContext);
        adapter.setItems(dataItems);
        forumList.setAdapter(adapter);
        //设置刷新事件
        forumList.setOnRefreshListener((ScrollListView.OnRefreshListener) mContext);
        //设置加载事件
        forumList.setOnLoadListener((ScrollListView.OnLoadListener) mContext);
        forumList.setVisibility(View.VISIBLE);

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
    private void monitorProgress(){
        //mBuilder.build().contentView.setProgressBar(R.id.content_view_progress, 100, progress, false);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private int count = 0;
    final Handler handlerDownload = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //每一次就来1024个字节
            count += msg.arg1;
            //移除时要把整个事件停掉！
            //这里就一条消息
            //System.out.println(count);
            mBuilder.build().contentView.setProgressBar(R.id.progressBar_download, 1608704, count, false);
            monitorProgress();
            //if(count<100) handler.postDelayed(run, 200);
            //200毫秒count加1

            if(count>=1472648){
                System.out.println("结束");
            }
        }
    };


    public void confrimDownload(){

        AlertDialog.Builder builder=new AlertDialog.Builder(mContext);  //先得到构造器
        builder.setTitle(R.string.new_version_publish); //设置标题
        builder.setMessage(R.string.confirm_download); //设置内容
        builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //开始下载，转入后台
                //新线程下载
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //新建一个下载
                        System.out.println("新建一个下载");
                        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.status_bar_download);//通知栏中进度布局
                        //load = (ProgressBar) findViewById(R.id.progressBar_download);
                        mNotificationManager=(NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
                        mBuilder = new NotificationCompat.Builder(Forum.this);
                        mBuilder.setSmallIcon(R.drawable.send);
                        mBuilder.setTicker("正在下载中……");
                        mBuilder.setContentTitle("hello world");
                        mBuilder.setContentText("no,you are stupid");
                        mBuilder.setContent(remoteViews);
                        /*
                        Download load = new Download(downloadUrl);
                        String value = load.downloadAsString();
                        mHandler.obtainMessage(Config.SUCCESS_FULL_PAGE).sendToTarget();
                        */
                        Download l = new Download(downloadUrl);
                        mBuilder.build().contentView.setProgressBar(R.id.progressBar_download, l.getLength(),0, false);
                        monitorProgress();
                        //设置进度条，最大值 为100,当前值为0，最后一个参数为true时显示条纹

                        System.out.println("得到长度");
                        int status = l.down2sd("downtemp/", "1.ddd", l.new downhandler() {
                            @Override
                            public void setSize(int size) {
                                Message msg = handlerDownload.obtainMessage();
                                msg.arg1 = size;
                                msg.sendToTarget();
                                //Log.d("log", Integer.toString(size));
                            }
                        });
                        //log输出
                        Log.d("log", Integer.toString(status));

                    }
                }).start();

                dialog.dismiss(); //关闭dialog
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //参数都设置完成了，创建并显示出来
        builder.create().show();
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
                     forumList.onRefreshComplete();
                    //加载失败，请重新尝试
                    initReloadView();
                    //init
                    button_reload.setVisibility(View.VISIBLE);
                    tipReload.setText(R.string.page_load_failure);
                    refreshingReload.setVisibility(View.GONE);
                    button_reload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            isLoad = true;

                            v.setVisibility(View.GONE);
                            tipReload.setText(R.string.start_reload);
                            refreshingReload.setVisibility(View.VISIBLE);
                            loadData(ScrollListView.LOADFIRST);
                        }
                    });
                    break;
                case Config.SUCCESS_NEED_DOWNLOAD:
                    confrimDownload();
                    break;
                default:
                    break;
            }
            isLoad = false;  //解锁
        }

    };

    class MyThread implements Runnable{

        public void run(){

            String url = Config.FORUM_URL;
            Integer originalPage = page;
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
            //Log.d(Config.TAG,"string is "+string);

            //if(page == 1) {
            //    string = "net_error";
            //    page++;
            //}

            if(string == "net_error"){
                page = originalPage;    //如果之前分页有过变动，回归原始的page
                mHandler.obtainMessage(Config.FAILURE_NET_ERROR).sendToTarget();
                return;
            }
            //Pattern p = Pattern.compile("<a href=\"viewtopic.asp/?fid=1&tid=(.*?)\" title='(.*?)'>(.*?)></a><br />");
            Pattern p = Pattern.compile("<a href=\"viewtopic.asp\\?fid=1&tid=(.*?)\" title='(.*?)'>(.*?)</a><br />");
            Matcher m = p.matcher(string);

            while (m.find()) {
                strs.add(new String[]{m.group(1), m.group(3)});
            }

            //用的是阿里的，序列化，存入文件
            String strJson= JSON.toJSONString(strs);
            fileCache.setStream(strJson);
            fileCache.save();

            mHandler.obtainMessage(Config.SUCCESS).sendToTarget();
            /*
            for (String s : strs){
                System.out.println(s);
            }*/
            //<a href="viewtopic.asp?fid=1&tid=5406" title='【2015-7-7 10:35:09 猫晓扑】'>【法律相关】关于土地纠纷 (19/778)</a><br />
        }
    };
    /*
    检查下载点是否有数据
     */
    class CheckThread implements Runnable{

        @Override
        public void run() {
            //得到我的当前版本
            String url = Config.CHECK_VERSION_URL;
            Url.getInstance().setUrl(url);
            String string = Url.getInstance().doGet();
            //解Json
            JSONObject strsCahce = JSON.parseObject(string);
            System.out.println(strsCahce.get("loadurl"));

            PackageManager manager;
            PackageInfo info = null;
            manager = mContext.getPackageManager();
            try {
                info = manager.getPackageInfo(mContext.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if(info.versionCode>Integer.valueOf(String.valueOf(strsCahce.get("version")))){
                //有下载，需要用户确认
                //需要回主线程
                downloadUrl = String.valueOf(strsCahce.get("loadurl"));
                mHandler.obtainMessage(Config.SUCCESS_NEED_DOWNLOAD).sendToTarget();
            }
        }
    }
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
    protected void onResume(){
        super.onResume();
        //System.out.println("重新进来了");
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

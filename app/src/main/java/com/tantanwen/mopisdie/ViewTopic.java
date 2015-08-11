package com.tantanwen.mopisdie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.tantanwen.mopisdie.http.AndroidJavaScript;
import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;
import com.tantanwen.mopisdie.utils.FilesCache;
import com.tantanwen.mopisdie.utils.HTMLSpirit;
import com.tantanwen.mopisdie.utils.Utils;

import java.io.InputStream;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@SuppressLint("JavascriptInterface")
public class ViewTopic extends AppCompatActivity {

    private String tid;
    private String string;
    private Toolbar toolbar;
    private Context mContext;
    private String fromJs_pid,fromJs_f;
    private LinearLayout forumLayout;
    private WebView webView;
    private View reloadHeader;
    private Button button_reload;
    private TextView tipReload;
    private ProgressBar refreshingReload;
    private DrawerLayout mDrawerLayout;
    private NetworkInfo mWifi;
    private FilesCache fileCache;
    private int webViewCurrentHeight = 0;
    boolean dontWriteFile = false;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_topic);
        Intent intent = getIntent();
        tid = intent.getStringExtra("tid");
        mContext = this;

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        initToolBar();
        initDrawer();
        webView = (WebView)findViewById(R.id.webView);
        /*
        webView.getSettings().setJavaScriptEnabled(true);
        //webView.addJavascriptInterface(new JsObject(), "injectedObject");

        string = "<a onclick=\"Android.showquot('144919','1');\" class=\"bluelink\">回复</a>";
        webView.loadDataWithBaseURL(null, string, "text/html", "UTF-8", null);
        */

        fileCache = new FilesCache(mContext);

        fileCache.setFileName("viewtopic_"+tid+".cache");
        string = fileCache.load();

        if(string != null){
            showWeb();
        }else {
            loadData();
        }
        setMenuList();

    }

    private void setMenuList(){
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
                        list.putExtra("tid",tid);
                        mContext.startActivity(list);
                        //LayoutInflater inflater = getLayoutInflater();
                        //View layout = inflater.inflate(R.layout.activity_reply,(ViewGroup) findViewById(R.id.dialog));
                        //new AlertDialog.Builder(mContext).setTitle("我要回复").setView(layout).setPositiveButton("确定", null).setNegativeButton("取消", null).show();
                        break;
                    case 1:
                        loadData();
                        mDrawerLayout.closeDrawers();
                        break;
                    case 2:
                        if(webViewCurrentHeight>0) {
                            //System.out.println(webView.getScrollY());
                            webView.setScrollY(webViewCurrentHeight);
                        }else {
                            Toast.makeText(getApplicationContext(), getResources().getString
                                    (R.string.webview_loading), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }
    private void loadData(){
        initReloadView();
        button_reload.setVisibility(View.GONE);
        tipReload.setText(R.string.start_reload);
        refreshingReload.setVisibility(View.VISIBLE);

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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);//设置监听器
    }
    public void initReloadView(){

        //调出加载页面
        forumLayout = (LinearLayout)findViewById(R.id.topic_layout);
        webView.setVisibility(View.GONE);
        View failureReloadHeader = forumLayout.findViewById(R.id.failure_reload_header);

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
    private SharedPreferences sp;
    private String source;
    private Handler mHandler = new Handler() {

        public void handleMessage (Message msg) {//此方法在ui线程运行

            Pattern p;
            Matcher m;

            switch(msg.what) {
                case Config.SUCCESS:
                    //System.out.println(string);
                    //启动正则，过滤数据
                    sp = mContext.getSharedPreferences("mop_config",MODE_PRIVATE);
                    Integer loadImagesNoWifi = sp.getInt("load_images_nowifi", 0);
                    //System.out.println(string);
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
                        source = getHtmlSource("common","css");
                        string = source+m.group(1);//
                    }else{
                        p = Pattern.compile("<table class=\"tipsborder\" cellSpacing=\"0\" cellPadding=\"0\" align=\"center\">([\\s\\S]*?)</table>");
                        m = p.matcher(string);
                        if(m.find() == true){
                            source = getHtmlSource("common","css");
                            string = source+m.group(1);
                            //string = "<link rel=\"stylesheet\" href=\"file:///android_asset/common.css\" type=\"text/css\" />"+m.group(1);
                        }else{
                            string = "出现了意外";
                            dontWriteFile = true;
                        }
                    }
                    //return shows3
                    string = string.replaceAll("<a href=\"topicedit.asp\\?pid=(.*?)\" class=\"showun\" onclick=\"return shows3\\(this.href\\);\">","<a href=\"topicedit.asp\" class=\"showun\" onclick=\"return injectedObject.shows3(\'$1\');\">");
                    //string = string.replaceAll("<a href=\"topicedit.asp\\?pid=(.*?)\" class=\"showun\" onclick=\"return shows3(this.href);\">","123123");
                    //string = string.replaceAll("return shows3","return injectedObject.shows3");
                    string = string.replaceAll("return shows","return injectedObject.shows");
                    string = string.replaceAll("onclick=\"showquot","onclick=\"injectedObject.showquot");

                    //内部地址全加上跳转
                    string = string.replaceAll("<a href=\"attachments", "<a href=\"" + Config.HOST + "/attachments");

                    if(loadImagesNoWifi != 1 && mWifi.isConnected() == false){
                        //批量将图片全部替换成url形式
                        //string = string.replaceAll("<img src=\"|\'([\\s\\S]*?)\"|'","11111111111");
                        /*
                        String regex = "<img src=\"(.*?)\"([\\s\\S]*?)>";  //查找开始标签的<
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(string);
                        while (matcher.find()) {
                            System.out.println(matcher.group(1));
                        }*/
                        string = string.replaceAll("<img src=[\"|'](attachments|face)/(.*?)[\"|'](.*?)/>","<font color=yellow>图片</font>");
                        string = string.replaceAll("<img src=\"(.*?)\"(.*?)/>","<a href=\"$1\" target=\"_blank\"><font color=blue>图片</font></a>");
                    }else{
                        string = string.replaceAll("<img src=[\"|'](attachments|face)/(.*?)[\"|'](.*?)/>","<img src=\""+Config.HOST+"/$1/$2\"$3>");
                    }
                    source = getHtmlSource("init","js");
                    string += source;
                    //string += "<script type=\"text/javascript\" src=\"file:///android_asset/init.js\"></script>";

                    //将string写入文件
                    if(dontWriteFile == false) {
                        fileCache.setStream(string);
                        fileCache.save();
                    }
                    showWeb();

                    break;
                case Config.SUCCESS_02:
                    //System.out.println(string);
                    //<script type="text/javascript">$('quot_message').value=''</script>
                    p = Pattern.compile("<script type=\"text/javascript\">\\$\\('quot_message'\\).value='([\\s\\S]*?)'</script>");
                    m = p.matcher(string);
                    if(m.find() == true){
                        String quote = m.group(1);
                        //页面跳转，并把值带过去
                        Intent list = new Intent(mContext,Reply.class);
                        list.putExtra("tid",tid);
                        list.putExtra("quote",quote);
                        mContext.startActivity(list);
                    }
                    break;
            }
        }
    };
    @JavascriptInterface
    @SuppressLint("SetJavaScriptEnabled")
    private void showWeb(){

        webView.getSettings().setBuiltInZoomControls(true);

        webView.setBackgroundColor(0); // 设置背景色
        WebViewClient webviewcilnt = new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        };
        webView.getSettings().setJavaScriptEnabled(true);

        webView.getSettings().setDefaultTextEncodingName("UTF-8");

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT){
            //webView.loadData(string, "text/html; charset=UTF-8", "UTF-8");
            webView.loadDataWithBaseURL("", string, "text/html; charset=UTF-8", "UTF-8", null);

        }else{

            //webView.loadUrl("file:///"+fileCache.getFilesPath()+"/"+"viewtopic_"+tid+".html");
            webView.loadDataWithBaseURL(null, string, "text/html", "UTF-8", null);
            webView.setWebViewClient(webviewcilnt);
            //string = "<a onclick=\"injectedObject.showquot('146124', '2');\" class=\"bluelink\">回复</a>";
            //webView.loadData(string, "text/html; charset=UTF-8", "UTF-8");
            /*
            try {
                webView.loadData(URLEncoder.encode(string, "utf-8"), "text/html; charset=UTF-8", "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }*/

        }
        //new AndroidJavaScript(this,new CpThread())
        //在这里声明一个变量，把值全分配好再传走

        webView.addJavascriptInterface(new AndroidJavaScript(this,new CpThread()), "injectedObject");
        //webView.addJavascriptInterface(new JsObject(), "injectedObject");
        //webView.loadDataWithBaseURL("", string, "text/html; charset=UTF-8", "UTF-8", null);
        webView.setVisibility(View.VISIBLE);

        //webView.setScrollY();
    }

    private String getHtmlSource(String fileName,String postfix){

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT){
            if(postfix == "css") {
                return "<link rel=\"stylesheet\" href=\"file:///android_asset/" + fileName + "." + postfix + "\" type=\"text/css\" />";
            }
            if(postfix == "js"){
                return "<script type=\"text/javascript\" src=\"file:///android_asset/" + fileName + "." + postfix + "\"></script>";
            }
        }else{
            if(postfix == "css") {
                return "<style>"+readFromAssets(fileName+"."+postfix)+"</style>";
            }
            if(postfix == "js") {
                //return "<script>"+readFromAssets(fileName+"."+postfix)+"</script>";
            }
        }
        return "";

    }

    /**
     * 从assets中读取txt
     */
    private String readFromAssets(String fileName) {
        try {
            InputStream is = getAssets().open(fileName);
            String text = Utils.readSourceFromSDcard(is);
            return text;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    class JsObject {
        @JavascriptInterface
        public boolean shows3(String pid) {
            //页面跳转，并把值带过去

            Intent list = new Intent(mContext,MyTopic.class);
            list.putExtra("pid",pid);
            mContext.startActivity(list);
            //Toast.makeText(getApplicationContext(), "这有啥好看的？看自己小鸡鸡去。", Toast.LENGTH_SHORT).show();
            return false;
        }
        @JavascriptInterface
        public boolean shows(Objects href) {

            Toast.makeText(getApplicationContext(), "发送短消息", Toast.LENGTH_SHORT).show();
            return false;
        }
        @JavascriptInterface
        public void showquot(String pid,String f) {
            System.out.println("ttttttt");
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
        @JavascriptInterface
        public void adjustHeight(int height){
            webViewCurrentHeight = height;
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

    public class CpThread implements Runnable{

        public void run(){

            Url.getInstance().setUrl(Config.VIEW_TOPIC_CP_URL+"&pid="+fromJs_pid+"&f="+fromJs_f);
            System.out.println(Config.VIEW_TOPIC_CP_URL+"&pid="+fromJs_pid+"&f="+fromJs_f);
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

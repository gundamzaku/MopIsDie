package com.tantanwen.mopisdie;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.tantanwen.mopisdie.adapter.PmAdapter;
import com.tantanwen.mopisdie.adapter.PmContainer;
import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pm extends AppCompatActivity {

    private Toolbar toolbar;
    private ArrayList<PmContainer> strs = new ArrayList<>();
    private ListView pmList;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pm);
        mContext = this;
        initToolBar();

        //启动线程
        MyThread myThread = new MyThread();
        Thread td1 = new Thread(myThread);
        td1.start();
    }

    private void initToolBar(){

        toolbar = (Toolbar)findViewById(R.id.id_toolbar);
        toolbar.setTitle(R.string.title_activity_pm);
        setSupportActionBar(toolbar);
    }
    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行
            switch(msg.what) {
                case 1001:

                    PmAdapter adapter = new PmAdapter(mContext);
                    adapter.setItems(strs);

                    //拿到数据
                    pmList = (ListView)findViewById(R.id.forum_list);
                    pmList.setAdapter(adapter);
                    /*
                    for (int i =0;i<strs.size();i++){
                        System.out.println(strs.get(i).getRe());
                    }*/
            }
        }
    };
    class MyThread implements Runnable{

        public void run(){

            Url.getInstance().setUrl(Config.VIEW_PM_URL);
            String string = Url.getInstance().doGet();
            //先取一次批量的集合，拿出所有的消息
            /*
            <span class="pink">呼噜哥</span>给您发送的信息 【2015-3-5 9:02:19】【<a href="?pmid=2377&action=save">保存</a>】
            <hr color="black" />
            <span class="grey">re:呼噜哥。我去邮局问的话就是问能不能开发票吗？开发票有各种类型？只要告诉他我要开某个类别的发票就行了？</span>
            <p>是的</p>
            <br />*/
            Pattern p;
            Matcher m;
            Matcher mc;

            p = Pattern.compile("<span class=\"pink\">([\\s\\S]*?)<form method=\"post\"");
            m = p.matcher(string);
            while (m.find()) {
                PmContainer pmContainer = new PmContainer();

                pmContainer.setSendUserNick("");
                pmContainer.setSendTime("");
                pmContainer.setPmid(0);
                pmContainer.setRe("");
                pmContainer.setContent("");

                //开始截取数据
                p = Pattern.compile("<span class=\"pink\">(.*?)</span>给您发送的信息 【(.*?)】【<a href=\"\\?pmid=(.*?)&action=save\">");
                mc = p.matcher(m.group());
                if(mc.find() == true){

                    /*
                    System.out.println(mc.group(1));
                    System.out.println(mc.group(2));
                    System.out.println(mc.group(3));
                    */
                    pmContainer.setSendUserNick(mc.group(1));
                    pmContainer.setSendTime(mc.group(2));
                    pmContainer.setPmid(Integer.parseInt(mc.group(3)));
                }

                p = Pattern.compile("<span class=\"grey\">([\\s\\S]*?)</span>");
                mc = p.matcher(m.group());
                if(mc.find() == true){
                    pmContainer.setRe(mc.group(1));
                }
                p = Pattern.compile("<p>([\\s\\S]*?)</p>");
                mc = p.matcher(m.group());
                if(mc.find() == true){
                    pmContainer.setContent(mc.group(1));
                }
                strs.add(pmContainer);
            }

            mHandler.obtainMessage(Config.SUCCESS).sendToTarget();

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pm, menu);
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

package com.tantanwen.mopisdie.thread;

import android.os.Handler;
import android.util.Log;

import com.tantanwen.mopisdie.Search;
import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gundamzaku on 2015/7/31.
 */
public class SearchThread implements Runnable{

    private int type;
    private Handler mHandler;
    private String string;
    private String keyword;
    private String searchType;
    private ArrayList<String[]> strs = new ArrayList<>();

    public SearchThread(Handler mHandler){

        /*
            1：查条件
            2：查自己发送
            3:查自己回复
         */

        this.mHandler = mHandler;
    }

    public void setType(int type){
        this.type = type;
    }

    //查询的关键字
    public void setKeyword(String keyword){
        this.keyword = keyword;
    }

    public void setSearchType(String searchType){
        this.searchType = searchType;
    }

    public void run(){
        lockStatus();

        //http://www.fuyuncun.com/search.asp?action=search&keyword=nexus&searchtype=title
        Url.getInstance().addParameter("action", "search");
        if(this.type == 1){ //普通条件查询

            //Url.getInstance().addParameter("keyword", this.keyword);
            //Url.getInstance().addParameter("searchtype", this.searchType);
            //Url.getInstance().addParameter("submit", "搜索");
            Url.getInstance().setUrl(Config.SEARCH_URL+"?action=search&keyword="+this.keyword+"&searchtype="+this.searchType);

        }else if(this.type == 2){   //自回
            Url.getInstance().setUrl(Config.SEARCH_MYPOST_URL);
        }else if(this.type == 3){   //自发
            Url.getInstance().setUrl(Config.SEARCH_MYTOPIC_URL);
        }
        string = Url.getInstance().doGet();
        Pattern p;  //声明
        Matcher m;
        //重置
        if(strs.size()>0){
            strs.clear();
        }
        if(this.type == 1){
            //<div class="tips_content">搜索完成，请点击这里查看搜索结果。<p><a href="?searchid=5569" target="_self">如果您的浏览器没有跳转，请点击这里。</a>
            p = Pattern.compile("<div class=\"tips_content\">搜索完成，请点击这里查看搜索结果。<p><a href=\"\\?searchid=(.*?)\" target=\"_self\">如果您的浏览器没有跳转，请点击这里。</a>");
            m = p.matcher(string);
            if(m.find() == true) {
                //再发一次线程的请求
                Url.getInstance().setUrl(Config.SEARCH_URL+"?searchid="+m.group(1));
                string = Url.getInstance().doGet();
               // System.out.println(string);
            }
            //分析一下
            //<a href="viewtopic.asp?fid=1&tid=6056" title="2015-7-31 20:23:36">STRING (17/677)</a>
            //<a title="2015-7-31 20:23:36" href="viewtopic.asp?fid=1&tid=6056">STRING (17/668)</a>
            p = Pattern.compile("<a href=\"viewtopic.asp\\?fid=1&tid=(.*?)\" title=\"(.*?)\">(.*?)</a>");
            m = p.matcher(string);
            while (m.find()) {
                strs.add(new String[]{m.group(1), m.group(3)});
            }

        }else if(this.type == 2){    //自回

            //<a href="topicmisc.asp?action=redirectpost&pid=139858">STRING</a>
            p = Pattern.compile("<a href=\"topicmisc.asp\\?action=redirectpost&pid=(.*?)\">(.*?)</a>");
            m = p.matcher(string);
            while (m.find()) {
                strs.add(new String[]{null, m.group(2)});
            }
        }else if (this.type == 3){
            //<a href="viewtopic.asp?fid=1&tid=5868" title="2015-7-27 1:15:49">关于如何培养和激励部门员工的问题。。 (26/1141)</a>
            p = Pattern.compile("<a href=\"viewtopic.asp\\?fid=1&tid=(.*?)\" title=\"(.*?)\">(.*?)</a>");
            m = p.matcher(string);
            while (m.find()) {
                strs.add(new String[]{m.group(1), m.group(3)});
            }
        }else {
            this.mHandler.obtainMessage(Config.FAILURE).sendToTarget();
            return;
        }
        //System.out.println(string);
        this.mHandler.obtainMessage(Config.SUCCESS).sendToTarget();
        //Log.d(Config.TAG,""+string);
    }
    public ArrayList getData(){
        return strs;
    }
    public void lockStatus(){
        Search.isLock = true;
    }
    public void unLockStatus(){
        Search.isLock = false;
    }
}

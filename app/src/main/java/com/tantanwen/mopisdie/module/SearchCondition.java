package com.tantanwen.mopisdie.module;

import android.os.Handler;
import android.util.Log;

import com.tantanwen.mopisdie.Search;
import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gundamzaku on 2015/7/31.
 */
public class SearchCondition implements Runnable{

    private int type;
    private Handler mHandler;
    private String string;
    private String keyword;
    private String searchType;

    public SearchCondition(Handler mHandler){

        /*
            1：查条件
            2：查自己发送
            3:查自己回复
         */

        this.mHandler = mHandler;
        System.out.println(Search.isLock);
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
        System.out.println(Url.getInstance().getParameter());
        string = Url.getInstance().doGet();
        if(this.type == 1){
           //<div class="tips_content">搜索完成，请点击这里查看搜索结果。<p><a href="?searchid=5569" target="_self">如果您的浏览器没有跳转，请点击这里。</a>
           Pattern p = Pattern.compile("<div class=\"tips_content\">搜索完成，请点击这里查看搜索结果。<p><a href=\"\\?searchid=(.*?)\" target=\"_self\">如果您的浏览器没有跳转，请点击这里。</a>");
           Matcher m = p.matcher(string);
           if(m.find() == true) {
               //再发一次线程的请求
               Url.getInstance().setUrl(Config.SEARCH_URL+"?searchid="+m.group(1));
               string = Url.getInstance().doGet();
               System.out.println(string);
           }
        }else {
            System.out.println(string);
        }
        //需要解析是否是登录成功
        //System.out.println(string);
        this.mHandler.obtainMessage(Config.SUCCESS).sendToTarget();
        //Log.d(Config.TAG,""+string);
    }

    public void lockStatus(){
        Search.isLock = true;
    }
    public void unLockStatus(){
        Search.isLock = false;
    }
}

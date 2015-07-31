package com.tantanwen.mopisdie.module;

import android.os.Handler;

import com.tantanwen.mopisdie.Search;
import com.tantanwen.mopisdie.http.Url;
import com.tantanwen.mopisdie.utils.Config;

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
        Url.getInstance().setUrl(Config.SEARCH_URL);

        Url.getInstance().addParameter("action", "search");
        if(this.type == 1){ //普通条件查询

            Url.getInstance().addParameter("keyword", this.keyword);
            Url.getInstance().addParameter("searchtype", this.searchType);
            Url.getInstance().addParameter("submit", "搜索");

        }else if(this.type == 2){   //自回
            Url.getInstance().setUrl(Config.SEARCH_URL+"?action=myposts");
        }else if(this.type == 3){   //自发
            Url.getInstance().setUrl(Config.SEARCH_URL+"?action=mytopics");
        }
        System.out.println(Url.getInstance().getParameter());
        string = Url.getInstance().doGet();

        //需要解析是否是登录成功
        System.out.println(Config.SEARCH_URL);
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

package com.tantanwen.mopisdie.adapter;

import android.content.Intent;

/**
 * Created by gundamzaku on 2015/7/15.
 */
public class PmContainer {

    public int pmid;
    public String title;
    public String re;
    public String content;
    public String sendUserNick;
    public String sendTime;

    public void setPmid(int _pmid){
        pmid = _pmid;
    }

    public void setSendUserNick(String _sendUserNick){
        sendUserNick = _sendUserNick;
    }

    public void setTitle(String _title){
        title = _title;
    }

    public void setSendTime(String _sendTime){
        sendTime = _sendTime;
    }

    public void setRe(String _re){
        re = _re;
    }

    public void setContent(String _content){
        content = _content;
    }

    public int getPmid(){
        return pmid;
    }

    public String getSendUserNick(){
        return sendUserNick;
    }

    public String getTitle(){
        return title;
    }

    public String getSendTime(){
        return sendTime;
    }

    public String getRe(){
        return re;
    }

    public String getContent(){
        return content;
    }
}

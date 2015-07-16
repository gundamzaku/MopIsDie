package com.tantanwen.mopisdie.adapter;

/**
 * Created by gundamzaku on 2015/7/16.
 */
public class PmMessageContainer {

    private static final String TAG = PmMessageContainer.class.getSimpleName();
    //名字
    private String name;
    //日期
    private String date;
    //聊天内容
    private String text;
    //是否为对方发来的信息
    private boolean isComMeg = true;

    public PmMessageContainer(String name, String date, String text, boolean isComMsg) {
        this.name = name;
        this.date = date;
        this.text = text;
        this.isComMeg = isComMsg;
    }

    public PmMessageContainer() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getMsgType() {
        return isComMeg;
    }

    public void setMsgType(boolean isComMsg) {
        isComMeg = isComMsg;
    }
}

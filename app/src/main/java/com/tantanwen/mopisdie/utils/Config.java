package com.tantanwen.mopisdie.utils;

/**
 * Created by gundamzaku on 2015/7/6.
 */
public class Config {

    public static String TAG = "debug";
    public static String HOST = "http://www.fuyuncun.com";
    public static String LOGIN_URL = HOST+"/login.asp?action=login";
    public static String FORUM_URL = HOST+"/forumdisplay.asp?fid=1&typeid=0";
    //http://fuyuncun.com/forumdisplay.asp?page=2&fid=1&typeid=0
    public static String VIEW_TOPIC_URL = HOST+"/viewtopic.asp?fid=1&tid=";
    public static String VIEW_TOPIC_CP_URL = HOST+"/topiccp.asp?action=ajaxquot";
    public static String VIEW_PM_URL = HOST+"/pm.asp";
    public static String POST_REPLY_URL = HOST+"/post.asp?action=newreply";
    public static String POST_NEW_URL = HOST+"/post.asp?action=newtopic";
    public static String POST_MESSAGE_URL = HOST+"/pm.asp?action=reply";
    public static String POST_TOPICEDIT_URL = HOST+"/topicedit.asp?pid=";
    public static String POST_TOPIC_MODIFY_URL = HOST+"/topicedit.asp?action=submitmodify";
    public static String SEARCH_URL = HOST+"/search.asp";
    public static String SEARCH_MYPOST_URL = HOST+"/membermisc.asp?action=myposts";
    public static String SEARCH_MYTOPIC_URL = HOST+"/membermisc.asp?action=mytopics";
    public static String SEND_PM_URL = HOST+"/pm.asp?action=sendpost&r=mcp";
    public static String CHECK_VERSION_URL = "http://tantanwen.sinaapp.com/check.php";


    //定义成功的操作符
    public static final int SUCCESS                 = 1001;
    public static final int SUCCESS_02              = 2001;
    public static final int SUCCESS_FULL_PAGE     = 2002;
    public static final int SUCCESS_NEED_DOWNLOAD     = 2003;
    public static final int FAILURE                 = 9999;
    public static final int NOTHING                 = 9998;
    public static final int FAILURE_LOGIN_INFO      = 1002;
    public static final int FAILURE_NET_ERROR       = 1003;
    public static final int FAILURE_MESSAGE_EMPTY    = 1004;
    public static final int FAILURE_REGIST_NOOPEN    = 1005;    //未开发登记
    public static final int FAILURE_ANONYMOUS       = 1006;    //未开发登记
}

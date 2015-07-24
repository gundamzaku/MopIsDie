package com.tantanwen.mopisdie.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gundamzaku on 2015/7/6.
 */
public class Url {

    List<NameValuePair> params = new ArrayList<NameValuePair>();
    private HttpPost httpPost;
    private String result = "";
    private CookieStore cookieStore;
    private DefaultHttpClient httpClient;
    private HttpResponse httpResponse;
    private String uriApi;
    private HttpParams httpParams;

    private static Url helper = null;

    public static final Url getInstance(){
        if(helper  == null){
            helper = new Url();
        }
        return helper;
    }

    public static final void clearInstance(){
        helper = null;
    }

    public Url(){
        httpParams = new BasicHttpParams();
        //设置超时时间
        HttpConnectionParams.setConnectionTimeout(httpParams,15000);
        HttpConnectionParams.setSoTimeout(httpParams,15000);
        httpClient = new DefaultHttpClient(httpParams);
        cookieStore = new BasicCookieStore();
    }

    public void setUrl(String url){
        //清除
        if(params.size()>0) {
            clearParameter();
        }
        uriApi = url;
    }

    public void clearParameter(){
        params.clear();
    }
    public void addParameter(String name,String value){
        params.add(new BasicNameValuePair(name, value));
    }

    public List<NameValuePair> getParameter(){
        return params;
    }

    public String doGet(){

        // 实例化HTTP方法
        HttpGet httpGet = new HttpGet(uriApi);
        for (int i = 0; i < cookieStore.getCookies().size(); i++) {
            //httpGet.setHeader(cookieStore.getCookies().get(i).getName(),cookieStore.getCookies().get(i).getValue());
        }

        try {
            httpResponse = httpClient.execute(httpGet);
            if(httpResponse.getStatusLine().getStatusCode() == 200){
                HttpEntity httpEntity = httpResponse.getEntity();
                result = EntityUtils.toString(httpEntity);
            }
        } catch (IOException e) {
            return "net_error";
        }

        return result;
    }

    public String doPost() {
        //Log.d(Config.TAG,uriApi);
        httpPost = new HttpPost(uriApi);//创建HttpPost对象
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            httpResponse = httpClient.execute(httpPost);

            if(httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = httpResponse.getEntity();
                result = EntityUtils.toString(httpEntity);//取出应答字符串
            }
        } catch (IOException e) {
            return "net_error";
        }

        return result;
    }

    public void setCookieStore(){

        List<Cookie> cookies = httpClient.getCookieStore().getCookies();
        for (int i = 0; i < cookies.size(); i++) {
            cookieStore.addCookie(cookies.get(i));
        }
    }

    public Map getCookieStore(){
        List<Cookie> cookiesList = cookieStore.getCookies();
        Map map = new HashMap();
        //cookiesList.get(i).getName()
        for (int i = 0;i<cookiesList.size();i++){
           map.put(cookiesList.get(i).getName(),cookiesList.get(i));
        }
        return map;
    }
}

package com.tantanwen.mopisdie.utils;

/**
 * Created by gundamzaku on 2015/7/14.
 * 这是我写的？我都忘了。。。
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLSpirit{

    public static String delHTMLTag(String htmlStr){
        String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式

        Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
        Matcher m_script=p_script.matcher(htmlStr);
        htmlStr=m_script.replaceAll(""); //过滤script标签

        Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
        Matcher m_style=p_style.matcher(htmlStr);
        htmlStr=m_style.replaceAll(""); //过滤style标签

        Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
        Matcher m_html=p_html.matcher(htmlStr);
        htmlStr=m_html.replaceAll(""); //过滤html标签

        return htmlStr.trim(); //返回文本字符串
    }
    //这个是网上抄的。。
    public static String htmlEncode(String source) {
        if (source == null) {
            return "";
        }
        String html = "";
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            switch (c) {
                case '<':
                    buffer.append("&lt;");
                    break;
                case '>':
                    buffer.append("&gt;");
                    break;
                case '&':
                    buffer.append("&amp;");
                    break;
                case '"':
                    buffer.append("&quot;");
                    break;
                case 10:
                case 13:
                    break;
                default:
                    buffer.append(c);
            }
        }
        html = buffer.toString();
        return html;
    }

    //有更好的方法吗
    public static String htmlDecode(String source) {
        if (source == null) {
            return "";
        }
        /*
        <li>& （和号）成为 &amp;</li>
        <li>&quot; （双引号）成为 &quot;</li>
        <li>' （单引号）成为 '</li>
        <li>&lt; （小于）成为 &lt;</li>
        <li>&gt; （大于）成为 &gt;</li>
        */
        source = source.replace("&amp;", "&");
        source = source.replace("&quot;", "\"");
        source = source.replace("&#39;", "'");
        source = source.replace("&lt;", "<");
        source = source.replace("&gt;", ">");
        //System.out.println("这个是："+source);
        source = source.replaceAll("<br>|<br />", "\n");
        return source;
    }

    public static String transMessage(String message){
        String messageTrans = message.replaceAll("\n", "<br>");
        return messageTrans;
    }
}
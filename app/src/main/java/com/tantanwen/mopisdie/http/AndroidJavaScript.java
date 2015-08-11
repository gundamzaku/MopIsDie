package com.tantanwen.mopisdie.http;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.tantanwen.mopisdie.MyTopic;
import com.tantanwen.mopisdie.ViewTopic;

import java.util.Objects;

public class AndroidJavaScript {

	private final ViewTopic.CpThread cpThread;
	Context mContext;
	private String fromJs_pid;
	private String fromJs_f;

	public AndroidJavaScript(ViewTopic mContext, ViewTopic.CpThread cpThread) {
		this.mContext = mContext;
		this.cpThread = cpThread;
	}

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
	public boolean shows(String href) {

		Toast.makeText(mContext.getApplicationContext(), "发送短消息", Toast.LENGTH_SHORT).show();
		return false;
	}

	@JavascriptInterface
	public void showquot(String pid,String f) {

		fromJs_pid = pid;
		fromJs_f = f;
		//启动线程
		Thread td1 = new Thread(cpThread);
		td1.start();
		//组织成url
		//http://fuyuncun.com/topiccp.asp?action=ajaxquot&pid=129518&f=1
		//Toast.makeText(getApplicationContext(), "引用", Toast.LENGTH_SHORT).show();

	}
	/*
	@JavascriptInterface
	public void adjustHeight(int height){
		webViewCurrentHeight = height;
	}*/
}
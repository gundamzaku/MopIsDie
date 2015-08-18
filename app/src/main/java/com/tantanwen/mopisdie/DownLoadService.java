package com.tantanwen.mopisdie;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.tantanwen.mopisdie.receiver.DownLoadReceiver;
import com.tantanwen.mopisdie.utils.Download;

public class DownLoadService extends Service {
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private CommandReceiver cmdReceiver;
    private boolean flag;
    private Download l;
    private String downloadUrl;
    private Context mContext;

    public DownLoadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     *
     */
    public void onCreate() {

        cmdReceiver = new CommandReceiver();
        flag = true;
        mContext = this;

        //开启一个消息栏通知
        System.out.println("新建一个下载");
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.status_bar_download);//通知栏中进度布局
        //load = (ProgressBar) findViewById(R.id.progressBar_download);
        mNotificationManager=(NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(DownLoadService.this);
        mBuilder.setSmallIcon(R.drawable.send);
        mBuilder.setTicker("正在下载中……");
        mBuilder.setOngoing(false);//意思是可不可以手动移除这个通知
        mBuilder.setContentTitle("hello world");
        mBuilder.setContentText("no,you are stupid");
        mBuilder.setContent(remoteViews);

        Intent i = new Intent(this, FileDownLoad.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);

        mBuilder.setContentIntent(pendingIntent);

    }

    public int onStartCommand(Intent intent, int flags,int startId) {

        downloadUrl = intent.getStringExtra("downloadUrl");

        IntentFilter filter = new IntentFilter();//创建IntentFilter对象
        filter.addAction("com.tantanwen.mopisdie.DownLoadService");
        registerReceiver(cmdReceiver, filter);//注册Broadcast Receiver

        //设置进度条，最大值 为100,当前值为0，最后一个参数为true时显示条纹
        //开始下载，转入后台
        //新线程下载
        new Thread(new Runnable() {
            public Intent intentThread;

            @Override
            public void run() {

                l = new Download(downloadUrl);

                mBuilder.build().contentView.setProgressBar(R.id.progressBar_download, l.getLength(), 0, false);
                monitorProgress();
                intentThread = new Intent();//创建Intent对象
                intentThread.setAction("com.tantanwen.mopisdie.FileDownLoad");

                int status = l.down2sd("mop_temp/", "mop.apk", l.new downhandler() {
                    public int sizeAll=0;

                    @Override
                    public void setSize(int size) {
                        sizeAll +=size;
                        if( sizeAll>10240 || size == 0) {
                            Message msg = handlerDownload.obtainMessage();
                            msg.arg1 = sizeAll;
                            msg.sendToTarget();
                            intentThread.putExtra("data", msg.arg1);
                            sendBroadcast(intentThread);//发送广播
                            sizeAll = 0;
                        }
                        //Log.d("log", Integer.toString(size));
                    }
                });

            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }


    private void monitorProgress(){
        //mBuilder.build().contentView.setProgressBar(R.id.content_view_progress, 100, progress, false);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private int count;
    private boolean stop = false;
    final Handler handlerDownload = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //每一次就来1024个字节
            count += msg.arg1;
            //移除时要把整个事件停掉！
            //这里就一条消息
            //System.out.println(count);
            mBuilder.build().contentView.setProgressBar(R.id.progressBar_download, l.getLength(), count, false);
            monitorProgress();
            //if(count<100) handler.postDelayed(run, 200);
            //200毫秒count加1
            if(count>=l.getLength()  ){
                if(stop == false) {
                    System.out.println(count);
                    mBuilder.build().contentView.setProgressBar(R.id.progressBar_download, l.getLength(), l.getLength(), false);
                    mBuilder.build().contentView.setTextViewText(R.id.text_download, "下载完成，点击打开。");
                    Intent i = l.openFile();
                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, i, 0);
                    mBuilder.setContentIntent(pendingIntent);
                    //mContext.startActivity(i);
                    stop = true;
                }
            }
        }
    };

    private class CommandReceiver extends BroadcastReceiver {//继承自BroadcastReceiver的子类
        @Override
        public void onReceive(Context context, Intent intent) {//重写onReceive方法
            int cmd = intent.getIntExtra("cmd", -1);//获取Extra信息
            System.out.println("cmd is :"+cmd);
            if(cmd == DownLoadReceiver.CMD_STOP_SERVICE){//如果发来的消息是停止服务
                flag = false;//停止线程
                stopSelf();//停止服务
                l.stop();
                mNotificationManager.cancel(0);
            }
        }
    }
    @Override
    public void onDestroy() {//重写onDestroy方法
        this.unregisterReceiver(cmdReceiver);//取消注册的CommandReceiver
        super.onDestroy();
    }
}

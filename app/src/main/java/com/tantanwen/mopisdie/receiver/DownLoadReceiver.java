package com.tantanwen.mopisdie.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownLoadReceiver extends BroadcastReceiver {

    public static final int CMD_STOP_SERVICE = 0;

    public DownLoadReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        double data = intent.getIntExtra("data", 0);
        System.out.println(data);
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
    }
}

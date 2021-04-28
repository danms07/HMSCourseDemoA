package com.hms.demo.hmscoursedemoa;

import android.util.Log;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

public class MyHmsMessageService extends HmsMessageService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.i("Token",token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        

    }
}

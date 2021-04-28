package com.hms.demo.hmscoursedemoa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.huawei.hms.kit.awareness.barrier.BarrierStatus;

class HeadsetBarrierReceiver extends BroadcastReceiver {
    public static final String BARRIER_RECEIVER_ACTION ="com.hms.demo.HEADSET_BARRIER_RECEIVER_ACTION";
    private static final String TAG="Barrier";
    private OnBarrierEventListener listener;
    private boolean isRegistered=false;
    public HeadsetBarrierReceiver(){
    }
    public HeadsetBarrierReceiver(OnBarrierEventListener listener){
        this.listener=listener;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        BarrierStatus barrierStatus = BarrierStatus.extract(intent);
        String label = barrierStatus.getBarrierLabel();
        String status;
        switch(barrierStatus.getPresentStatus()) {
            case BarrierStatus.TRUE:
                status=" status:connected";
                break;
            case BarrierStatus.FALSE:
                status=" status:disconnected";
                break;
            case BarrierStatus.UNKNOWN:
                status=" status:unknown";
                break;

            default:status="";
        }
        Log.i(TAG, label + status);
        if(listener!=null){
            listener.onBarrierEvent(label+status);
        }
    }

    public void register(Context context){
        context.registerReceiver(this, new IntentFilter(BARRIER_RECEIVER_ACTION));
        isRegistered=true;
    }

    public interface OnBarrierEventListener{
        void onBarrierEvent(String event);
    }
}

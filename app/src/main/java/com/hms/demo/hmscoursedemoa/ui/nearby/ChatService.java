package com.hms.demo.hmscoursedemoa.ui.nearby;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.huawei.hms.nearby.Nearby;
import com.huawei.hms.nearby.StatusCode;
import com.huawei.hms.nearby.discovery.BroadcastOption;
import com.huawei.hms.nearby.discovery.ConnectCallback;
import com.huawei.hms.nearby.discovery.ConnectInfo;
import com.huawei.hms.nearby.discovery.ConnectResult;
import com.huawei.hms.nearby.discovery.DiscoveryEngine;
import com.huawei.hms.nearby.discovery.Policy;
import com.huawei.hms.nearby.discovery.ScanEndpointCallback;
import com.huawei.hms.nearby.discovery.ScanEndpointInfo;
import com.huawei.hms.nearby.discovery.ScanOption;
import com.huawei.hms.nearby.transfer.Data;
import com.huawei.hms.nearby.transfer.DataCallback;
import com.huawei.hms.nearby.transfer.TransferEngine;
import com.huawei.hms.nearby.transfer.TransferStateUpdate;

import java.nio.charset.Charset;

public class ChatService extends ConnectCallback {
    private final String myName;
    private final String friendName;
    private final String serviceId;
    private String endpointId;
    private Context context;
    private DiscoveryEngine mDiscoveryEngine = null;
    private TransferEngine mTransferEngine = null;
    private int connectTaskResult;
    private ChatServiceListener listener;
    private static final String TAG = "ChatService";
    private static final int TIMEOUT_MILLISECONDS = 10000;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            handler.removeMessages(0);
            Log.e(TAG,"messageReceived");
            if (connectTaskResult != StatusCode.STATUS_SUCCESS) {
                if (listener != null) {
                    listener.onDisconnected();
                }
                displayToast("Connection timeout, make sure your friend is ready and try again.");
                if (myName.compareTo(friendName) > 0) {
                    mDiscoveryEngine.stopScan();
                } else {
                    mDiscoveryEngine.stopBroadcasting();
                }
            }
        }
    };

    public ChatService(String myName, String friendName) {
        this.myName = myName;
        this.friendName = friendName;
        connectTaskResult = StatusCode.STATUS_ENDPOINT_UNKNOWN;
        if (myName.compareTo(friendName) > 0) {
            serviceId = myName + friendName;
        } else {
            serviceId = friendName + myName;
        }
    }

    public void setListener(ChatServiceListener listener) {
        this.listener = listener;
    }

    public void connect(Context context) {
        displayToast("Connecting to your friend.");
        this.context = context;
        mDiscoveryEngine = Nearby.getDiscoveryEngine(context);

        try{
            if (myName.compareTo(friendName) > 0) {
                startScanning();//Client Mode
            } else {
                startBroadcasting();//Server Mode
            }
        }catch (RemoteException e){
            Log.e(TAG,e.toString());
        }
        Log.e(TAG,"sending message");
        handler.sendEmptyMessageDelayed(0, TIMEOUT_MILLISECONDS);
    }

    public void startScanning() throws RemoteException {
        Log.e(TAG,"startScanning()");
        ScanOption.Builder discBuilder = new ScanOption.Builder();
        discBuilder.setPolicy(Policy.POLICY_STAR);
        mDiscoveryEngine.startScan(serviceId, scanEndpointCallback, discBuilder.build());
    }

    public void startBroadcasting() throws RemoteException {
        Log.e(TAG,"startBroadcasting()");
        BroadcastOption.Builder advBuilder = new BroadcastOption.Builder();
        advBuilder.setPolicy(Policy.POLICY_STAR);
        mDiscoveryEngine.startBroadcasting(myName, serviceId, this, advBuilder.build());
    }

    private void displayToast(String message) {
        if (listener != null) listener.showToast(message);
    }

    public void sendMessage(String message) {
        Data data = Data.fromBytes(message.getBytes(Charset.defaultCharset()));
        Log.d(TAG, "myEndpointId " + endpointId);
        mTransferEngine.sendData(endpointId, data)
                .addOnSuccessListener((result) -> {
                    MessageBean item = new MessageBean();
                    item.setMyName(myName);
                    item.setFriendName(friendName);
                    item.setMsg(message);
                    item.setSend(true);
                    if (listener != null) listener.onMessageSent(item);
                });

    }


    private final ScanEndpointCallback scanEndpointCallback =
            new ScanEndpointCallback() {
                @Override
                public void onFound(String endpointId, ScanEndpointInfo discoveryEndpointInfo) {
                    ChatService.this.endpointId = endpointId;
                    mDiscoveryEngine.requestConnect(myName, endpointId, ChatService.this);
                }

                @Override
                public void onLost(String endpointId) {
                    Log.d(TAG, "Nearby Connection Demo app: Lost endpoint: " + endpointId);
                }
            };

    private final DataCallback dataCallback =
            new DataCallback() {
                @Override
                public void onReceived(String string, Data data) {
                    MessageBean item = new MessageBean();
                    item.setMyName(myName);
                    item.setFriendName(friendName);
                    item.setMsg(new String(data.asBytes()));
                    item.setSend(false);
                    if (listener != null) listener.onMessageReceived(item);
                }

                @Override
                public void onTransferUpdate(String string, TransferStateUpdate update) {
                }
            };

    @Override
    public void onEstablish(String endpointId, ConnectInfo connectionInfo) {
        mTransferEngine = Nearby.getTransferEngine(context);
        this.endpointId = endpointId;
        mDiscoveryEngine.acceptConnect(endpointId, dataCallback);
        displayToast("Let's chat!");
        connectTaskResult = StatusCode.STATUS_SUCCESS;
        if (listener != null) listener.onConnection();
        if (myName.compareTo(friendName) > 0) {
            mDiscoveryEngine.stopScan();
        } else {
            mDiscoveryEngine.stopBroadcasting();
        }
    }

    @Override
    public void onResult(String endpointId, ConnectResult resolution) {
        this.endpointId = endpointId;
    }

    @Override
    public void onDisconnected(String endpointId) {
        displayToast("Disconnect.");
        connectTaskResult = StatusCode.STATUS_NOT_CONNECTED;
    }

    private final ConnectCallback connectCallback= new ConnectCallback(){
        @Override
        public void onEstablish(String s, ConnectInfo connectInfo) {

        }
        @Override
        public void onResult(String s, ConnectResult connectResult) {

        }
        @Override
        public void onDisconnected(String s) {

        }
    };

    public interface ChatServiceListener {
        void showToast(String message);

        void onConnection();

        void onDisconnected();

        void onMessageReceived(MessageBean item);

        void onMessageSent(MessageBean item);
    }

}

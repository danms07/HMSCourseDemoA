package com.hms.demo.hmscoursedemoa.ui.nearby;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hms.demo.hmscoursedemoa.databinding.NearbyBinding;

import java.util.ArrayList;

public class NearbyFragment extends Fragment implements ChatService.ChatServiceListener {

    private static final int PERMISSION_CODE = 1;
    private NearbyBinding binding;
    private ArrayList<MessageBean> messages;
    private ChatService chatService;
    private ChatAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //nearbyViewModel = new ViewModelProvider(this).get(NearbyViewModel.class);
        binding = NearbyBinding.inflate(inflater, container, false);

        binding.btnConnect.setOnClickListener((view) -> {
            if (checkPermissions()) {
                setupConnection();
            } else requestPermissions();
        });

        binding.btnSend.setOnClickListener((view) -> {
                    String message = binding.etMsg.getText().toString();
                    if (isMessageValid(message)) {
                        binding.etMsg.setText("");
                        chatService.sendMessage(message);
                    }
                }
        );
        return binding.getRoot();
    }

    public void setupConnection() {
        String myName = binding.etMyName.getText().toString();
        String friendName = binding.etFriendName.getText().toString();
        if (validateNames(myName, friendName)) {
            if (chatService == null)
                chatService = new ChatService(myName, friendName);
            chatService.setListener(this);
            chatService.connect(requireContext().getApplicationContext());

        }
    }


    private boolean validateNames(String myName, String friendName) {
        if (TextUtils.isEmpty(myName)) {
            showShortToastTop("Please input your name.");
            return false;
        }
        if (TextUtils.isEmpty(friendName)) {
            showShortToastTop("Please input your friend's name.");
            return false;
        }
        if (TextUtils.equals(myName, friendName)) {
            showShortToastTop("Please input two different names.");
            return false;
        }
        return true;
    }


    public boolean checkPermissions() {
        int acl = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int afl = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        return acl == PackageManager.PERMISSION_GRANTED || afl == PackageManager.PERMISSION_GRANTED;
    }

    public String[] getPermissions() {
        return new String[]{Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};
    }

    public void requestPermissions() {
        requestPermissions(getPermissions(), PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        {
            if (checkPermissions()) setupConnection();
        }
    }

    public void showShortToastTop(String msg) {
        Toast toast = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    private boolean isMessageValid(String message) {
        if (TextUtils.isEmpty(message)) {
            showShortToastTop("Please input data you want to send.");
            return false;
        }
        return true;
    }


    @Override
    public void showToast(String message) {
        showShortToastTop(message);
    }

    @Override
    public void onConnection() {
        messages = new ArrayList<>();
        adapter = new ChatAdapter();
        adapter.setItems(messages);
        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        manager.setReverseLayout(true);
        binding.recycler.setLayoutManager(manager);
        binding.recycler.setAdapter(adapter);
    }

    @Override
    public void onDisconnected() {
        Log.e("TAG","onDisconnected");
    }

    @Override
    public void onMessageReceived(MessageBean item) {
        messages.add(0,item);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMessageSent(MessageBean item) {
        messages.add(0,item);
        adapter.notifyDataSetChanged();
    }
}
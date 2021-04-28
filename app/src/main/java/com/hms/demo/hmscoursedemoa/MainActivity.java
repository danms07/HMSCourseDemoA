package com.hms.demo.hmscoursedemoa;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.hms.demo.hmscoursedemoa.databinding.MainBinding;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.kit.awareness.Awareness;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest;
import com.huawei.hms.kit.awareness.barrier.HeadsetBarrier;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements HeadsetBarrierReceiver.OnBarrierEventListener {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private HeadsetBarrierReceiver barrierReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.hms.demo.hmscoursedemoa.databinding.MainBinding binding = MainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        BannerView bannerView = findViewById(R.id.bannerView);
        bannerView.loadAd(new AdParam.Builder().build());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_map, R.id.nav_remote, R.id.nav_nearby, R.id.nav_iap, R.id.nav_safety, R.id.nav_panorama)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        //Awareness kit
        setupHeadsetAwareness();
    }

    private void setupHeadsetAwareness() {
        AwarenessBarrier headsetBarrier = HeadsetBarrier.connecting();
        Intent intent = new Intent(HeadsetBarrierReceiver.BARRIER_RECEIVER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        barrierReceiver = new HeadsetBarrierReceiver(this);
        barrierReceiver.register(this);
        String headsetBarrierLabel = "headset connection status";
        // Define a request for updating a barrier.
        BarrierUpdateRequest.Builder builder = new BarrierUpdateRequest.Builder();
        BarrierUpdateRequest request = builder.addBarrier(headsetBarrierLabel, headsetBarrier, pendingIntent).build();
        Awareness.getBarrierClient(this).updateBarriers(request)
                // Callback listener for execution success.
                .addOnSuccessListener((aVoid) -> Toast.makeText(getApplicationContext(), "add barrier success", Toast.LENGTH_SHORT).show()
                )
                // Callback listener for execution failure.
                .addOnFailureListener((e) -> {
                            unregisterReceiver(barrierReceiver);
                            Toast.makeText(getApplicationContext(), "add barrier failed", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "add barrier failed", e);
                        }
                );
    }

    @Override
    public void onBarrierEvent(String event) {
        Toast.makeText(this, event, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_scan) {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        BarrierUpdateRequest.Builder builder = new BarrierUpdateRequest.Builder();
        builder.deleteAll();
        Awareness.getBarrierClient(this).updateBarriers(builder.build());
        if(barrierReceiver!=null&&barrierReceiver.isRegistered()){
            unregisterReceiver(barrierReceiver);
        }
        super.onDestroy();
    }
}
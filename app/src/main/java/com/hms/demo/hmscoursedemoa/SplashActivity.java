package com.hms.demo.hmscoursedemoa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.AudioFocusType;
import com.huawei.hms.ads.splash.SplashView;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;

public class SplashActivity extends AppCompatActivity {

    private static final int AD_TIMEOUT = 5000;
    private static final int MSG_AD_TIMEOUT = 1001;
    //private boolean hasPaused = false;
    //private Intent jumpIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //loadAd();
        silentSignIn();
    }

    // Callback processing when an ad display timeout message is received.
    private final Handler timeoutHandler = new Handler((msg)-> {
            if (SplashActivity.this.hasWindowFocus()) {
                silentSignIn();
            }
            return false;
        }
    );

    public void silentSignIn(){
        //HMS Account silent sign in
        AccountAuthParams authParams = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM).createParams();
        AccountAuthService service = AccountAuthManager.getService(SplashActivity.this, authParams);
        Task<AuthAccount> task = service.silentSignIn();
        task.addOnSuccessListener((account)->{
            Intent intent=new Intent(this,MainActivity.class);
            intent.putExtra(LoginActivity.ACCOUNT_KEY,account);
            startActivity(intent);
            finish();
        });
        task.addOnFailureListener((e)->{
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        } );
    }

    private void loadAd() {
        int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        AdParam adParam = new AdParam.Builder().build();
        SplashView.SplashAdLoadListener splashAdLoadListener = new SplashView.SplashAdLoadListener() {

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Called when an ad fails to be loaded. The app home screen is then displayed.
                Toast.makeText(SplashActivity.this,"Ad failed "+errorCode,Toast.LENGTH_SHORT).show();
//                timeoutHandler.removeMessages(MSG_AD_TIMEOUT);
//                silentSignIn();
            }
            @Override
            public void onAdDismissed() {
                //Toast.makeText(SplashActivity.this,"onAdDismissed",Toast.LENGTH_SHORT).show();
                // Called when the display of an ad is complete. The app home screen is then displayed.
                timeoutHandler.removeMessages(MSG_AD_TIMEOUT);
                silentSignIn();
            }
        };
        // Obtain SplashView.
        SplashView splashView = findViewById(R.id.splash_ad_view);
        // Set the default slogan.
        //splashView.setSloganResId(R.drawable.default_slogan);
        // Set the audio focus type for a video splash ad.
        splashView.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE);
        // Load the ad. AD_ID indicates the ad slot ID.
        splashView.load(getString(R.string.splashAdId), orientation, adParam, splashAdLoadListener);
        // Send a delay message to ensure that the app home screen can be properly displayed after the ad display times out.
        timeoutHandler.removeMessages(MSG_AD_TIMEOUT);
        timeoutHandler.sendEmptyMessageDelayed(MSG_AD_TIMEOUT, AD_TIMEOUT);
    }

}
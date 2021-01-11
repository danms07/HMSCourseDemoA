package com.hms.demo.hmscoursedemoa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
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
        task.addOnFailureListener((e)-> startActivity(new Intent(this,LoginActivity.class)));
    }
}
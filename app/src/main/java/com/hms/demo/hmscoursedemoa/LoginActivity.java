package com.hms.demo.hmscoursedemoa;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.hms.demo.hmscoursedemoa.databinding.LoginBinding;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;

public class LoginActivity extends AppCompatActivity implements LoginViewModel.LoginNavigator {

    public static final int LOGIN_CODE=100;
    public static final String ACCOUNT_KEY="ACCOUNT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginBinding binding=LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        LoginViewModel viewModel = new ViewModelProvider(this)
                .get(LoginViewModel.class);
        viewModel.setNavigator(this);
        binding.setViewModel(viewModel);
    }

    @Override
    public void openSignInPage(AccountAuthService service) {
        startActivityForResult(service.getSignInIntent(),LOGIN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==LOGIN_CODE){
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                // The sign-in is successful, and the user's ID information and authorization code are obtained.
                AuthAccount authAccount = authAccountTask.getResult();
                Intent intent =new Intent(this,MainActivity.class);
                intent.putExtra(ACCOUNT_KEY,authAccount);
                startActivity(intent);
            } else {
                // The sign-in failed.
                Toast.makeText(this,
                        "sign in failed:" + (authAccountTask.getException()),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
package com.hms.demo.hmscoursedemoa;

import android.content.Context;
import android.view.View;

import androidx.lifecycle.ViewModel;

import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.service.AccountAuthService;

public class LoginViewModel extends ViewModel {

    private LoginNavigator navigator;

    public void setNavigator(LoginNavigator navigator) {
        this.navigator = navigator;
    }

    public void signIn(Context context){
        AccountAuthParams authParams = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode().createParams();
        AccountAuthService service = AccountAuthManager.getService(context, authParams);
        if(navigator!=null)navigator.openSignInPage(service);
    }


    interface LoginNavigator{
        void openSignInPage(AccountAuthService service);
    }
}

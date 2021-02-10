package com.hms.demo.hmscoursedemoa.ui.nearby;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NearbyViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public NearbyViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is nearby fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
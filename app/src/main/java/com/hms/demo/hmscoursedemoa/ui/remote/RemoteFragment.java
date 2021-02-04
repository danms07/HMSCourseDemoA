package com.hms.demo.hmscoursedemoa.ui.remote;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hms.demo.hmscoursedemoa.R;
import com.hms.demo.hmscoursedemoa.databinding.FragmentRemoteBinding;
import com.huawei.agconnect.remoteconfig.AGConnectConfig;
import com.huawei.agconnect.remoteconfig.ConfigValues;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.location.LocationCallback;

public class RemoteFragment extends Fragment implements OnSuccessListener<ConfigValues> {

    public static final long MINUTE=60;
    public static final long HOUR=MINUTE*60;
    public static final String WHITE="WHITE";
    public static final String RED="RED";
    public static final String BLUE="BLUE";
    public static final String BLACK="BLACK";

    TextView textView;
    AGConnectConfig config;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FragmentRemoteBinding binding=FragmentRemoteBinding.inflate(getLayoutInflater());
        textView=binding.remoteText;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchRemoteParameters();
    }


    private void fetchRemoteParameters() {
        config = AGConnectConfig.getInstance();
        config.fetch(10).addOnSuccessListener(this).addOnFailureListener(
                (failure)->{
                    Log.e("Remote",failure.toString());
                }
        );
    }

    @Override
    public void onSuccess(ConfigValues configValues) {
        config.apply(configValues);

        String textValue=config.getValueAsString("text");
        long textSize=config.getValueAsLong("size");
        Log.e("Remote",textValue+" "+textSize);
        textView.setText(textValue);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,textSize);

        String textColor=config.getValueAsString("text_color");
        int fontColor;
        Resources res=requireContext().getResources();
        switch (textColor){
            case WHITE:
                fontColor=res.getColor(R.color.white,requireContext().getTheme());
                break;
            case BLUE:
                fontColor=res.getColor(R.color.blue,requireContext().getTheme());
                break;
            case RED:
                fontColor=res.getColor(R.color.red,requireContext().getTheme());
                break;
            default:
                fontColor=res.getColor(R.color.black,requireContext().getTheme());
                break;
        }

        textView.setTextColor(fontColor);

        String bgColor=config.getValueAsString("bg_color");
        int backgroundColor;
        switch (bgColor){
            case WHITE:
                backgroundColor=res.getColor(R.color.white,requireContext().getTheme());
                break;
            case BLUE:
                backgroundColor=res.getColor(R.color.blue,requireContext().getTheme());
                break;
            case RED:
                backgroundColor=res.getColor(R.color.red,requireContext().getTheme());
                break;
            default:
                backgroundColor=res.getColor(R.color.black,requireContext().getTheme());
                break;
        }

        textView.setBackgroundColor(backgroundColor);
    }
}
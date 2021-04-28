package com.hms.demo.hmscoursedemoa.ui.panorama;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hms.demo.hmscoursedemoa.R;
import com.hms.demo.hmscoursedemoa.databinding.PanoramaBinding;
import com.huawei.hms.panorama.Panorama;
import com.huawei.hms.panorama.PanoramaInterface;
import com.huawei.hms.panorama.PanoramaLocalApi;
import com.huawei.hms.support.api.client.ResultCallback;

public class PanoramaFragment extends Fragment implements ResultCallback<PanoramaInterface.ImageInfoResult> {
    private static final String TAG = "LocalInterfaceActivity";
    private PanoramaBinding binding;
    private PanoramaInterface.PanoramaLocalInterface localInterface;
    private boolean panoramaFlag = false;
    private Uri currentUri;
    private final int[] controls={PanoramaInterface.CONTROL_TYPE_TOUCH,PanoramaInterface.CONTROL_TYPE_POSE,PanoramaInterface.CONTROL_TYPE_MIX};
    private int controlMode=0;
    public PanoramaFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = PanoramaBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentUri = getUriFromResource(R.raw.pano);
        int type = PanoramaInterface.IMAGE_TYPE_SPHERICAL;
        binding.changeButton.setOnClickListener((v)->changeLocalPanorama());
        binding.controlBtn.setOnClickListener((v)->changeControlMode());
        binding.fullScreenBtn.setOnClickListener((v)->loadFullScreenPanorama());
        initLocalPanorama(currentUri, type);
    }

    private void loadFullScreenPanorama() {
        Panorama.getInstance()
                .loadImageInfoWithPermission(
                        requireContext(),
                        currentUri,
                        PanoramaInterface.IMAGE_TYPE_RING
                )
                .setResultCallback(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initLocalPanorama(Uri uri, int type) {
        if (localInterface == null) {
            localInterface = Panorama.getInstance().getLocalInstance(requireContext());
        }
        if (localInterface.init() == 0 && localInterface.setImage(uri, type) == 0) {
            localInterface.setControlMode(controls[controlMode]);
            View view = localInterface.getView();
            binding.container.addView(view);
            view.setOnTouchListener((v, event) -> {
                localInterface.updateTouchEvent(event);
                return true;
            });

        }
    }

    private Uri getUriFromResource(int resourceId) {
        return Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + resourceId);
    }

    private void changeControlMode(){
        controlMode=controlMode==2?0:controlMode+1;
        localInterface.setControlMode(controls[controlMode]);
        String mode;
        switch (controlMode){
            case 0: mode ="Touch";
                break;

            case 1: mode="Pose";
                break;

            case 2: mode="Mix";
                break;
            default:mode="";
        }
        Toast.makeText(requireContext(),mode,Toast.LENGTH_SHORT).show();
    }

    private void changeLocalPanorama() {
        panoramaFlag=!panoramaFlag;
        int resource=panoramaFlag?R.raw.pano2:R.raw.pano;
        currentUri=getUriFromResource(resource);
        localInterface.setImage(
                currentUri,
                PanoramaLocalApi.IMAGE_TYPE_SPHERICAL
        );
    }

    @Override
    public void onResult(PanoramaInterface.ImageInfoResult result) {
        if (result != null && result.getStatus().isSuccess()) {
            requireContext().startActivity(result.getImageDisplayIntent());
        }
    }
}
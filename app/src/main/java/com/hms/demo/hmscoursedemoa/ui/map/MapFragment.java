package com.hms.demo.hmscoursedemoa.ui.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hms.demo.hmscoursedemoa.R;
import com.hms.demo.hmscoursedemoa.databinding.MapBinding;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Site;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MapFragment extends Fragment implements MapViewModel.MapNavigator {

    private MapViewModel mapViewModel;
    private MapBinding binding;
    public static final int LOCATION_PERMISSION_CODE=100;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapViewModel =new ViewModelProvider(this).get(MapViewModel.class);
        mapViewModel.setNavigator(this);
        binding=MapBinding.inflate(getLayoutInflater());
        binding.setViewModel(mapViewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpGPS();
        binding.mapView.onCreate(null);
        binding.mapView.getMapAsync(mapViewModel);
    }

    public void setUpGPS(){
        if(mapViewModel.checkLocationPermissions(requireContext())){
            mapViewModel.setUpGPS(requireContext());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapViewModel.onPause();
        binding.mapView.onPause();
    }



    @Override
    public void requestLocationPermissions() {
        requestPermissions(
                new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_CODE);
    }

    @Override
    public void startResolutionForResult(ResolvableApiException e) {
        try {
            e.startResolutionForResult(requireActivity(),LOCATION_PERMISSION_CODE);
        } catch (IntentSender.SendIntentException sendIntentException) {
            sendIntentException.printStackTrace();
        }
    }

    @Override
    public SearchService loadSearchService() {
        String key= AGConnectServicesConfig
                .fromContext(requireContext())
                .getString("client/api_key");
        try {
            String apiKey= URLEncoder.encode(key, StandardCharsets.UTF_8.name());
            return  SearchServiceFactory.create(requireContext(),
                    key);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public void displaySiteDialog(Site site) {
        AlertDialog.Builder builder=new AlertDialog.Builder(requireContext())
        .setTitle(site.name)
        .setMessage(site.formatAddress)
        .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_PERMISSION_CODE){
            setUpGPS();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }
}
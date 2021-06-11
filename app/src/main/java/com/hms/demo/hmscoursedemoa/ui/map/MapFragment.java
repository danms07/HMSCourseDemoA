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
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.BitmapDescriptor;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.maps.model.Polygon;
import com.huawei.hms.maps.model.PolygonOptions;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Site;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MapFragment extends Fragment implements MapViewModel.MapNavigator, OnMapReadyCallback, HuaweiMap.OnMarkerDragListener {

    private MapViewModel mapViewModel;
    private MapBinding binding;
    private HuaweiMap map;
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
        binding.mapView.getMapAsync(this);
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
    public void navigateToLocation(LatLng location) {
        CameraUpdate update= CameraUpdateFactory.newLatLngZoom(location,15f);
        if(map!=null){
            map.animateCamera(update);
        }
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

    private Polygon polygon1;

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        huaweiMap.setOnPoiClickListener(mapViewModel);
        this.map=huaweiMap;
        //Creamos un marcador
        MarkerOptions markerOptions=new MarkerOptions();
        LatLng position=new LatLng(19.0,-99.0);
        markerOptions.position(position);

        BitmapDescriptor descriptor=BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
        markerOptions.icon(descriptor);
        //Hacemos que el usuario pueda arrastrar el marcador
        markerOptions.draggable(true);
        //Agregamos
        huaweiMap.addMarker(markerOptions);
        //Escuchamos el movimiento del marcador
        huaweiMap.setOnMarkerDragListener(this);
        //Agrupamos marcadores cercanos
        huaweiMap.setMarkersClustering(true);
        //Movemos la camara
        huaweiMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,15.0f));
        //Creamos un poligono
        ArrayList<LatLng> points=new ArrayList<>();
        points.add(new LatLng(19.0,-99.0));
        points.add(new LatLng(19.0,-98.0));
        points.add(new LatLng(18.0,-98.5));
        PolygonOptions polygon=new PolygonOptions();
        polygon.addAll(points);
        polygon1=huaweiMap.addPolygon(polygon);


    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        if(polygon1!=null){
            polygon1.remove();
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
}
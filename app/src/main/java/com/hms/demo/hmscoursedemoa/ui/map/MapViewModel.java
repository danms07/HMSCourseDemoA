package com.hms.demo.hmscoursedemoa.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.lifecycle.ViewModel;

import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.LatLng;

public class MapViewModel extends ViewModel
        implements OnMapReadyCallback,GPS.OnGPSEventListener {

    private MapNavigator navigator;
    private boolean locationPermissions;
    private GPS gps=null;
    private final LatLng location;
    private boolean pendingLocation=false;
    private HuaweiMap map;

    public MapViewModel() {
        locationPermissions=false;
        location=new LatLng(0,0);
    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        this.map=huaweiMap;
    }

    public void setNavigator(MapNavigator navigator){
        this.navigator=navigator;
    }

    public void onLocationButtonPressed(Context context){
        pendingLocation=true;//The map will navigate upon the next location update
        if(gps==null) {
            locationPermissions=checkLocationPermissions(context);
            if(!locationPermissions&&navigator!=null){
                navigator.requestLocationPermissions();
            }
        }
    }

    public void setUpGPS(Context context){
        if(gps==null){
            gps=new GPS(context);
            gps.setListener(this);
        }
        gps.startLocationRequests();
    }

    public void onPause(){
        if(gps!=null&&gps.isStarted()){
            gps.removeLocationUpdates();
        }
    }

    public boolean checkLocationPermissions(Context context){
        int afl=context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        int acl=context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        return afl == PackageManager.PERMISSION_GRANTED || acl == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onResolutionRequired(Exception e) {
        ResolvableApiException locationException=(ResolvableApiException) e;
        navigator.startResolutionForResult(locationException);

    }

    @Override
    public void onLocationUpdate(LatLng location) {
        if(pendingLocation){
            navigateToLocation(location);
        }
        this.location.latitude=location.latitude;
        this.location.longitude=location.longitude;

    }

    private void navigateToLocation(LatLng location) {

        CameraUpdate update=CameraUpdateFactory.newLatLngZoom(location,15f);
        if(map!=null){
            pendingLocation=false;
            map.animateCamera(update);
        }
    }

    interface MapNavigator{
        void requestLocationPermissions();
        void startResolutionForResult(ResolvableApiException e);
    }
}
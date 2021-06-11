package com.hms.demo.hmscoursedemoa.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.maps.model.PointOfInterest;
import com.huawei.hms.maps.model.PolygonOptions;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.DetailSearchRequest;
import com.huawei.hms.site.api.model.DetailSearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MapViewModel extends ViewModel
        implements  GPS.OnGPSEventListener,
        HuaweiMap.OnPoiClickListener, SearchResultListener<DetailSearchResponse> {

    private MapNavigator navigator;
    private boolean locationPermissions;
    private GPS gps=null;
    private final LatLng location;
    private boolean pendingLocation=false;

    public MapViewModel() {
        locationPermissions=false;
        location=new LatLng(0,0);
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
        if(pendingLocation&&navigator!=null){
            pendingLocation=false;
            navigator.navigateToLocation(location);
        }
        this.location.latitude=location.latitude;
        this.location.longitude=location.longitude;

    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        if(navigator!=null){
            SearchService service=navigator.loadSearchService();
            if(service!=null){
                DetailSearchRequest request=new DetailSearchRequest();
                request.siteId=pointOfInterest.placeId;
                service.detailSearch(request, this);
            }
        }
    }

    @Override
    public void onSearchResult(DetailSearchResponse detailSearchResponse) {
        if(detailSearchResponse!=null){
            if(detailSearchResponse.site!=null&&navigator!=null){
                navigator.displaySiteDialog(detailSearchResponse.site);
            }
        }
    }

    @Override
    public void onSearchError(SearchStatus searchStatus) {
        Log.e(
                "MapViewModel",
                "Error : " + searchStatus.errorCode + " " + searchStatus.errorMessage
        );
    }

    interface MapNavigator{
        void navigateToLocation(LatLng location);
        void requestLocationPermissions();
        void startResolutionForResult(ResolvableApiException e);
        SearchService loadSearchService();
        void displaySiteDialog(Site site);
    }
}
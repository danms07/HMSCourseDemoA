package com.hms.demo.hmscoursedemoa.ui.map;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.maps.model.LatLng;

public class GPS extends LocationCallback {
    private static final String TAG = "GPS Tracker";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Context context;
    private boolean isStarted = false;
    private OnGPSEventListener listener;

    public GPS(Context context) {
        this.context = context;
        if (context instanceof OnGPSEventListener) {
            this.listener = (OnGPSEventListener) context;
        }
    }

    public void startLocationRequests(){
        startLocationRequests(1000);
    }

    public void startLocationRequests(long interval) {
        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        // set the interval for location updates, in milliseconds.
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(interval);
        // set the priority of the request
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener((locationSettingsResponse) -> requestLocationUpdates(mLocationRequest))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "checkLocationSetting onFailure:" + e.toString());
                    ApiException apiException = (ApiException) e;
                    if (apiException.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        Log.e(TAG, "Resolution required");
                        if (listener != null)
                            listener.onResolutionRequired(e);
                    }
                });
    }


    private void requestLocationUpdates(LocationRequest mLocationRequest) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationProviderClient.requestLocationUpdates(
                mLocationRequest,
                this,
                Looper.getMainLooper()
        ).addOnSuccessListener(success -> isStarted = true)
                .addOnFailureListener(e ->
                        Log.e(TAG, "requestLocationUpdatesWithCallback onFailure:" + e.toString())
                );
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setListener(OnGPSEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        Location lastLocation = locationResult.getLastLocation();
        if (listener != null) {
            listener.onLocationUpdate(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
        }

    }

    public void removeLocationUpdates() {
        if (isStarted) try {
            fusedLocationProviderClient.removeLocationUpdates(this)
                    .addOnSuccessListener(success -> {
                        Log.i(
                                TAG,
                                "removeLocationUpdatesWithCallback onSuccess"
                        );
                        isStarted = false;
                    })
                    .addOnFailureListener(e -> {
                        Log.e(
                                TAG,
                                "removeLocationUpdatesWithCallback onFailure:" + e.toString()
                        );
                    });
        } catch (Exception e) {
            Log.e(TAG, "removeLocationUpdatesWithCallback exception:" + e.toString());
        }

    }



    public interface OnGPSEventListener {
        void onResolutionRequired(Exception e);
        void onLocationUpdate(LatLng latLng);
    }
}


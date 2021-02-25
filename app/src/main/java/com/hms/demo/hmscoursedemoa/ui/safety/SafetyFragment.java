package com.hms.demo.hmscoursedemoa.ui.safety;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hms.demo.hmscoursedemoa.databinding.SafetyBinding;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.entity.core.CommonCode;
import com.huawei.hms.support.api.entity.safetydetect.MaliciousAppsData;
import com.huawei.hms.support.api.entity.safetydetect.MaliciousAppsListResp;
import com.huawei.hms.support.api.entity.safetydetect.SysIntegrityRequest;
import com.huawei.hms.support.api.entity.safetydetect.SysIntegrityResp;
import com.huawei.hms.support.api.entity.safetydetect.WifiDetectResponse;
import com.huawei.hms.support.api.safetydetect.SafetyDetect;
import com.huawei.hms.support.api.safetydetect.SafetyDetectClient;
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class SafetyFragment extends Fragment implements View.OnClickListener {
    final static String TAG = "SafetyDetect";
    final static String RS256 = "RS256";
    final static String PS256 = "PS256";
    final static String KEY_APP_ID = "client/app_id";

    private boolean isUserDetectApiAvailable = false;
    private SafetyBinding binding;

    public SafetyFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUserDetect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=SafetyBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startSafety();
        binding.robotCheck.setOnClickListener(this);
    }

    private void startSafety() {
        invokeSysIntegrity();
        invokeGetMaliciousApps();
        invokeGetWifiDetectStatus();
    }

    private void invokeGetWifiDetectStatus() {
        Log.i(TAG, "Start to getWifiDetectStatus!");
        SafetyDetectClient wifiDetectClient = SafetyDetect.getClient(requireActivity());
        Task<WifiDetectResponse> task = wifiDetectClient.getWifiDetectStatus();
        task.addOnSuccessListener((wifiDetectResponse) -> {
                    int wifiDetectStatus = wifiDetectResponse.getWifiDetectStatus();
                    binding.wifi.setText("");
                    binding.wifi.append("\n-1: Failed to obtain the Wi-Fi status. \n" + "0: No Wi-Fi is connected. \n" + "1: The connected Wi-Fi is secure. \n" + "2: The connected Wi-Fi is insecure.");
                    binding.wifi.append( "wifiDetectStatus is: " + wifiDetectStatus);
                }
        ).addOnFailureListener((e) -> {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        binding.wifi.setText(
                                "Error: " + apiException.getStatusCode() + ":"
                                        + SafetyDetectStatusCodes.getStatusCodeString(apiException.getStatusCode()) + ": "
                                        + apiException.getStatusMessage());
                    } else {
                        Log.e(TAG, "ERROR! " + e.getMessage());
                    }
                }
        );
    }

    private void invokeGetMaliciousApps() {
        SafetyDetectClient appsCheckClient = SafetyDetect.getClient(requireActivity());
        Task<MaliciousAppsListResp> task = appsCheckClient.getMaliciousAppsList();
        task.addOnSuccessListener((maliciousAppsListResp) -> {
                    // Indicates that communication with the service was successful.
                    // Use resp.getMaliciousApps() to get malicious apps data.
                    List<MaliciousAppsData> appsDataList = maliciousAppsListResp.getMaliciousAppsList();
                    // Indicates get malicious apps was successful.
                    if (maliciousAppsListResp.getRtnCode() == CommonCode.OK) {
                        if (appsDataList.isEmpty()) {
                            // Indicates there are no known malicious apps.
                            binding.maliciousApps.setText("There are no known potentially malicious apps installed.");
                        } else {
                            Log.e(TAG, "Potentially malicious apps are installed!");
                            binding.maliciousApps.setText("");
                            for (MaliciousAppsData maliciousApp : appsDataList) {
                                binding.maliciousApps.append("APP");
                                binding.maliciousApps.append(
                                        "\nPackage:"+maliciousApp.getApkPackageName()+
                                                "\nCategory"+maliciousApp.getApkCategory()+
                                                "\n\n"
                                );
                                Log.e(TAG, "-----APP-----");
                                // Use getApkPackageName() to get APK name of malicious app.
                                Log.e(TAG, "APK: " + maliciousApp.getApkPackageName());
                                // Use getApkSha256() to get APK sha256 of malicious app.
                                Log.e(TAG, "SHA-256: " + maliciousApp.getApkSha256());
                                // Use getApkCategory() to get category of malicious app.
                                // Categories are defined in AppsCheckConstants
                                Log.e(TAG, "Category: " + maliciousApp.getApkCategory());
                            }
                        }
                    } else {
                        binding.maliciousApps.setText("getMaliciousAppsList failed: " + maliciousAppsListResp.getErrorReason());
                    }
                }
        ).addOnFailureListener((e) -> {
                    // An error occurred during communication with the service.
                    if (e instanceof ApiException) {
                        // An error with the HMS API contains some
                        // additional details.
                        ApiException apiException = (ApiException) e;
                        // You can retrieve the status code using the apiException.getStatusCode() method.
                        Log.e(TAG, "Error: " + SafetyDetectStatusCodes.getStatusCodeString(apiException.getStatusCode()) + ": " + apiException.getStatusMessage());
                    } else {
                        // A different, unknown type of error occurred.
                        Log.e(TAG, "ERROR: " + e.getMessage());
                    }
                }
        );
    }

    private void invokeSysIntegrity() {
        SafetyDetectClient mClient = SafetyDetect.getClient(requireActivity());
        // TODO(developer): Change the nonce generation to include your own, used once value,
        // ideally from your remote server.
        byte[] nonce = ("Course" + System.currentTimeMillis()).getBytes();
        String appId = AGConnectServicesConfig
                .fromContext(requireContext())
                .getString(KEY_APP_ID);
        SysIntegrityRequest sysintegrityrequest = new SysIntegrityRequest();
        sysintegrityrequest.setAppId(appId);
        sysintegrityrequest.setNonce(nonce);
        sysintegrityrequest.setAlg(RS256);
        Task<SysIntegrityResp> task = mClient.sysIntegrity(sysintegrityrequest);
        task.addOnSuccessListener((response) -> {
                    // Indicates communication with the service was successful.
                    // Use response.getResult() to get the result data.
                    String jwsStr = response.getResult();
                    try {
                        JSONObject payload=decodePayload(jwsStr);
                        boolean integrityCheck=payload.getBoolean("basicIntegrity");
                        String integrityMessage=integrityCheck?"OK":"Fail";
                        binding.sysIntegrity.setText(integrityMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        ).addOnFailureListener((e) -> {
            binding.sysIntegrity.setText("Error");
                    // An error occurred during communication with the service.
                    if (e instanceof ApiException) {
                        // An error with the HMS API contains some
                        // additional details.
                        ApiException apiException = (ApiException) e;
                        // You can retrieve the status code using
                        // the apiException.getStatusCode() method.
                        Log.e(TAG, "Error: " + SafetyDetectStatusCodes.getStatusCodeString(apiException.getStatusCode()) + ": " + apiException.getMessage());
                    } else {
                        // A different, unknown type of error occurred.
                        Log.e(TAG, "ERROR:" + e.getMessage());
                    }
                }
        );
    }

    private JSONObject decodePayload(String jwsEncoded) throws JSONException {
        String[] parts = jwsEncoded.split("\\.", 0);
            byte[] bytes = Base64.getUrlDecoder().decode(parts[1]);
            String decodedString = new String(bytes, StandardCharsets.UTF_8);
            Log.e("SysIntegrity", decodedString);
            return new JSONObject(decodedString);

    }

    private void initUserDetect() {
        // Replace with your activity or context as a parameter.
        SafetyDetectClient client = SafetyDetect.getClient(requireActivity());
        client.initUserDetect().addOnSuccessListener((v) -> {
            isUserDetectApiAvailable = true;
        }).addOnFailureListener((e) -> {
            isUserDetectApiAvailable = false;
        });
    }

    @Override
    public void onClick(View v) {
        SafetyDetectClient client = SafetyDetect.getClient(requireActivity());
        String appId = AGConnectServicesConfig
                .fromContext(requireContext())
                .getString(KEY_APP_ID);
        if(isUserDetectApiAvailable){
            client.userDetection(appId)
                    .addOnSuccessListener((userDetectResponse) -> {
                                shutdownUserDetect();
                                showValidDialog();
                                // Indicates that communication with the service was successful.
                                String responseToken = userDetectResponse.getResponseToken();
                                if (!responseToken.isEmpty()) {
                                    showValidDialog();
                                    // Send the response token to your app server, and call the cloud API of HMS Core on your server to obtain the fake user detection result.
                                }
                            }
                    )
                    .addOnFailureListener((e) -> {
                                shutdownUserDetect();
                                // An error occurred during communication with the service.
                                String errorMsg;
                                if (e instanceof ApiException) {
                                    // An error with the HMS API contains some additional details.
                                    // You can use the apiException.getStatusCode() method to get the status code.
                                    ApiException apiException = (ApiException) e;
                                    errorMsg = SafetyDetectStatusCodes.getStatusCodeString(apiException.getStatusCode()) + ": "
                                            + apiException.getMessage();
                                } else {
                                    // Unknown type of error has occurred.
                                    errorMsg = e.getMessage();
                                }
                                Log.i(TAG, "User detection fail. Error info: " + errorMsg);
                            }
                    );
        }else initUserDetect();
    }

    private void showValidDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Congratulations")
                .setMessage("Yo are not a Robot")
                .setCancelable(false)
                .setPositiveButton("ok",(dialog,which)->{
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void shutdownUserDetect() {
        // Replace with your activity or context as a parameter.
        SafetyDetectClient client = SafetyDetect.getClient(requireActivity());
        client.shutdownUserDetect().addOnSuccessListener((v) -> {
                    // Indicates that communication with the service was successful.
                }
        ).addOnFailureListener((e) -> {
                    // An error occurred during communication with the service.
                }
        );
    }
}
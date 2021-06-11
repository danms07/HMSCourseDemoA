package com.hms.demo.hmscoursedemoa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;

public class ScanActivity extends AppCompatActivity {
    private final static int CAMERA_REQUEST=1;
    private final static int SCAN_REQUEST=100;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Button scanBtn=findViewById(R.id.buttonScan);
        scanBtn.setOnClickListener((v)->onScanRequest());
        tv=findViewById(R.id.result);
    }

    public void onScanRequest(){
        if(checkCameraPermissions()){
            startScanning();
        }else requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE},CAMERA_REQUEST);
    }

    private boolean checkCameraPermissions() {
        int cam= ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int storage=ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return cam== PackageManager.PERMISSION_GRANTED&&storage==PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==CAMERA_REQUEST){
            if(checkCameraPermissions()){
                startScanning();
            }
        }
    }

    private void startScanning() {
        // QRCODE_SCAN_TYPE and DATAMATRIX_SCAN_TYPE are set for the barcode format, indicating that Scan Kit will support only QR code and Data Matrix.
        HmsScanAnalyzerOptions options = new HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE).create();
        ScanUtil.startScan(this, SCAN_REQUEST, options);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        if (requestCode == SCAN_REQUEST) {
            // Input an image for scanning and return the result.
            Object obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj instanceof HmsScan) {
                HmsScan result=(HmsScan) obj;
                if (!TextUtils.isEmpty(((HmsScan) obj).getOriginalValue())) {
                    tv.setText(result.getOriginalValue());
                    Toast.makeText(this, result.getOriginalValue(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
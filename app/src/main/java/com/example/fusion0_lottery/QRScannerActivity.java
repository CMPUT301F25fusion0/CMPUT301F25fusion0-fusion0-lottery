package com.example.fusion0_lottery;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fusion0_lottery.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;


import me.dm7.barcodescanner.zxing.ZXingScannerView;

/*
QR scanning code customized from:
Author: R. Zagorski https://stackoverflow.com/users/6507689/r-zag%c3%b3rski
Title: "How to scan QR code in Android"
Answer: https://stackoverflow.com/a/39656445
Date: sep 23, 2016 at 8:43
License: CC BY-SA 3.0
 */
/*
Request runtime permissions code from Android Developers documentation
Source: https://developer.android.com/training/permissions/requesting#java
License: Apache 2.0
 */
/**
 * purpose:
 *  This activity allows users to scan QR code within the app and
 *  allow them to navigate the event detail screen.
 *  design pattern:
 *  uses ZXing library for QR code scanning
 *  outstanding issues:
 *  no outstanding issues for now
 *
 */


public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private Button cancel;


    //The following function is from DeepSeek AI, "how to handle run time camera permission in android java using activity launch?", 2025-01-11

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // The following Layout setup with cancel button is from DeepSeek AI, "how to add cancel button to ZXingScannerView programmatically in Android Java", 2025-06-11
        FrameLayout mainLayout = new FrameLayout(this);
        mScannerView = new ZXingScannerView(this);
        mainLayout.addView(mScannerView);
        cancel = new Button(this);
        cancel.setText("Cancel");

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER
        );
        params.setMargins(0,0,0,100);
        cancel.setLayoutParams(params);
        cancel.setOnClickListener(view -> {
            if(mScannerView != null){
                mScannerView.stopCamera();
            }
            setResult(RESULT_CANCELED);
            finish();
        });
        mainLayout.addView(cancel);
        setContentView(mainLayout);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
               == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }

  }
    private void startCamera() {
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        mScannerView.setAutoFocus(true);
    }

    /**
     * used to resume the camera when the user initiates the QR code scanner
     */
    @Override
    public void onResume(){
        super.onResume();
        if(mScannerView != null) {
            mScannerView.setResultHandler(this);
            mScannerView.startCamera();
        }
    }

    /**
     * used to stop camera when QR code is scanned or canceled
     */
    @Override
    public void onPause(){
        super.onPause();
        if(mScannerView != null) {
            mScannerView.stopCamera();
        }
    }

    /**
     * used to validate the the scanned QR code
     * @param rawResult
     */
    @Override
    public void handleResult(Result rawResult){
        String result = rawResult.getText();
        BarcodeFormat format = rawResult.getBarcodeFormat();

        if(result != null && !result.isEmpty() && format.equals(BarcodeFormat.QR_CODE)){
                Intent result_intent = new Intent();
                result_intent.putExtra("EVENT_ID", result);
                setResult(RESULT_OK, result_intent);

                if(mScannerView != null){
                    mScannerView.stopCamera();
                }
                finish();
                Toast.makeText(this,"Valid QR code\n"+ result, Toast.LENGTH_SHORT).show();
            } else{
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show();
            if (mScannerView != null) {
                mScannerView.resumeCameraPreview(this);
            }
        }
    }

}
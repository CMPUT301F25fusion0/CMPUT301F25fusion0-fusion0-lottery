package com.example.fusion0_lottery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QRScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private Button btnCancel;

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startScanning();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result == null || result.getText() == null) return;

            // Return the scanned text to the caller
            Intent data = new Intent();
            data.putExtra("EVENT_ID", result.getText());
            setResult(RESULT_OK, data);

            barcodeView.pause();
            finish();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) { /* no-op */ }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcode_scanner);
        btnCancel = findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // Ask for camera permission if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void startScanning() {
        // Only QR codes (optional – can scan all types if you remove this line)
        // barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(Collections.singletonList(BarcodeFormat.QR_CODE)));

        barcodeView.decodeContinuous(callback);
        barcodeView.resume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        barcodeView.pause();
        super.onPause();
    }
}

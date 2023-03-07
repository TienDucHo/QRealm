package com.llamoss.qrealm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.CameraConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.llamoss.qrealm.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 0;
    private ActivityMainBinding binding;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private ExecutorService cameraExecutor;
    private final ArrayList<String> REQUESTED_PERMISSIONS = new ArrayList<String>();

    private final int REQUEST_CODE = 10;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate((getLayoutInflater()));
        setContentView(binding.getRoot());
        binding.imageCaptureButton.setOnClickListener(v -> takePhoto());
        binding.videoCaptureButton.setOnClickListener(v -> captureVideo());
        cameraExecutor = Executors.newSingleThreadExecutor();
        REQUESTED_PERMISSIONS.addAll((Arrays.asList(Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO)));
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
        {
            REQUESTED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (allPermissionGranted())
        {
            startCamera();
        }
        else {
            ActivityCompat.requestPermissions(this,
                    REQUESTED_PERMISSIONS.toArray(new String[0]), REQUEST_CODE);
        }
    }

    private void takePhoto() {}

    private void captureVideo() {}

    private boolean allPermissionGranted() {
        boolean flag = true;
        for (String permission: REQUESTED_PERMISSIONS)
        {
            flag = flag && (ContextCompat.checkSelfPermission(this,
                    permission) == PackageManager.PERMISSION_GRANTED);
        }
        return flag;
    }

    private void startCamera()
    {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder()
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
                cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                // Explain to the user that the feature is unavailable because
                // the feature requires a permission that the user has denied.
                // At the same time, respect the user's decision. Don't link to
                // system settings in an effort to convince the user to change
                // their decision.
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

}
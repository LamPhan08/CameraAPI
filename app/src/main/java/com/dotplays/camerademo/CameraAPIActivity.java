package com.dotplays.camerademo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class CameraAPIActivity extends AppCompatActivity {
    private Button btnCapture;
    private FrameLayout frameLayout;

    private Camera mCamera;

    private CameraPreview mPreview;

    private static int REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_api);

        btnCapture = findViewById(R.id.btnCapture);
        frameLayout = findViewById(R.id.container);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            mCamera = getCameraInstance();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE);
        }
        mCamera = getCameraInstance();

        mPreview = new CameraPreview(this, mCamera);

        frameLayout.addView(mPreview);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCameraHardware(CameraAPIActivity.this)) {
                    mCamera.takePicture(null, null, mPicture);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                mCamera = getCameraInstance();
            } else {
                Toast.makeText(this, "Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//    }

    // Ki???m tra thi???t b??? c?? camera kh??ng
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // Thi???t b??? c?? camera
            return true;
        } else {
            // Thi???t b??? kh??ng c?? camera
            return false;
        }
    }

    //M??? camera
    public Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open(); // Th??? m??? camera
            setCameraDisplayOrientation(CameraAPIActivity.this, Camera.CameraInfo.CAMERA_FACING_BACK, camera);
        } catch (Exception e) {
            // Camera kh??ng t???n t???i
            Log.e("mess", e.getMessage());
        }
        return camera; // tr??? v??? null n???u camera kh??ng t???n t???i
    }

    //Ch???p ???nh
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d("TAG", "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("TAG", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("TAG", "Error accessing file: " + e.getMessage());
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.R)
    private File getOutputMediaFile(int mediaTypeImage) {
        File file = new File(Environment.getStorageDirectory().getAbsolutePath());
        return file;

    }

    // T??nh to??n t??? l??? v?? chi???u hi???n th??? c???a Camera
    public static void setCameraDisplayOrientation(CameraAPIActivity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseCamera();
    }

    // Gi???i ph??ng Camera
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
package com.webbrains.timestampcamera.screens;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.webbrains.timestampcamera.R;
import com.webbrains.timestampcamera.utility.CameraPreview;
import com.webbrains.timestampcamera.utility.ImageCompressionAsyncTask;
import com.webbrains.timestampcamera.utility.Utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private Camera mCamera;
    private CameraPreview mPreview;
    FusedLocationProviderClient fusedLocationClient;
    LocationCallback mlocationCallback;
    LocationRequest locationRequest;
    Location currentLocation;
    String address1 = "";
    String address2 = "";
    TextView tvWatermark;
//    String currentLatLng = "";
    String currentDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        tvWatermark = findViewById(R.id.tvWatermark);
        preview.addView(mPreview);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(CameraActivity.this);

        mlocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e("locationResult", "null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.e("CONTINIOUSLOC: ", location.toString());
                    currentLocation = location;
                    getAddress();
                }
            }
        };

        getCurrentLocation();
    }

    public void getAddress() {
        if (currentLocation != null) {
            Geocoder geocoder;
            List<Address> addresses = new ArrayList<>();
            geocoder = new Geocoder(CameraActivity.this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (Exception e) {
                addresses = new ArrayList<>();
            }

            if (addresses.size() > 0) {
                Log.e("addresses", addresses + "");
//                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
//                String state = addresses.get(0).getAdminArea();
//                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();

                address1 = knownName;
                address2 = city + ", " + postalCode;

//                currentLatLng = currentLocation.getLatitude() + ", " + currentLocation.getLongitude();
                currentDate = Utility.getCurrentDate(new Date());

                tvWatermark.setText(address1 + "\n" + address2 + "\n" + currentDate);
//                tvWatermark.setText(currentLatLng + "\n" + address + "\n" + currentDate);

                fusedLocationClient.removeLocationUpdates(mlocationCallback);
            }
        }
    }

    void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 3);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 3) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    permissions[1].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (1 == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                getLocation();
            }
        }
    }

    public void getLocation() {
        locationRequest = LocationRequest.create()
                .setInterval(10)
                .setFastestInterval(10)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        LocationServices
                .getSettingsClient(CameraActivity.this)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(CameraActivity.this, (LocationSettingsResponse response) -> {
                    if (ActivityCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest, mlocationCallback, Looper.getMainLooper());
                })
                .addOnFailureListener(CameraActivity.this, ex -> {
                    if (ex instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) ex;
                            resolvable.startResolutionForResult(CameraActivity.this, 1);  // for activity
//                            startIntentSenderForResult(resolvable.getResolution().getIntentSender(), 1, null, 0, 0, 0, null); // for fragment
                        } catch (IntentSender.SendIntentException sendEx) {
                        }
                    }
                });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(mlocationCallback);
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    c = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return c; // returns null if camera is unavailable
    }

    public void takePhotoClick(View view) {
        try {
            mCamera.takePicture(null, null, mPicture);
        } catch (Exception e) {

        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = Utility.getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Log.e("photo", pictureFile.getPath());

                Log.e("photo file size", Integer.parseInt(String.valueOf(pictureFile.length() / 1024)) + "");

                ImageCompressionAsyncTask imageCompression = new ImageCompressionAsyncTask() {
                    @Override
                    protected void onPostExecute(File file) {
                        if (file != null) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    File tempFile = file;
                                    Log.e("photo compressed", tempFile.getPath());
                                    Log.e("photo com file size", Integer.parseInt(String.valueOf(tempFile.length() / 1024)) + "");

                                    Bitmap myBitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());

                                    int w = myBitmap.getWidth();
                                    int h = myBitmap.getHeight();
                                    Bitmap bitmapWithWatermark = Bitmap.createBitmap(w, h, myBitmap.getConfig());

                                    Canvas canvas = new Canvas(bitmapWithWatermark);
                                    canvas.drawBitmap(myBitmap, 0, 0, null);

                                    Paint paint = new Paint();
                                    paint.setColor(Color.BLACK);
                                    paint.setTextSize(16);
                                    paint.setAntiAlias(true);
//                                    canvas.drawText(currentLatLng, 16, myBitmap.getHeight() - 48, paint);
                                    canvas.drawText(address1, 16, myBitmap.getHeight() - 48, paint);
                                    canvas.drawText(address2, 16, myBitmap.getHeight() - 32, paint);
                                    canvas.drawText(currentDate, 16, myBitmap.getHeight() - 16, paint);

//                                    Bitmap bitmapWithWatermark = Utility.applyWaterMarkEffect(myBitmap, tvWatermark.getText().toString(), 0, myBitmap.getHeight() - 50, 15);
                                    tempFile.delete();
                                    tempFile = Utility.bitmapToFile(bitmapWithWatermark);

                                    Intent i = new Intent();
                                    i.putExtra("photo", tempFile.getPath());
                                    setResult(RESULT_OK, i);
                                    finish();
                                }
                            }, 1000);
                        } else {
                            Toast.makeText(CameraActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                imageCompression.execute(pictureFile.getPath());
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };
}
package com.rma.mycameraapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.rma.mycameraapp.R;
import com.rma.mycameraapp.databinding.ActivityDefaultCameraAppBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Map;

public class DefaultCameraAppActivity extends AppCompatActivity {
    private static final String TAG = "DefaultCameraAppActivity";
    private ActivityDefaultCameraAppBinding viewBinding;
    private String currentMediaPath;

    private static final List<String> requiredPermissionsList = new ArrayList<String>() {{
        add(Manifest.permission.CAMERA);
        add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }};
    private static final String[] REQUIRED_PERMISSIONS = requiredPermissionsList.toArray(new String[0]);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityDefaultCameraAppBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(viewBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!allPermissionsGranted())
            requestPermissions();

        viewBinding.defaultAppTakePhotoButton.setOnClickListener(v ->
                dispatchCameraIntent(MediaStore.ACTION_IMAGE_CAPTURE));
        viewBinding.defaultAppTakeVideoButton.setOnClickListener(v ->
                dispatchCameraIntent(MediaStore.ACTION_VIDEO_CAPTURE));
    }

    private void dispatchCameraIntent(String intentActionSpecifier) {
        Intent cameraIntent = new Intent(intentActionSpecifier);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {

            File mediaFile;
            Uri mediaURI;

            switch (intentActionSpecifier) {
                case(MediaStore.ACTION_IMAGE_CAPTURE):
                    mediaFile = createImageFile();
                    mediaURI = FileProvider.getUriForFile(this, "com.rma.mycameraapp.fileprovider", mediaFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mediaURI);
                    imageCaptureResultLauncher.launch(cameraIntent);
                    break;

                case(MediaStore.ACTION_VIDEO_CAPTURE):
                    mediaFile = createVideoFile();
                    mediaURI = FileProvider.getUriForFile(this, "com.rma.mycameraapp.fileprovider", mediaFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mediaURI);
                    videoCaptureResultLauncher.launch(cameraIntent);
                    break;

                default:
                    Log.d(TAG, "Error dispatching intent.");
                    finish();
                    break;
            }
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image;
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.d(TAG, "Error creating image file.");
            throw new RuntimeException(e);
        }

        currentMediaPath = image.getAbsolutePath();
        return image;
    }

    private File createVideoFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "MOV_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);

        File image;
        try {
            image = File.createTempFile(imageFileName, ".mp4", storageDir);
        } catch (IOException e) {
            Log.d(TAG, "Error creating video file.");
            throw new RuntimeException(e);
        }

        currentMediaPath = image.getAbsolutePath();
        return image;
    }

    private void deleteUnusedMediaFile() {
        File fileToDelete = new File(currentMediaPath);
        if (fileToDelete.exists()) {
            if (fileToDelete.delete())
                System.out.println("File deleted successfully: " + currentMediaPath);
            else
                System.out.println("Failed to delete file:" + currentMediaPath);
        }
    }

    ActivityResultLauncher<Intent> imageCaptureResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        viewBinding.videoPreview.setVisibility(View.GONE);
                        prepareDestinationView(viewBinding.imagePreview);
                        setImagePreview();
                    } else {
                        Log.d(TAG, "Image capture result code error.");
                        resetPreviewLayout();
                        deleteUnusedMediaFile();
                    }
                }
            });

    ActivityResultLauncher<Intent> videoCaptureResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        viewBinding.imagePreview.setVisibility(View.GONE);
                        prepareDestinationView(viewBinding.videoPreview);
                        setVideoPreview(Uri.fromFile(new File(currentMediaPath)));
                    } else {
                        Log.d(TAG, "Video capture result code error.");
                        resetPreviewLayout();
                        deleteUnusedMediaFile();
                    }
                }
            });

    private void setVideoPreview(Uri videoUri) {
        viewBinding.videoPreview.setVideoURI(videoUri);
        viewBinding.videoPreview.setMediaController(new MediaController(this));
        viewBinding.videoPreview.requestFocus();
        viewBinding.videoPreview.start();

        viewBinding.defaultAppTakeVideoButton.setText(R.string.take_new_video);
        viewBinding.defaultAppTakePhotoButton.setText(R.string.take_photo);

        Toast.makeText(this, "Video saved to " + currentMediaPath, Toast.LENGTH_LONG).show();
    }

    private void setImagePreview() {
        Glide.with(this).load(currentMediaPath).into(viewBinding.imagePreview);

        viewBinding.defaultAppTakePhotoButton.setText(R.string.take_new_photo);
        viewBinding.defaultAppTakeVideoButton.setText(R.string.take_video);

        Toast.makeText(this, "Image saved to " + currentMediaPath, Toast.LENGTH_LONG).show();
    }

    private void prepareDestinationView(View view) {
        if(view.getVisibility() != View.VISIBLE){
            view.setVisibility(View.VISIBLE);

            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    private void resetPreviewLayout() {
        viewBinding.imagePreview.setVisibility(View.GONE);
        viewBinding.videoPreview.setVisibility(View.GONE);
        viewBinding.defaultAppTakePhotoButton.setText(R.string.take_photo);
        viewBinding.defaultAppTakeVideoButton.setText(R.string.take_video);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS);
    }

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean permissionGranted = true;
                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                    if (Arrays.asList(REQUIRED_PERMISSIONS).contains(entry.getKey()) && !entry.getValue()) {
                        permissionGranted = false;
                        break;
                    }
                }
                if (!permissionGranted) {
                    finish();
                }
            });
}
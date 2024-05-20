package com.rma.mycameraapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rma.mycameraapp.databinding.ActivityMainBinding;
import com.rma.mycameraapp.gallery.GalleryActivity;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewBinding.defaultCamAppButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DefaultCameraAppActivity.class);
            startActivity(intent);
        });

        viewBinding.imageGalleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GalleryActivity.class);
            intent.putExtra(GalleryActivity.GALLERY_TYPE_KEY, GalleryActivity.TYPE_IMAGE_GALLERY);
            startActivity(intent);
        });

        viewBinding.videoGalleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GalleryActivity.class);
            intent.putExtra(GalleryActivity.GALLERY_TYPE_KEY, GalleryActivity.TYPE_VIDEO_GALLERY);
            startActivity(intent);
        });
    }
}
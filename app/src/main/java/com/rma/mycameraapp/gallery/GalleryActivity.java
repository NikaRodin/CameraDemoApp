package com.rma.mycameraapp.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rma.mycameraapp.databinding.ActivityGalleryBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    public static String GALLERY_TYPE_KEY = "gallery type key";
    public static final int TYPE_IMAGE_GALLERY = 1;
    public static final int TYPE_VIDEO_GALLERY = 2;
    private static final String TAG = "GalleryActivity";
    private RecyclerView.Adapter adapter;
    private int galleryType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityGalleryBinding viewBinding = ActivityGalleryBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(viewBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        if (intent != null)
            galleryType = intent.getIntExtra(GALLERY_TYPE_KEY, 0);
        else {
            Log.d(TAG, "You must provide a gallery type.");
            finish();
        }

        List<String> filePaths = getFilePaths();

        if (!filePaths.isEmpty())
            viewBinding.noFilesYetTextView.setVisibility(View.GONE);

        int numColumns = 0;
        switch (galleryType) {
            case(TYPE_IMAGE_GALLERY):
                numColumns = 3;
                adapter = new ImageGalleryAdapter(this, filePaths);
                break;
            case(TYPE_VIDEO_GALLERY):
                numColumns = 1;
                adapter = new VideoGalleryAdapter(this, filePaths);
                break;
            default:
                Log.d(TAG, "Unsupported gallery type. Couldn't define column size.");
                finish();
                break;
        }

        RecyclerView recyclerView = viewBinding.recyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numColumns));
        recyclerView.setAdapter(adapter);
    }

    private List<String> getFilePaths() {
        List<String> paths = new ArrayList<>();
        File directory = null;

        switch (galleryType) {
            case(TYPE_IMAGE_GALLERY):
                directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                break;
            case(TYPE_VIDEO_GALLERY):
                directory = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                break;
            default:
                Log.d(TAG, "Unsupported gallery type. Couldn't load file paths.");
                finish();
                break;
        }

        if (directory != null) {
            File[] files = directory.listFiles();
            if (files != null) {
                Arrays.sort(files, (f1, f2) -> f2.getName().compareTo(f1.getName()));
                for (File file : files) {
                    paths.add(file.getAbsolutePath());
                }
            }
        }
        return paths;
    }
}
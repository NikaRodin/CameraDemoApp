package com.rma.mycameraapp.gallery;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.rma.mycameraapp.R;

import java.io.File;
import java.util.List;

public class VideoGalleryAdapter extends RecyclerView.Adapter<VideoGalleryAdapter.ViewHolder> {
    private final Context context;
    private final List<String> videoPaths;

    public VideoGalleryAdapter(Context context, List<String> videoPaths) {
        this.context = context;
        this.videoPaths = videoPaths;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File videoFile = new File(videoPaths.get(position));
        Uri videoUri = FileProvider.getUriForFile(context, "com.rma.mycameraapp.fileprovider", videoFile);
        VideoView videoView = holder.frameLayout.findViewById(R.id.video_view);

        videoView.setVideoURI(videoUri);
        videoView.setMediaController(new MediaController(context));
        videoView.seekTo(1);
    }

    @Override
    public int getItemCount() {
        return videoPaths.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout frameLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            frameLayout = itemView.findViewById(R.id.frame_layout);
        }
    }
}

package com.app.autismplay.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.autismplay.R;
import com.app.autismplay.models.Video;
import com.app.autismplay.responseyoutube.Item;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {

    private Context context;
    private List<Item> videos = new ArrayList<>();

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_video,parent,false);
        return new VideoAdapter.MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        Item video = videos.get(position);
        if(video.snippet!=null){
            if(video.snippet.title!=null) holder.titulo.setText(video.snippet.title);
            if(video.snippet.thumbnails.high.url!=null){
                String url = video.snippet.thumbnails.high.url;
                Picasso.get().load(url).into(holder.cover);
            }
        }



    }
    @Override
    public int getItemCount() {
        return videos.size();
    }


    public VideoAdapter(List<Item> videos, Context context) {
        this.videos = videos;
        this.context = context;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView titulo,data;
        private ImageView cover;



        public MyViewHolder(View itemView){
            super(itemView);
            titulo = itemView.findViewById(R.id.txtTitleVideoAdapter);
            cover = itemView.findViewById(R.id.imageCoverAdaper);

        }
    }


}

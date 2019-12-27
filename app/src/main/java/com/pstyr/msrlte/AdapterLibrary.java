package com.pstyr.msrlte;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdapterLibrary extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Object> listOffline;
    ArrayList<Integer> count = new ArrayList<>();

    public AdapterLibrary(Context context, List<Object> listOffline) {
        this.context = context;
        this.listOffline = listOffline;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView songTitle;
        LinearLayout player;
        RelativeLayout listArea;

        public ViewHolder(View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.trackTitleOffline);
            listArea = itemView.findViewById(R.id.listArea);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder offlineViewHolder = (ViewHolder)holder;
        final TrackLibrary trackoff = (TrackLibrary)listOffline.get(position);

        offlineViewHolder.songTitle.setText(trackoff.getSongTitle());

        for (int i = 0; i < listOffline.size(); i++) {
            count.add(0);
        }
        offlineViewHolder.listArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PlayLibrary.class);
                i.putExtra("songtitle", trackoff.getSongTitle());
                i.putExtra("songurl", trackoff.getSongUrl());
                i.putExtra("source", "offline");
                context.startActivity(i);

            }
        });
    }
    @Override
    public int getItemCount() {
        return listOffline.size();
    }
}

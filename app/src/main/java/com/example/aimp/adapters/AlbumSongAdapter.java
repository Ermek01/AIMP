package com.example.aimp.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aimp.R;
import com.example.aimp.models.Song;

import java.util.List;

public class AlbumSongAdapter extends RecyclerView.Adapter<AlbumSongAdapter.ASVH> {

    private Activity context;
    private List<Song> albumSongList;

    public AlbumSongAdapter(Activity context, List<Song> albumSongList) {
        this.context = context;
        this.albumSongList = albumSongList;
    }

    @NonNull
    @Override
    public ASVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ASVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.album_list_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ASVH holder, int position) {

        Song song = albumSongList.get(position);

        if (song != null){

            holder.atv.setText(song.title);
            holder.dtv.setText(song.artistName);
            int trackN = song.trackNumber;
            if (trackN == 0){
                holder.ntv.setText("-");
            }
            else {
                holder.ntv.setText(String.valueOf(trackN));
            }
        }
    }

    @Override
    public int getItemCount() {
        return albumSongList != null?albumSongList.size():0;
    }

    public class ASVH extends RecyclerView.ViewHolder {

        private TextView atv, ntv, dtv;

        public ASVH(@NonNull View itemView) {
            super(itemView);

            atv = itemView.findViewById(R.id.songTitle);
            ntv = itemView.findViewById(R.id.number);
            dtv = itemView.findViewById(R.id.detail);
        }
    }
}

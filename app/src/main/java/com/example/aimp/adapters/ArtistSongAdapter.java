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

public class ArtistSongAdapter extends RecyclerView.Adapter<ArtistSongAdapter.VH> {

    private Activity context;
    private List<Song> artistSongList;

    public ArtistSongAdapter(Activity context, List<Song> artistSongList) {
        this.context = context;
        this.artistSongList = artistSongList;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.album_list_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        Song song = artistSongList.get(position);

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
        return artistSongList != null?artistSongList.size():0;
    }

    public class VH extends RecyclerView.ViewHolder {

        private TextView atv, ntv, dtv;


        public VH(@NonNull View itemView) {
            super(itemView);

            atv = itemView.findViewById(R.id.songTitle);
            ntv = itemView.findViewById(R.id.number);
            dtv = itemView.findViewById(R.id.detail);
        }
    }
}

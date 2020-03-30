package com.example.aimp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aimp.R;
import com.example.aimp.fragments.AlbumDetailsFragment;
import com.example.aimp.fragments.ArtistDetailsFragment;
import com.example.aimp.models.Album;
import com.example.aimp.models.Artist;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import static com.example.aimp.adapters.SongAdapter.getImage;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ARV> {

    private Context context;
    private List<Artist> artistList;

    public ArtistAdapter(Context context, List<Artist> artistList) {
        this.context = context;
        this.artistList = artistList;
    }

    @NonNull
    @Override
    public ARV onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ARV(LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ARV holder, int position) {

        Artist artist = artistList.get(position);

        if (artist != null){
            holder.artistName.setText(artist.artName);
            ImageLoader.getInstance().displayImage(getImage(artist.id).toString(), holder.artimg,
                    new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.note)
                            .resetViewBeforeLoading(true).build());
        }

    }

    @Override
    public int getItemCount() {
        return artistList != null?artistList.size():0;
    }

    public class ARV extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView artimg;
        private TextView artistName;

        public ARV(@NonNull View itemView) {
            super(itemView);

            artimg = itemView.findViewById(R.id.artthum);
            artistName = itemView.findViewById(R.id.artname);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            long artistId = artistList.get(getAdapterPosition()).id;

            FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment;

            transaction.setCustomAnimations(R.anim.layout_fad_in, R.anim.layout_fad_out,
                    R.anim.layout_fad_in, R.anim.layout_fad_out);

            fragment = ArtistDetailsFragment.newInstance(artistId);

            transaction.hide(((AppCompatActivity)context).getSupportFragmentManager().findFragmentById(R.id.main_container));

            transaction.add(R.id.main_container, fragment);
            transaction.addToBackStack(null).commit();
        }
    }
}

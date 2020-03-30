package com.example.aimp.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.aimp.R;
import com.example.aimp.adapters.AlbumSongAdapter;
import com.example.aimp.adapters.ArtistSongAdapter;
import com.example.aimp.dataloder.AlbumLoder;
import com.example.aimp.dataloder.AlbumSongLoder;
import com.example.aimp.dataloder.ArtistLoder;
import com.example.aimp.dataloder.ArtistSongLoder;
import com.example.aimp.models.Album;
import com.example.aimp.models.Artist;
import com.example.aimp.models.Song;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import static com.example.aimp.adapters.SongAdapter.getImage;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistDetailsFragment extends Fragment {

    CollapsingToolbarLayout collapsingToolbarLayout;
    private long artistId;

    private List<Song> songList = new ArrayList<>();
    private Artist artist;

    private ImageView imageView;
    private ImageView aaimg;
    private TextView anaam, ade;
    private RecyclerView recyclerView;
    private AlbumSongAdapter adapter;

    public static ArtistDetailsFragment newInstance(long artist_id) {

        Bundle args = new Bundle();
        args.putLong("_ID", artist_id);
        ArtistDetailsFragment fragment = new ArtistDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        artistId = getArguments().getLong("_ID");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_artist_details, container, false);
        aaimg = rootView.findViewById(R.id.artistimg);
        imageView = rootView.findViewById(R.id.bigartist);
        anaam = rootView.findViewById(R.id.artistnaam);
        ade = rootView.findViewById(R.id.artistDetails);
        collapsingToolbarLayout = rootView.findViewById(R.id.artistcollapsinglayout);
        recyclerView = rootView.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        artist = ArtistLoder.getArtist(getActivity(), artistId);
        setDetails();
        setupAlbumlist();
        return rootView;
    }

    private void setupAlbumlist() {

        songList = ArtistSongLoder.getAllArtistSongs(getActivity(), artistId);
        adapter = new AlbumSongAdapter(getActivity(), songList);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    private void setDetails() {

        collapsingToolbarLayout.setTitle(artist.artName);
        anaam.setText(artist.artName);
        ade.setText("песен: " + artist.songCont);
        ImageLoader.getInstance().displayImage(getImage(artist.id).toString(), imageView,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.note)
                        .resetViewBeforeLoading(true).build());

        ImageLoader.getInstance().displayImage(getImage(artist.id).toString(), aaimg,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.note)
                        .resetViewBeforeLoading(true).build());
    }
}

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
import com.example.aimp.dataloder.AlbumLoder;
import com.example.aimp.dataloder.AlbumSongLoder;
import com.example.aimp.models.Album;
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
public class AlbumDetailsFragment extends Fragment {

    CollapsingToolbarLayout collapsingToolbarLayout;
    private long album_id;

    private List<Song> songList = new ArrayList<>();
    private Album album;

    private ImageView imageView;
    private ImageView aaimg;
    private TextView anaam, ade;
    private RecyclerView recyclerView;
    private AlbumSongAdapter adapter;

    public static AlbumDetailsFragment newInstance(long id) {

        Bundle args = new Bundle();
        args.putLong("_ID", id);
        AlbumDetailsFragment fragment = new AlbumDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        album_id = getArguments().getLong("_ID");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_album_details, container, false);
        aaimg = rootView.findViewById(R.id.aaimg);
        imageView = rootView.findViewById(R.id.bigart);
        anaam = rootView.findViewById(R.id.artnaam);
        ade = rootView.findViewById(R.id.albumDetails);
        collapsingToolbarLayout = rootView.findViewById(R.id.collapsinglayout);
        recyclerView = rootView.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        album = new AlbumLoder().getAlbum(getActivity(), album_id);
        setDetails();
        setupAlbumlist();
        return rootView;
    }

    private void setupAlbumlist() {

        songList = AlbumSongLoder.getAllAlbumSongs(getActivity(), album_id);
        adapter = new AlbumSongAdapter(getActivity(), songList);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    private void setDetails() {

        collapsingToolbarLayout.setTitle(album.albumName);
        anaam.setText(album.albumName);
        ade.setText(album.artistName + " " + album.year + " song: " + album.numSong);
        ImageLoader.getInstance().displayImage(getImage(album.id).toString(), imageView,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.note)
                        .resetViewBeforeLoading(true).build());

        ImageLoader.getInstance().displayImage(getImage(album.id).toString(), aaimg,
                new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.note)
                        .resetViewBeforeLoading(true).build());
    }
}

package com.example.aimp.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aimp.R;
import com.example.aimp.adapters.ArtistAdapter;
import com.example.aimp.adapters.GridSpacingDecoration;
import com.example.aimp.dataloder.ArtistLoder;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArtistAdapter adapter;
    int spanCount = 2;
    int spacing = 20;
    boolean includeEdge = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_artist, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.arr);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));

        new LoadData().execute("");
        return view;
    }

    public class LoadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            if (getActivity() != null) {
                adapter = new ArtistAdapter(getActivity(), new ArtistLoder().artistList(getActivity()));
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String s) {
            recyclerView.setAdapter(adapter);
            if (getActivity() != null){
                recyclerView.addItemDecoration(new GridSpacingDecoration(spanCount, spacing, includeEdge));
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}

package com.example.aimp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.aimp.R;


public class ControllerFragment extends Fragment {

    public static View top_container;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_controller, container, false);

        top_container = view.findViewById(R.id.top_container);
        return view;
    }
}

package com.example.qiblatfinder2;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class CustomFragment extends Fragment {
    private CardView piramid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootview = inflater.inflate(R.layout.fragment_custom, container, false);
        piramid = rootview.findViewById(R.id.piramid);
        return rootview;
    }
}
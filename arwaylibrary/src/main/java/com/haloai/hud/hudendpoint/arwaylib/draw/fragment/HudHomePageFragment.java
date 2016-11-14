package com.haloai.hud.hudendpoint.arwaylib.draw.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.haloai.hud.hudendpoint.arwaylib.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HudHomePageFragment extends Fragment {


    public HudHomePageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hud_home_page, container, false);
    }

}

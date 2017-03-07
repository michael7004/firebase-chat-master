package com.crazyhitty.chdev.ks.firebasechatModule.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crazyhitty.chdev.ks.firebasechatModule.R;

/**
 * Created by indianic on 16/02/17.
 */

public class CustomCameraFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_custom_camera, container, false);
        intiView(view);
        return view;
    }

    /**
     * Binding views
     */
    private void intiView(View view) {
    }
}

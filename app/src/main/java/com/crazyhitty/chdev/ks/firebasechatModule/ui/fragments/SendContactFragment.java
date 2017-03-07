package com.crazyhitty.chdev.ks.firebasechatModule.ui.fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crazyhitty.chdev.ks.firebasechatModule.R;
import com.crazyhitty.chdev.ks.firebasechatModule.utils.Constants;
import com.crazyhitty.chdev.ks.firebasechatModule.utils.ContactUtil;

import java.io.InputStream;

import static com.crazyhitty.chdev.ks.firebasechatModule.R.id.fragment_send_contact_tv_email_id;

/**
 * Created by indianic on 14/02/17.
 */

public class SendContactFragment extends Fragment {

    private ImageView ivSend;
    private ImageView ivProfile;
    private ImageView ivEmailBox;
    private TextView tvEmail;
    private TextView tvEmailBox;
    private TextView tvEmailid;
    Uri uriContact;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle bundle = getArguments();
        if (bundle != null) {
            try {
                String uri = bundle.getString("URI_KEY");
                uriContact = Uri.parse(uri);
            } catch (Exception e) {
                e.getMessage();
            }


        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_contact, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Send Contact");
        getActivity().invalidateOptionsMenu();
        setHasOptionsMenu(true);
        bindViews(view);
        view.setFocusableInTouchMode(true);
        view.requestFocus();

        // handle back press in fragment explicitly
        view.setOnKeyListener(new View.OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // hide it first then back to fragment
                    if (getFragmentManager().getBackStackEntryCount() > 0) {
                        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    } else {
                        getActivity().onBackPressed();
                    }
                }
                return true;
            }
        });
        return view;
    }

    private void bindViews(View view) {
        tvEmail = (TextView) view.findViewById(fragment_send_contact_tv_email_id);
        tvEmailBox = (TextView) view.findViewById(R.id.fragment_send_contact_tv_email_box);
        ivSend = (ImageView) view.findViewById(R.id.fragment_send_contact_iv_profile);
        ivProfile = (ImageView) view.findViewById(R.id.fragment_send_contact_iv_email_box);
        ivEmailBox = (ImageView) view.findViewById(R.id.fragment_send_contact_iv_send);
        String contactID = ContactUtil.getContatctID(getActivity(), uriContact);
        String name = ContactUtil.getContatctName(getActivity(), uriContact);
        String emailID = ContactUtil.getNameEmailDetails(getActivity(), uriContact);
        Log.d("email", emailID);
        tvEmail.setText(name);
        String tvEmailBoxStr = ContactUtil.getContatctNumber(getActivity(), contactID);
        tvEmailBox.setText(tvEmailBoxStr);
        final InputStream inputStream = ContactUtil.getPhoto(getActivity(), Long.parseLong(contactID));
        if (inputStream != null) {
            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
            ivProfile.setImageBitmap(bmp);
            ivSend.setImageBitmap(bmp);
        }
        ContactUtil.getVCFFile(getActivity(),name,emailID,tvEmailBoxStr);


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (item.getItemId() == android.R.id.home) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                getActivity().onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getActivity().getIntent().getExtras().getString(Constants.ARG_RECEIVER));
    }


}

package com.crazyhitty.chdev.ks.firebasechatModule.ui.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crazyhitty.chdev.ks.firebasechatModule.R;
import com.crazyhitty.chdev.ks.firebasechatModule.core.chat.ChatContract;
import com.crazyhitty.chdev.ks.firebasechatModule.core.chat.ChatPresenter;
import com.crazyhitty.chdev.ks.firebasechatModule.events.PushNotificationEvent;
import com.crazyhitty.chdev.ks.firebasechatModule.models.Chat;
import com.crazyhitty.chdev.ks.firebasechatModule.models.SharePostModel;
import com.crazyhitty.chdev.ks.firebasechatModule.ui.adapters.ChatRecyclerAdapter;
import com.crazyhitty.chdev.ks.firebasechatModule.utils.Constants;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Author: Kartik Sharma
 * Created on: 8/28/2016 , 10:36 AM
 * Project: FirebaseChat
 */

public class ChatFragment extends Fragment implements ChatContract.View, TextView.OnEditorActionListener, View.OnClickListener {

    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;


    //Option menu like whats app
    private View theMenu;
    private LinearLayout menu1;
    private LinearLayout menu2;
    private LinearLayout menu3;
    private LinearLayout menu4;
    private LinearLayout menu5;
    private LinearLayout menu6;
    private View overlay;
    private ImageView imLebanon;
    private ImageView imCamera;
    private ImageView imGallery;
    private ImageView imFrance;
    private ImageView imContact;
    private ImageView imAudio;
    private Context context;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseAuth firebaseAuth;
    private Uri uri;
    private String contactID;
    private boolean menuOpen = false;
    private RecyclerView mRecyclerViewChat;
    private EditText mETxtMessage;
    private ImageButton btnSend;

    private ProgressDialog mProgressDialog;

    private ChatRecyclerAdapter mChatRecyclerAdapter;

    private ChatPresenter mChatPresenter;
    private static String userName;

    public static ChatFragment newInstance(String receiver,
                                           String receiverUid,
                                           String firebaseToken) {
        userName = receiver;
        Bundle args = new Bundle();
        args.putString(Constants.ARG_RECEIVER, receiver);
        args.putString(Constants.ARG_RECEIVER_UID, receiverUid);
        args.putString(Constants.ARG_FIREBASE_TOKEN, firebaseToken);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_chat, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        bindViews(fragmentView);
        firebaseAuth = FirebaseAuth.getInstance();
        getActivity().invalidateOptionsMenu();
        setHasOptionsMenu(true);
        //==== CODE FOR MANAGE BACK PRESS ====
        fragmentView.setFocusableInTouchMode(true);
        fragmentView.requestFocus();

        // handle back press in fragment explicitly
        fragmentView.setOnKeyListener(new View.OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // hide it first then back to fragment
                    if (menuOpen) {


                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            // Do something for lollipop and above versions
                            hideMenu();
                        } else {
                            // do something for phones running an SDK before lollipop
                            hideMenuPreLoloPop();
                        }

                    } else {

                        getActivity().onBackPressed();


                    }


//                    if (getFragmentManager().getBackStackEntryCount() > 0) {
////                        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//
//                    }
//                    // show footer in MainActivity
//                    ((MainActivity) getActivity()).showFooter();
                }
                return true;
            }
        });


        return fragmentView;
    }

    private void bindViews(View view) {

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://config-cbd51.appspot.com");
        theMenu = view.findViewById(R.id.the_menu);
        menu1 = (LinearLayout) view.findViewById(R.id.menu1);
        menu2 = (LinearLayout) view.findViewById(R.id.menu2);
        menu3 = (LinearLayout) view.findViewById(R.id.menu3);
        menu4 = (LinearLayout) view.findViewById(R.id.menu4);
        menu5 = (LinearLayout) view.findViewById(R.id.menu5);
        menu6 = (LinearLayout) view.findViewById(R.id.menu6);
        imLebanon = (ImageView) view.findViewById(R.id.fragment_chat_iv_lebanon);
        imCamera = (ImageView) view.findViewById(R.id.fragment_chat_iv_camera);
        imGallery = (ImageView) view.findViewById(R.id.fragment_chat_iv_gallery);
        imFrance = (ImageView) view.findViewById(R.id.fragment_chat_iv_france);
        imContact = (ImageView) view.findViewById(R.id.fragment_chat_iv_contatact);
        imAudio = (ImageView) view.findViewById(R.id.fragment_chat_iv_audio);
        overlay = view.findViewById(R.id.overlay);
        mRecyclerViewChat = (RecyclerView) view.findViewById(R.id.recycler_view_chat);
        mETxtMessage = (EditText) view.findViewById(R.id.edit_text_message);
        btnSend = (ImageButton) view.findViewById(R.id.fragment_chat_btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendMessage();

//                uploadDocument(uri);
                uploadImage(uri);
            }
        });

        imLebanon.setOnClickListener(this);
        imCamera.setOnClickListener(this);
        imGallery.setOnClickListener(this);
        imFrance.setOnClickListener(this);
        imContact.setOnClickListener(this);
        imAudio.setOnClickListener(this);

    }


    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat_options, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (item.getItemId() == android.R.id.home) {

            if (!menuOpen) {
                getActivity().onBackPressed();
            } else {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    // Do something for lollipop and above versions
                    hideMenu();
                } else {
                    // do something for phones running an SDK before lollipop
                    hideMenuPreLoloPop();
                }
            }


        }


        if (id == R.id.menu_chat_option_edit) {

            Toast.makeText(getActivity(), "click", Toast.LENGTH_SHORT).show();

        }

        if (id == R.id.menu_chat_options) {


            // Do something for lollipop and above versions
            if (!menuOpen) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    // Do something for lollipop and above versions
                    revealMenu();
                } else {
                    // do something for phones running an SDK before lollipop
                    revealMenuPreLoliPop();
                }


            } else {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    // Do something for lollipop and above versions
                    hideMenu();
                } else {
                    // do something for phones running an SDK before lollipop
                    hideMenuPreLoloPop();
                }

            }
        }


        return super.onOptionsItemSelected(item);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void revealMenu() {
        menuOpen = true;

        theMenu.setVisibility(View.INVISIBLE);
        int cx = theMenu.getRight() - 200;
        int cy = theMenu.getTop();
        int finalRadius = Math.max(theMenu.getWidth(), theMenu.getHeight());
        Animator anim =
                ViewAnimationUtils.createCircularReveal(theMenu, cx, cy, 0, finalRadius);
        anim.setDuration(600);
        theMenu.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.VISIBLE);
        anim.start();


        // Animate The Icons One after the other, I really would like to know if there is any
        // simpler way to do it
        Animation popeup1 = AnimationUtils.loadAnimation(getActivity(), R.anim.popeup);
        Animation popeup2 = AnimationUtils.loadAnimation(getActivity(), R.anim.popeup);
        Animation popeup3 = AnimationUtils.loadAnimation(getActivity(), R.anim.popeup);
        Animation popeup4 = AnimationUtils.loadAnimation(getActivity(), R.anim.popeup);
        Animation popeup5 = AnimationUtils.loadAnimation(getActivity(), R.anim.popeup);
        Animation popeup6 = AnimationUtils.loadAnimation(getActivity(), R.anim.popeup);
        popeup1.setStartOffset(50);
        popeup2.setStartOffset(100);
        popeup3.setStartOffset(150);
        popeup4.setStartOffset(200);
        popeup5.setStartOffset(250);
        popeup6.setStartOffset(300);
        menu1.startAnimation(popeup1);
        menu2.startAnimation(popeup2);
        menu3.startAnimation(popeup3);
        menu4.startAnimation(popeup4);
        menu5.startAnimation(popeup5);
        menu6.startAnimation(popeup6);

    }

    public void revealMenuPreLoliPop() {
        menuOpen = true;

        theMenu.setVisibility(View.INVISIBLE);
        int cx = theMenu.getRight() - 200;
        int cy = theMenu.getTop();
        int finalRadius = Math.max(theMenu.getWidth(), theMenu.getHeight());
        theMenu.setVisibility(View.VISIBLE);
        overlay.setVisibility(View.VISIBLE);


    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void hideMenu() {
        menuOpen = false;
        int cx = theMenu.getRight() - 200;
        int cy = theMenu.getTop();
        int initialRadius = theMenu.getWidth();
        Animator anim = ViewAnimationUtils.createCircularReveal(theMenu, cx, cy, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                theMenu.setVisibility(View.INVISIBLE);
                theMenu.setVisibility(View.GONE);
                overlay.setVisibility(View.INVISIBLE);
                overlay.setVisibility(View.GONE);
            }
        });
        anim.start();
    }

    public void hideMenuPreLoloPop() {
        menuOpen = false;
        int cx = theMenu.getRight() - 200;
        int cy = theMenu.getTop();
        int initialRadius = theMenu.getWidth();

        theMenu.setVisibility(View.INVISIBLE);
        theMenu.setVisibility(View.GONE);
        overlay.setVisibility(View.INVISIBLE);
        overlay.setVisibility(View.GONE);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void overlayClick(View v) {
        hideMenu();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        permissionStatus = getActivity().getSharedPreferences("permissionStatus", getActivity().MODE_PRIVATE);
        init();
    }

    private void init() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(getString(R.string.loading));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);

        mETxtMessage.setOnEditorActionListener(this);
//        btnSend.setOnClickListener(this);

        mChatPresenter = new ChatPresenter(this);
        mChatPresenter.getMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                getArguments().getString(Constants.ARG_RECEIVER_UID));
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendMessage();
            return true;
        }
        return false;
    }

    private void sendMessage() {
        String message = mETxtMessage.getText().toString();
        String receiver = getArguments().getString(Constants.ARG_RECEIVER);
        String receiverUid = getArguments().getString(Constants.ARG_RECEIVER_UID);
        String sender = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String receiverFirebaseToken = getArguments().getString(Constants.ARG_FIREBASE_TOKEN);
        Chat chat = new Chat(sender,
                receiver,
                senderUid,
                receiverUid,
                message,
                System.currentTimeMillis());
        mChatPresenter.sendMessage(getActivity().getApplicationContext(),
                chat,
                receiverFirebaseToken);
    }

    @Override
    public void onSendMessageSuccess() {
        mETxtMessage.setText("");
        Toast.makeText(getActivity(), "Message sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendMessageFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetMessagesSuccess(Chat chat) {
        if (mChatRecyclerAdapter == null) {
            mChatRecyclerAdapter = new ChatRecyclerAdapter(new ArrayList<Chat>());
            mRecyclerViewChat.setAdapter(mChatRecyclerAdapter);
        }
        mChatRecyclerAdapter.add(chat);
        mRecyclerViewChat.smoothScrollToPosition(mChatRecyclerAdapter.getItemCount() - 1);
    }

    @Override
    public void onGetMessagesFailure(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onPushNotificationEvent(PushNotificationEvent pushNotificationEvent) {
        if (mChatRecyclerAdapter == null || mChatRecyclerAdapter.getItemCount() == 0) {
            mChatPresenter.getMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    pushNotificationEvent.getUid());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fragment_chat_iv_lebanon) {
            if (menuOpen) {
                /**
                 * Below code for native intetn picker or you can use third party library
                 */
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf|text/*");
//                intent.setType("image/*|application/pdf|audio/*");
//                intent.setType("application/text");//pdf
//                intent.setType("text/xml");
                startActivityForResult(intent, Constants.PICKFILE_REQUEST_CODE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    hideMenu();
                } else {
                    hideMenuPreLoloPop();
                }


            }
        } else if (v.getId() == R.id.fragment_chat_iv_camera) {


        } else if (v.getId() == R.id.fragment_chat_iv_gallery) {
            if (menuOpen) {
                // select a file
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    hideMenu();
                } else {
                    hideMenuPreLoloPop();
                }

                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), Constants.PICKGALLERY_REQUEST_CODE);
            }

        } else if (v.getId() == R.id.fragment_chat_iv_france) {
            Log.d("menu", "three");
            Toast.makeText(getActivity(), "france", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.fragment_chat_iv_contatact) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                hideMenu();
            } else {
                hideMenuPreLoloPop();
            }

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_CONTACTS)) {
                    //Show Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Need Storage Permission");
                    builder.setMessage("This app needs phone permission.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, Constants.PERMISSION_CALLBACK_CONSTANT);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else if (permissionStatus.getBoolean(Manifest.permission.READ_CONTACTS, false)) {
                    //Previously Permission Request was cancelled with 'Dont Ask Again',
                    // Redirect to Settings after showing Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Need Storage Permission");
                    builder.setMessage("This app needs storage permission.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            sentToSettings = true;
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, Constants.REQUEST_PERMISSION_SETTING);
                            Toast.makeText(getActivity(), "Go to Permissions to Grant Phone", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    //just request the permission
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, Constants.PERMISSION_CALLBACK_CONSTANT);
                }
//                Toast.makeText(context, "Permissions Required", Toast.LENGTH_SHORT).show();


                SharedPreferences.Editor editor = permissionStatus.edit();
                editor.putBoolean(Manifest.permission.READ_CONTACTS, true);
                editor.commit();
            } else {
                //You already have the permission, just go ahead.
                proceedAfterPermission();
            }


        } else if (v.getId() == R.id.fragment_chat_iv_audio) {
            if (menuOpen) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivityForResult(intent, Constants.PICKAUDIO_REQUEST_CODE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    hideMenu();
                } else {
                    hideMenuPreLoloPop();
                }

            }


        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                uri = data.getData();
                mETxtMessage.setText(uri.toString());
            }
        } else if (requestCode == Constants.PICKAUDIO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (data != null) {
                uri = data.getData();
                mETxtMessage.setText(uri.toString());

            }
        } else if (requestCode == Constants.PICKGALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (data != null) {
                uri = data.getData();

//                final File file = new File(Environment.getExternalStorageDirectory(), data.getData().toString());
//                uri= Uri.fromFile(file);
//                File auxFile = new File(uri.toString());
//                assertEquals(file.getAbsolutePath(), auxFile.getAbsolutePath());
                mETxtMessage.setText(uri.toString());


            }
        } else if (requestCode == Constants.PICKCONTACT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (data != null) {
                uri = data.getData();
                final SendContactFragment sendContactFragment = new SendContactFragment();
                final Bundle bundle = new Bundle();
                bundle.putString("URI_KEY", String.valueOf(uri));
                sendContactFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().add(R.id.frame_layout_content_chat, sendContactFragment, SendContactFragment.class.getSimpleName()).addToBackStack(SendContactFragment.class.getSimpleName()).hide(this).commit();


            }
        } else if (requestCode == Constants.REQUEST_PERMISSION_SETTING) {

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission();

            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {
                proceedAfterPermission();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_CONTACTS)) {

                Toast.makeText(context, "Permissions Required", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs phone permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, Constants.PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getActivity(), "Unable to get Permission", Toast.LENGTH_LONG).show();
            }
        }
    }


    //upload image to firebase storage
    private void uploadImage(final Uri filePath) {
        if (filePath != null) {
            //displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            final StorageReference riversRef = storageReference.child("images/" + timeStamp() + ".jpg");
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();
                            //save data into database
//                            storeShareData(taskSnapshot.getDownloadUrl());
                            //and displaying a success toast
                            Toast.makeText(getActivity(), "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("error", exception.getMessage());
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " +
                                    "" + ((int) progress) + "%...");
                        }
                    });

        }
        //if there is not any file
        else {
            //you can display an error toast

        }
    }

    private void uploadDocument(final Uri filePath) {
        if (filePath != null) {
            //displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            final StorageReference riversRef = storageReference.child("Document/" + timeStamp() + ".pdf");
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();
                            //save data into database
//                            storeShareData(taskSnapshot.getDownloadUrl());
                            //and displaying a success toast
                            Toast.makeText(getActivity(), "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("error", exception.getMessage());
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " +
                                    "" + ((int) progress) + "%...");
                        }
                    });

        }
        //if there is not any file
        else {
            //you can display an error toast

        }
    }

    //save shared data into firebase database
    private void storeShareData(final Uri imageURl) {
        final Firebase ref = new Firebase("https://config-cbd51.firebaseio.com/");
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        final SharePostModel sharePostModel = new SharePostModel();
        sharePostModel.setTimeStamp(timeStamp());
        sharePostModel.setImageURL(imageURl.toString());
        ref.child("share_post_details").child(user.getUid()).child(timeStamp()).setValue(sharePostModel, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Toast.makeText(getActivity(), "Eroor" + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Shared sucessful", Toast.LENGTH_SHORT).show();
                }
//                proSharePostModelsArrayList.add(sharePostModel);
//                listViewProUserAdapter = new ProUserTimeLineListViewAdapter(proSharePostModelsArrayList, getActivity());
//                lvProUserTimeLine.setAdapter(listViewProUserAdapter);
            }
        });
    }

    //method to generate time stamp
    private String timeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
    }

    private void proceedAfterPermission() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, Constants.PICKCONTACT_REQUEST_CODE);
    }


}

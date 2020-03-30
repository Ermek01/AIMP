package com.example.aimp;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;


import com.example.aimp.fragments.ControllerFragment;
import com.example.aimp.fragments.MainFragment;
import com.example.aimp.music.PlayerService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public class MainActivity extends AppCompatActivity implements ServiceConnection {

    public static MusicAIDL mRemote = null;
    public static final int KEY_PER = 123;
    private SlidingUpPanelLayout panelLayout;

    private PlayerService.ServiceToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        token = PlayerService.bindToService(MainActivity.this, MainActivity.this);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {Manifest.permission.READ_EXTERNAL_STORAGE}, KEY_PER);
            return;
        }
        else {
            setupViews();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case KEY_PER:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    setupViews();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setupViews() {

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));

        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        Fragment fragment = new MainFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_container, fragment);
        transaction.commit();

        //setPanelSlideListener(panelLayout);

        //new initQuickControls().execute("");

    }

//    private void setPanelSlideListener(SlidingUpPanelLayout panelLayout) {
//
//        panelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
//            @Override
//            public void onPanelSlide(View panel, float slideOffset) {
//                View playingCon = ControllerFragment.top_container;
//                playingCon.setAlpha(1-slideOffset);
//            }
//
//            @Override
//            public void onPanelCollapsed(View panel) {
//                View playingCon = ControllerFragment.top_container;
//                playingCon.setAlpha(1);
//            }
//
//            @Override
//            public void onPanelExpanded(View panel) {
//                View playingCon = ControllerFragment.top_container;
//                playingCon.setAlpha(0);
//            }
//
//            @Override
//            public void onPanelAnchored(View panel) {
//
//            }
//
//            @Override
//            public void onPanelHidden(View panel) {
//
//            }
//        });
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (token != null){
            PlayerService.unBindToService(token);
            token = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
         mRemote = MusicAIDL.Stub.asInterface(iBinder);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mRemote = null;
    }

    public class initQuickControls extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            ControllerFragment fragment1 = new ControllerFragment();
            FragmentManager fragmentManager1 = getSupportFragmentManager();
            fragmentManager1.beginTransaction()
                    .replace(R.id.controls_container, fragment1).commitAllowingStateLoss();
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}

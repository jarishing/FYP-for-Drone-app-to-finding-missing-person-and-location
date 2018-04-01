package com.example.finding;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import dji.sdk.base.BaseProduct;

/**
 * Created by jarishing on 20/10/2017.
 */

public class ConnectionActivity extends Activity {
    private static final String TAG = ConnectionActivity.class.getName();

    private Button mBtnOpen,InputBtn;
    private TextView mConnection,mGallery;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        initUI();
        OnClick();

        // Register the broadcast receiver for receiving the device connection's changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(FPVApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }
    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }
    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }
    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }
    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSDKRelativeUI();
        }
    };

    private void refreshSDKRelativeUI() {
        BaseProduct mProduct = FPVApplication.getProductInstance();
        if (null != mProduct && mProduct.isConnected()) {
            Log.v(TAG, "refreshSDK: True");
            mBtnOpen.setEnabled(true);
            mBtnOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            });

            }else {
            Toast.makeText(getApplicationContext(), "Disconnect", Toast.LENGTH_LONG).show();
        }
    }

    private void initUI() {
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        InputBtn = (Button) findViewById(R.id.Gallery_View_btn);

        mConnection = (TextView)findViewById(R.id.connection_connect);
        mGallery = (TextView)findViewById(R.id.connection_gallery);

        mConnection.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/StudioGothicAlternate-ExtraLigh Trial.ttf"));
        mGallery.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/StudioGothicAlternate-ExtraLigh Trial.ttf"));
    }

    private void OnClick(){
        InputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Gallery_Page = new Intent();
                Gallery_Page.setClass(getApplication(), GalleryActivity.class);
                startActivity(Gallery_Page);
            }
        });
    }
}

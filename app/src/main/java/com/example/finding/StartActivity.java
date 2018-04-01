package com.example.finding;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import static android.os.Build.VERSION_CODES.M;
/**
 * Created by jarishing on 19/11/2017.
 */

public class StartActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSION = 2;

    private ImageButton Start_Button;
    private TextView Title_front,Title_back;

    ImageButton test;

    private DataBase Db;

    // Storage Permissions
    private static String[] PERMISSIONS_REQ = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.READ_PHONE_STATE
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Db = new DataBase(getApplicationContext());

        // For API 23+ you need to request the read/write permissions even if they are already in your manifest.
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentapiVersion >= M) {
            verifyPermissions(this);
        }

        initUI();
        OnClick();
        
    }

    private void initUI() {
        Title_front = (TextView) findViewById(R.id.Title_font);
        Title_back = (TextView) findViewById(R.id.Title_back);

        Start_Button = (ImageButton) findViewById(R.id.start_button);

        Title_front.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/StudioGothicAlternate-ExtraLigh Trial.ttf"));
        Title_back.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/StudioGothicAlternate-ExtraLigh Trial.ttf"));

    }

    private void OnClick() {
        Start_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Connection_Page = new Intent();
                Connection_Page.setClass(getApplication(), ConnectionActivity.class);
                startActivity(Connection_Page);
            }
        });
    }

    private static boolean verifyPermissions(Activity activity) {
        // Check if we have write permission
        int camera_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int write_external_storage_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_external_storage_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int access_coarse_location_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
        int access_fine_location_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        int read_phone_state_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE);

        if (camera_permission != PackageManager.PERMISSION_GRANTED ||
                write_external_storage_permission != PackageManager.PERMISSION_GRANTED ||
                access_coarse_location_permission != PackageManager.PERMISSION_GRANTED ||
                access_fine_location_permission != PackageManager.PERMISSION_GRANTED ||
                read_phone_state_permission != PackageManager.PERMISSION_GRANTED ||
                read_external_storage_permission != PackageManager.PERMISSION_GRANTED) {

            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_REQ,
                    REQUEST_CODE_PERMISSION
            );
            return false;
        } else {
            return true;
        }
    }
}

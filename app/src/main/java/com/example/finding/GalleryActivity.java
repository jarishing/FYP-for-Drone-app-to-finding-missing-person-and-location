package com.example.finding;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by jarishing on 20/10/2017.
 */

public class GalleryActivity extends Activity {

    private static final String TAG = "MainActivity";
    private DataBase Db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Db = new DataBase(getApplicationContext());

        final ListView lv =(ListView) findViewById(R.id.ListView);

        lv.setAdapter(new CustomAdapter(GalleryActivity.this,getData()));
    }

    private ArrayList<Spacecraft> getData(){
        ArrayList<Spacecraft> spacecrafts= new ArrayList<>();

        File TargetFolder = new File(Environment.getExternalStorageDirectory()+"/DjiPhoto");

        Spacecraft s;

        if(TargetFolder.exists()){

            File[] files = TargetFolder.listFiles();

            SQLiteDatabase db = Db.getReadableDatabase();
            String sql = "SELECT * FROM Photo_Data";
            Cursor LoadData = db.rawQuery(sql, null);

            for(int i=0;i<files.length;i++){

                File file=files[i];


                LoadData.moveToNext();
                String droneLocationLat = LoadData.getString(0);
                String droneLocationLng = LoadData.getString(1);

                s=new Spacecraft();
                s.getUri(Uri.fromFile(file));
                s.getName(file.getName());
                s.getdroneLocationLat(droneLocationLat);
                s.getdroneLocationLng(droneLocationLng);

                spacecrafts.add(s);
            }
        }

        return spacecrafts;
    }
}



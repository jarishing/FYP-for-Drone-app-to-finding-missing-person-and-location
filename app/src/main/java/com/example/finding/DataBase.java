package com.example.finding;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jarishing on 6/11/2017.
 */

public class DataBase extends SQLiteOpenHelper {

    private static final String DB_NAME = "photo.db";
    private static final int DB_VERSION = 1;
    private SQLiteDatabase bd;

    String PhotoData = "create table Photo_Data(" +
            "DroneLocationLat text," +
            "DroneLocationLng text," +
            "PhotoName text)";

    public DataBase(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PhotoData);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long Add_New_Photo(String DroneLocationLat, String DroneLocationLng, String PhotoName){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("DroneLocationLat", DroneLocationLat);
        values.put("DroneLocationLng", DroneLocationLng);
        values.put("PhotoName", PhotoName);

        long Data = db.insert("Photo_Data", null, values);

        db.close();

        return Data;
    }

    public void Delete_Table(){
        SQLiteDatabase db = getReadableDatabase();
        db.delete("Photo_Data", "1", null);

    }
}

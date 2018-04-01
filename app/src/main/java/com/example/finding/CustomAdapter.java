package com.example.finding;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by jarishing on 6/11/2017.
 */

public class CustomAdapter extends BaseAdapter {
    private static final String TAG = "CustomAdapter";
    Context c;
    ArrayList<Spacecraft> spacecrafts;

    public CustomAdapter(Context c, ArrayList<Spacecraft> spacecrafts){
        this.c =c;
        this.spacecrafts = spacecrafts;
    }

    @Override
    public int  getCount(){
        return spacecrafts.size();
    }

    @Override
    public Object getItem(int i){
        return spacecrafts.get(i);
    }

    @Override
    public long getItemId(int i){
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup){
        if(view == null){
            view = LayoutInflater.from(c).inflate(R.layout.list_single,viewGroup,false);
        }

        final Spacecraft s = (Spacecraft) this.getItem(i);

        ImageView img = (ImageView) view.findViewById(R.id.File_Photo);
        TextView nameTxt = (TextView) view.findViewById(R.id.Photo_Name);
        final ImageView gMap = (ImageView) view.findViewById(R.id.Drone_location);

        Picasso.with(c).load(s.getUri()).placeholder(R.drawable.replace).into(img);
        nameTxt.setText(s.getName());

        Log.d(TAG,"photo uri ="+s.getUri());

        String src ="https://maps.googleapis.com/maps/api/staticmap?center="+s.getdroneLocationLat()+","+s.getdroneLocationLng()+"&markers=color:red%7Clabel:C%7C"+s.getdroneLocationLat()+","+s.getdroneLocationLng()+"&zoom=19&size=600x400";


        GetImageByUrl getImageByUrl = new GetImageByUrl();
        getImageByUrl.setImage(gMap, src);

        gMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Gallery_Map_Act = new Intent(c,GalleryMapViewActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Gallery_Map_Act.putExtra("Drone_Location_Lat", s.getdroneLocationLat());
                Gallery_Map_Act.putExtra("Drone_Location_Lng", s.getdroneLocationLng());
                c.startActivity(Gallery_Map_Act);
            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Gallery_Photo_Act = new Intent(c,GalleryPhotoViewActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Gallery_Photo_Act.putExtra("Photo_Uri", s.getUri());
                c.startActivity(Gallery_Photo_Act);
            }
        });

        return view;
    }
}

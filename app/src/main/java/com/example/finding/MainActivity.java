package com.example.finding;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dji.common.product.Model;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;

import static com.example.finding.FPVApplication.getCameraInstance;
import static com.example.finding.FPVApplication.getProductInstance;


public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, GoogleMap.OnMapClickListener, OnMapReadyCallback{

    private static final String TAG = MainActivity.class.getName();

    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;

    private HandlerThread mHOG_Handler = new HandlerThread("HOG");
    private HandlerThread mColor_Detect_Handler = new HandlerThread("Color_Detect");
    private Handler HOG_Handler,mHandler,Color_Detect_Handler;

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;

    private DataBase Db;

    protected TextureView mVideoSurface = null;
    private ImageButton mCaptureBtn;
    private ImageView mImageView;
    private GoogleMap gMap;
    private double droneLocationLat = 22.3361759, droneLocationLng = 114.1735153;
    private String RecordDroneLat,RecordDroneLng;
    private Mat grayMat,people,mRgba;
    private Bitmap NonDetect,Detected;
    private int color_mode;
    private Scalar Color_Range_Low,Color_Range_High;
    private ImageButton mDetection_Menu;

    //private double droneLocationLat = 22.3361122, droneLocationLng = 114.1728399;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHOG_Handler.start();
        mColor_Detect_Handler.start();

        mHandler = new Handler();
        HOG_Handler = new Handler(mHOG_Handler.getLooper());
        Color_Detect_Handler = new Handler(mColor_Detect_Handler.getLooper());

        initUI();
        OnClick();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Drone_location);
        mapFragment.getMapAsync(this);

        Db = new DataBase(getApplicationContext());

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataCallBack = new VideoFeeder.VideoDataCallback() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };
    }
    protected void onProductChange() {
        initPreviewer();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        onProductChange();
        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mOpenCVLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mOpenCVLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mOpenCVLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG,"OpenCV loaded successfully");
                    try{

                    }
                    catch (Exception e)
                    {

                    }
                }break;
                default:
                {
                    super.onManagerConnected(status);
                }break;
            }
        }
    };

    private void initPreviewer() {
        BaseProduct product = getProductInstance();
        if (product == null || !product.isConnected()) {
            Toast.makeText(getApplicationContext(), "Disconnect", Toast.LENGTH_LONG).show();
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().setCallback(mReceivedVideoDataCallBack);
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = getCameraInstance();
        if (camera != null){
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().setCallback(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mColor_Detect_Handler != null){
            mColor_Detect_Handler.interrupt();
        }
        if (mHOG_Handler != null){
            mHOG_Handler.interrupt();
        }
    }
    public void onReturn(View view){
        this.finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mColor_Detect_Handler != null){
            mColor_Detect_Handler.interrupt();
        }
        if (mHOG_Handler != null){
            mHOG_Handler.interrupt();
        }
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }

    }
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }
        return false;
    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private void initUI() {
        // init mVideoSurface
        mVideoSurface = (TextureView)findViewById(R.id.Drone_Video_View);
        mCaptureBtn = (ImageButton) findViewById(R.id.Camera_Button);
        mImageView  = (ImageView) findViewById(R.id.OpenCV_View);

        mDetection_Menu = (ImageButton) findViewById(R.id.Detection_Menu_Button);
    }

    private void OnClick(){
        mCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //captureAction();
                storeImage(getBitmap());
            }
        });

        mDetection_Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu DetectionMenu = new PopupMenu(MainActivity.this, mCaptureBtn);
                DetectionMenu.getMenuInflater().inflate(R.menu.menu_detection, DetectionMenu.getMenu());

                DetectionMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.Cancel_Detection_Select:
                                mImageView.setVisibility(View.GONE);
                                if (mColor_Detect_Handler != null){
                                    mColor_Detect_Handler.interrupt();
                                }
                                if (mHOG_Handler != null){
                                    mHOG_Handler.interrupt();
                                }
                                break;
                            case R.id.HOG_Detection_Select:
                                mImageView.setVisibility(View.VISIBLE);
                                if (mColor_Detect_Handler != null){
                                    mColor_Detect_Handler.interrupt();
                                }
                                HOG_Handler.post(HOG);
                                break;
                            case R.id.Red_Color_Selected:
                                mImageView.setVisibility(View.VISIBLE);
                                color_mode = 1;
                                mImageView.setVisibility(View.VISIBLE);
                                if (mHOG_Handler != null){
                                    mHOG_Handler.interrupt();
                                }
                                Color_Detect_Handler.post(Color_Detect);
                                break;
                            case R.id.Blue_Color_Selected:
                                mImageView.setVisibility(View.VISIBLE);
                                color_mode = 2;
                                mImageView.setVisibility(View.VISIBLE);
                                if (mHOG_Handler != null){
                                    mHOG_Handler.interrupt();
                                }
                                Color_Detect_Handler.post(Color_Detect);
                                break;
                            case R.id.Green_Color_Selected:
                                mImageView.setVisibility(View.VISIBLE);
                                color_mode = 3;
                                mImageView.setVisibility(View.VISIBLE);
                                if (mHOG_Handler != null){
                                    mHOG_Handler.interrupt();
                                }
                                Color_Detect_Handler.post(Color_Detect);
                                break;
                            case R.id.White_Color_Selected:
                                mImageView.setVisibility(View.VISIBLE);
                                color_mode = 4;
                                mImageView.setVisibility(View.VISIBLE);
                                if (mHOG_Handler != null){
                                    mHOG_Handler.interrupt();
                                }
                                Color_Detect_Handler.post(Color_Detect);
                                break;
                            case R.id.Black_Color_Selected:
                                mImageView.setVisibility(View.VISIBLE);
                                color_mode = 5;
                                mImageView.setVisibility(View.VISIBLE);
                                if (mHOG_Handler != null){
                                    mHOG_Handler.interrupt();
                                }
                                Color_Detect_Handler.post(Color_Detect);
                                break;
                        }
                        return true;
                    }
                });
                DetectionMenu.show();
            }
        });
    }

    private Runnable Color_Detect = new Runnable() {
        @Override
        public void run() {
            try{
                for(int n = 0; n<100; n++){
                    mHandler.post(Color_Detect_putView);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable Color_Detect_putView = new Runnable() {
        @Override
        public void run() {
            NonDetect = getBitmap();
            Bitmap Detect = Bitmap.createScaledBitmap(NonDetect,640,360,true);

            Mat mRgba = new Mat();
            Utils.bitmapToMat(Detect,mRgba);

            if(color_mode == 1) {
                //red color
                Color_Range_Low = new Scalar(110, 100, 120);
                Color_Range_High = new Scalar(130, 255, 255);
            }else if(color_mode == 2) {
                //blue color
                Color_Range_Low = new Scalar(0, 50, 50);
                Color_Range_High = new Scalar(30, 255, 255);
            }else if(color_mode == 3) {
                //green color
                Color_Range_Low = new Scalar(40,60,60);
                Color_Range_High = new Scalar(80,255,255);
            }else if(color_mode == 4) {
                //white color
                Color_Range_Low = new Scalar(0, 0, 200);
                Color_Range_High = new Scalar(180, 255, 255);
            }else if(color_mode == 5) {
                //black color
                Color_Range_Low = new Scalar(0, 0, 0);
                Color_Range_High = new Scalar(180, 255, 30);
            }

            Mat imgHSV = new Mat();
            Mat threshold_img = new Mat();
            Mat mHierarchy = new Mat();
            MatOfPoint2f approxCurve = new MatOfPoint2f();

            Imgproc.cvtColor(mRgba,imgHSV,Imgproc.COLOR_BGR2HSV);
            Core.inRange(imgHSV,Color_Range_Low,Color_Range_High,threshold_img);
            Imgproc.erode(threshold_img,threshold_img, new Mat());
            Imgproc.dilate(threshold_img,threshold_img, new Mat());

            Imgproc.threshold(threshold_img,threshold_img, 125, 255, Imgproc.THRESH_BINARY);

            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Imgproc.findContours(threshold_img, (List<MatOfPoint>) contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            //For each contour found
            for (int i = 0; i < contours.size(); i++) {
                //Convert contours(i) from MatOfPoint to MatOfPoint2f
                MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
                //Processing on mMOP2f1 which is in type MatOfPoint2f
                double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
                Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

                //Convert back to MatOfPoint
                MatOfPoint points = new MatOfPoint(approxCurve.toArray());

                // Get bounding rect of contour
                org.opencv.core.Rect rect = Imgproc.boundingRect(points);

                if((rect.width*rect.height) > 800 && (rect.width*rect.height) < 80000) {
                    // draw enclosing rectangle (all same color, but you could use variable i to make them unique)
                    Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(100), 3);
                }

            }

            Utils.matToBitmap(mRgba,Detect);
            mImageView.setImageBitmap(Detect);
        }
    };

    private Runnable HOG = new Runnable() {
        @Override
        public void run() {
            try{
                // Android View require own handler? confirfmed yes
                for(int i = 0; i<100; i++) {
                    mHandler.post(HOG_putView);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable HOG_putView = new Runnable() {
        @Override
        public void run() {
            NonDetect = getBitmap();
            Bitmap Detect = Bitmap.createScaledBitmap(NonDetect,720,480,true);

            Mat mRgba = new Mat();
            Utils.bitmapToMat(Detect,mRgba);

            Mat grayMat = new Mat();
            Mat people = new Mat();

            Imgproc.cvtColor(mRgba, grayMat, Imgproc.COLOR_BGR2GRAY);

            HOGDescriptor hog = new HOGDescriptor();
            hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

            MatOfRect faces = new MatOfRect();
            MatOfDouble weights = new MatOfDouble();

            hog.detectMultiScale(grayMat, faces, weights);
            mRgba.copyTo(people);

            org.opencv.core.Rect[] facesArray = faces.toArray();

            for(int i = 0; i < facesArray.length; i++){
                Imgproc.rectangle(people, facesArray[i].tl(), facesArray[i].br(), new Scalar(100), 3);
            }

            Utils.matToBitmap(people, Detect);

            mImageView.setImageBitmap(Detect);
        }
    };

   // private void captureAction() {
   //     final Camera camera = getCameraInstance();
   //
   //     if (camera != null){
   //         SettingsDefinitions.ShootPhotoMode photoMode = SettingsDefinitions.ShootPhotoMode.SINGLE; // Set the camera capture mode as Single mode
   //         camera.setShootPhotoMode(photoMode, new CommonCallbacks.CompletionCallback(){
   //             @Override
   //             public void onResult(DJIError djiError) {
   //                 if (null == djiError) {
   //                     handler.postDelayed(new Runnable() {
   //                         @Override
   //                         public void run() {
   //                             camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
   //                                 @Override
   //                                 public void onResult(DJIError djiError) {
   //                                     if (djiError == null) {
   //                                         Toast.makeText(getApplicationContext(), "Take Photo", Toast.LENGTH_LONG).show();
   //                                     } else {
   //                                         Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_LONG).show();
   //                                     }
   //                                 }
   //                             });
   //
   //                         }
   //                     }, 2000);
   //                 }
   //             }
   //         });
   //     }
   // }

    public Bitmap getBitmap() {
        return mVideoSurface.getBitmap();
    }

    private void storeImage(Bitmap image){
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null){
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        }
        try{
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            Toast.makeText(getApplicationContext(), "Take Photo", Toast.LENGTH_LONG).show();
            fos.close();

            String No_Use_Filename = "drone.jpg";

            RecordDroneLat = String.valueOf(droneLocationLat);
            RecordDroneLng = String.valueOf(droneLocationLng);

            Db.Add_New_Photo(RecordDroneLat,RecordDroneLng,No_Use_Filename);

        } catch (FileNotFoundException e){
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e){
            Log.d(TAG, "Error accessing file: "+ e.getMessage());
        }
    }

    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        // p.s: MODIFY THE PATH YOU WANT YOUR IMAGE TO STORE HERE

        // 嗨嗨如果你想改儲存路徑記得改這邊

        // for example, you can just using this way to save image into the root of sdcard
        // File mediaStorageDir = new File(Environment.getExternalStorageDirectory().toString());

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()+"/DjiPhoto");

        //Environment.getExternalStorageDirectory()+"/DjiPhoto"

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name

        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // TODO Auto-generated method stub
        // Initializing Amap object
        if (gMap == null) {
            gMap = googleMap;
            setUpMap();
        }
        LatLng cityu = new LatLng(droneLocationLat, droneLocationLng);
        float zoomlevel = (float) 17.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(cityu, zoomlevel);
        gMap.addMarker(new MarkerOptions().position(cityu));
        gMap.moveCamera(cu);
    }

    private void setUpMap() {
        gMap.setOnMapClickListener(this);// add the listener for click for amap object
    }
}

package com.example.finding;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.IOException;

/**
 * Created by jarishing on 6/11/2017.
 */

public class GalleryPhotoViewActivity extends AppCompatActivity {

    private static final String TAG = "PhotoAct";

    ImageView Enlarge_Photo;
    Uri Photo_Uri;
    Context context;
    Bitmap Detection_Photo;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_photo_view);

        Photo_Uri = getIntent().getParcelableExtra("Photo_Uri");

        //Log.d(TAG,"photo uri ="+Photo_Uri);
        getView();

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mOpenCVLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mOpenCVLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        try {
            Detection_Photo = MediaStore.Images.Media.getBitmap(getContentResolver(), Photo_Uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Mat imgMAT = new Mat (Detection_Photo.getHeight(), Detection_Photo.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(Detection_Photo,imgMAT);

        Mat mPhoto_Graymat = new Mat();
        Mat mPhoto_people = new Mat();

        Imgproc.cvtColor(imgMAT, mPhoto_Graymat, Imgproc.COLOR_BGR2GRAY);

        HOGDescriptor hog = new HOGDescriptor();
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

        MatOfRect faces = new MatOfRect();
        MatOfDouble weights = new MatOfDouble();

        hog.detectMultiScale(mPhoto_Graymat, faces, weights);
        imgMAT.copyTo(mPhoto_people);

        org.opencv.core.Rect[] facesArray = faces.toArray();

        for(int i = 0; i < facesArray.length; i++){
            Imgproc.rectangle(mPhoto_people, facesArray[i].tl(), facesArray[i].br(), new Scalar(100), 3);
        }

        Utils.matToBitmap(mPhoto_people, Detection_Photo);

        Enlarge_Photo.setImageBitmap(Detection_Photo);
        //Enlarge_Photo.setImageURI(Photo_Uri);

    }

    private void getView() {
        Enlarge_Photo = (ImageView) findViewById(R.id.Enlarge_Photo);
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }

}

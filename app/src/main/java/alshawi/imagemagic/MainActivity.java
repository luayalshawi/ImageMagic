package alshawi.imagemagic;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static String TAG = "MainActivity";
    private CameraView javaCameraView;
    private Mat mRgba,imgGray,mRgbaT,mRgbaF;
    private BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status)
            {
                case BaseLoaderCallback.SUCCESS:
                {
                    javaCameraView.enableView();
                    break;
                }
                default:
                {
                    super.onManagerConnected(status);
                    break;
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        javaCameraView = (CameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.disableFpsMeter();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if(javaCameraView !=null)
        {
            javaCameraView.disableView();
        }

    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(javaCameraView !=null)
        {
            javaCameraView.disableView();
        }
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        if(OpenCVLoader.initDebug())
        {
            Log.d(TAG,"OpenCv works!");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else
        {
            Log.d(TAG,"OpenCv is not supported!");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this,mLoaderCallBack);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8UC4);
        mRgbaT = new Mat(height,width, CvType.CV_8UC4);
        mRgbaF = new Mat(height,width, CvType.CV_8UC4);
        imgGray = new Mat(height,width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        imgGray.release();
        mRgbaT.release();
        mRgbaF.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        return mRgba;
    }

    public void onImageCapture(View v)
    {
        Log.d(TAG,"Click on the Image");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = dateFormat.format(new Date());

        //image full path with extension
        String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath()+currentDateandTime + ".jpg";
        javaCameraView.takePicture(fileName);

        //Make image accessible to all applications by adding the image to media gallery
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(fileName);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);


        Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();
    }
}

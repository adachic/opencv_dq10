package org.opencv.samples.tutorial3;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class Sample3Native extends Activity implements CvCameraViewListener {
    private static final String TAG = "OCVSample::Activity";

    private Mat                    mRgba;
    private Mat                    mGrayMat;
    private CameraBridgeViewBase   mOpenCvCameraView;
    private AssetManager am;
    private Mat[] templEnemy;
    
    private BaseLoaderCallback     mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("native_sample");

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Sample3Native() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial3_surface_view);
        am = getAssets();

                
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial4_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }
    
    private void setupAssetToMats(){
    	//�e���v���[�g�摜��templ�ɂ���
    	Bitmap src = null;
        try {
    		for(int i=0; i< 8 ; i++){
    			switch(i){
    			case 0:
        			src = BitmapFactory.decodeStream(new BufferedInputStream(am.open("arrow0_40.png")));
        			break;
    			case 1:
    	    		src = BitmapFactory.decodeStream(new BufferedInputStream(am.open("arrow1_40.png")));
        			break;
    			case 2:
    	    		src = BitmapFactory.decodeStream(new BufferedInputStream(am.open("arrow2_40.png")));
        			break;
    			case 3:
    	    		src = BitmapFactory.decodeStream(new BufferedInputStream(am.open("arrow3_40.png")));
        			break;
    			case 4:
    	    		src = BitmapFactory.decodeStream(new BufferedInputStream(am.open("arrow4_40.png")));
        			break;
    			case 5:
    	    		src = BitmapFactory.decodeStream(new BufferedInputStream(am.open("arrow5_40.png")));
        			break;
    			case 6:
    	    		src = BitmapFactory.decodeStream(new BufferedInputStream(am.open("arrow6_40.png")));
        			break;
    			case 7:
    	    		src = BitmapFactory.decodeStream(new BufferedInputStream(am.open("arrow7_40.png")));
        			break;
    			}
            	Bitmap src2 = src.copy(Bitmap.Config.ARGB_8888, true);
                Utils.bitmapToMat(src2, templEnemy[i]);         
                src.recycle();
    		}
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} finally{
    		;        		
    	}
    }

    @Override
    public void onPause()
    {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGrayMat = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGrayMat.release();
    }

    public Mat onCameraFrame(Mat inputFrame) {
        templEnemy = new Mat[8];
        for(int i=0 ; i<8; i++){
           	templEnemy[i] = new Mat();       	
        }
        setupAssetToMats();

        Mat dst = null;
        double max = 0.0;
        //�e���v���[�g�}�b�`���O
    	for(int i=0; i< 8; i++){
            Mat result = new Mat();
            Imgproc.matchTemplate(inputFrame, templEnemy[i], result, Imgproc.TM_CCOEFF_NORMED);
            Core.MinMaxLocResult maxr = Core.minMaxLoc(result);
            Log.i(TAG, "maxval:" + maxr.maxVal);

            if(maxr.maxVal < 0.1){
            	continue;
            }
            if(maxr.maxVal > max){
            	max = maxr.maxVal;
            }else{
            	continue;
            }
            //�}�b�`���O���ʂ̕\��
            Point maxp = maxr.maxLoc;
            Point pt2 = new Point(maxp.x + templEnemy[i].width(), maxp.y + templEnemy[i].height());
            dst = inputFrame.clone();
            Core.rectangle(dst, maxp, pt2, new Scalar(255,0,0), 2);

            
              		
    	}
    	if(dst==null){
    		return inputFrame;
    	}
    	
        return dst;
    //    return mRgba;
    }

    public native void FindFeatures(long matAddrGr, long matAddrRgba);
}

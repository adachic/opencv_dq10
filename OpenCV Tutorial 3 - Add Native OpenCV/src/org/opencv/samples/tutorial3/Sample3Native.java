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

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Sample3Native extends Activity implements CvCameraViewListener {
    private static final String TAG = "OCVSample::Activity";

    private Mat                    mRgba;
    private Mat                    mGrayMat;
    private CameraBridgeViewBase   mOpenCvCameraView;
    private AssetManager am;
    private Mat[] templEnemy;
    
    private static final String ACTION_USB_PERMISSION = "com.regaria.simpleTransrateADKActivity.action.USB_PERMISSION";

    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;

    UsbAccessory mAccessory;
    ParcelFileDescriptor mFileDescriptor;
    FileOutputStream mOutputStream;
    
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
        
        /**/
        mUsbManager = UsbManager.getInstance(this);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter);
        /**/
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial3_surface_view);
        am = getAssets();
        status = searchStatus;
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial4_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }
    
    private void setupAssetToMats(){
    	//テンプレート画像をtemplにする
    	Bitmap src = null;
        try {
    		for(int i=0; i< 9 ; i++){
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
    			case 8:
    	    		src = BitmapFactory.decodeStream(new BufferedInputStream(am.open("window_b.png")));
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
        closeAccessory();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (mOutputStream != null) {
            return;
        }

        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (mUsbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            } else {
                synchronized (mUsbReceiver) {
                    if (!mPermissionRequestPending) {
                        mUsbManager.requestPermission(accessory,
                                mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.d(TAG, "mAccessory is null");
        }    	
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    private void openAccessory(UsbAccessory accessory) {

        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mOutputStream = new FileOutputStream(fd);

            Log.d(TAG, "accessory opened");
        } else {
            Log.d(TAG, "accessory open fail");
        }
    }

    private void closeAccessory() {
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = UsbManager.getAccessory(intent);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    } else {
                        Log.d(TAG, "permission denied for accessory "
                                + accessory);
                    }
                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = UsbManager.getAccessory(intent);
                if (accessory != null && accessory.equals(mAccessory)) {
                    closeAccessory();
                }
            }
        }
    };

    private final byte MOVE_UP = 0x10;
    private final byte CAMERA_LEFT = 0x11;
    private final byte CAMERA_RIGHT = 0x12;
    private final byte LED_OFF = 0x00;
    private final byte LED_ON = 0x01;

    public void sendCommand(byte command, byte value) {

        byte[] buffer = new byte[2];

        buffer[0] = command;
        buffer[1] = value;
        if (mOutputStream != null) {
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        }
    }
 
    
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGrayMat = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGrayMat.release();
    }

    private final int buttleStatus = 2;
    private final int movingStatus = 1;
    private final int searchStatus = 0;
    private int status = searchStatus;
    
    private void resetCommand(){
        sendCommand(MOVE_UP, 	  LED_OFF);
        sendCommand(CAMERA_RIGHT, LED_OFF);
        sendCommand(CAMERA_LEFT,  LED_OFF);    	
    }
    
    private void intoButtle(){
    	status = buttleStatus;
    	resetCommand();
    }

    //5秒歩く
    private void intoMoving(){
    	status = movingStatus;
    	resetCommand();
    	sendCommand(MOVE_UP,LED_ON);

		try {
			Thread.sleep(5000);
	    	sendCommand(MOVE_UP,LED_OFF);    	       
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*
    	Handler mHandler = new Handler();
    	Runnable mUpdateTimeTask = new Runnable() {
    	   public void run() {
    	    	sendCommand(MOVE_UP,LED_OFF);
    	   }
    	};
    	mHandler.postDelayed(mUpdateTimeTask, 3000);
*/
    }    
    
    public Mat onCameraFrame(Mat inputFrame) {
        templEnemy = new Mat[9];
        for(int i=0 ; i<9; i++){
           	templEnemy[i] = new Mat();       	
        }
        setupAssetToMats();

        Mat dst = null;
        Mat result = new Mat();
        double max = 0.0;

		/*バトル状態検出*/
        Imgproc.matchTemplate(inputFrame, templEnemy[8], result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult maxr = Core.minMaxLoc(result);
        Point maxp = maxr.maxLoc;
		if(maxp.x < 200 && maxp.y > 200){
			intoButtle();            			
			return result;
		}
		if(status == buttleStatus){
			status = searchStatus;			
		}
        
        //テンプレートマッチング
    	for(int i=0; i< 8; i++){
            Imgproc.matchTemplate(inputFrame, templEnemy[i], result, Imgproc.TM_CCOEFF_NORMED);
            maxr = Core.minMaxLoc(result);
            Log.i(TAG, "maxval:" + maxr.maxVal);

            if(maxr.maxVal < 0.1){
            	continue;
            }
            if(maxr.maxVal > max){
            	max = maxr.maxVal;
            }else{
            	continue;
            }
            //マッチング結果の表示
            maxp = maxr.maxLoc;
            Point pt2 = new Point(maxp.x + templEnemy[i].width(), maxp.y + templEnemy[i].height());
            dst = inputFrame.clone();
            Core.rectangle(dst, maxp, pt2, new Scalar(255,0,0), 2);

            //なにかしらのマッチがあった
    		if(maxp.y < 240){
    			//カメラ補正：左右距離
    			if(maxp.x<360){
    				int leftAjust = 360 - (int)maxp.x;
    				int mount = leftAjust*1000/360;
    				sendCommand(CAMERA_LEFT,LED_ON);
    				
    				try {
						Thread.sleep((long)mount);
		    	    	sendCommand(CAMERA_LEFT,LED_OFF);    	       
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				/*
    				Handler mHandler = new Handler();
    		    	Runnable mUpdateTimeTask = new Runnable() {
    		    	   public void run() {
    		    	    	sendCommand(CAMERA_LEFT,LED_OFF);    	       
    		    	   }
    		    	};
    		    	mHandler.postDelayed(mUpdateTimeTask, mount);    				
	*/
    			}else{
    				int rightAjust =  (int)maxp.x -360;
    				int mount = rightAjust*1000/360;
    				sendCommand(CAMERA_RIGHT,LED_ON);
    				try {
						Thread.sleep((long)mount);
		    	    	sendCommand(CAMERA_RIGHT,LED_OFF);    	       
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				/*
    				Handler mHandler = new Handler();
    		    	Runnable mUpdateTimeTask = new Runnable() {
    		    	   public void run() {
    		    	    	sendCommand(CAMERA_RIGHT,LED_OFF);    	       
    		    	   }
    		    	};
    		    	mHandler.postDelayed(mUpdateTimeTask, mount);    				    				
					*/
    			}
    			//直進
    			intoMoving();
    		}            
    	}
    	if(dst==null){
    		return inputFrame;
    	}
    	
        return dst;
    }

    public native void FindFeatures(long matAddrGr, long matAddrRgba);
}

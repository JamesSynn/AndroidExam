package com.example.camerathird;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private CameraView mCamView;     // 카메라 프리뷰를 보여주는 SurfaceView 
    private Button btnCapture;       // '촬영' 버튼
	private Button btnCancel;        // '취소' 버튼 (현재 액티비티 종료)
	private Button btnChangeFacing;  // '카메라 전환' 버튼
	private int mCameraFacing;       // 전면 or 후면 카메라 상태 저장
	private static MainActivity me; // CameraView에서 Activity.finish()로 액티비티를 종료시키기 위한 변수
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	    
	mCamView = (CameraView)findViewById(R.id.preview);
	me = this;        
	mCameraFacing  = Camera.CameraInfo.CAMERA_FACING_BACK; // 최초 카메라 상태는 후면카메라로 설정     
	    
	        // '카메라 전환' 버튼을 선택하여 카메라를 새로 생성하면 
	        // contentView 설정과 Listener 설정 역시 다시 해주어야 하므로,
	        // 해당 부분들을 init()메소드로 빼내어 onClick()에서 재호출하도록 함
	        init();		
	    }
	
	@SuppressLint("NewApi")
	private void init(){
	        mCamView = new CameraView(me, mCameraFacing);
	        setContentView(mCamView);
	        addContentView(LayoutInflater.from( me ).inflate( R.layout.activity_main, null), 
	                       new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ));
		
	        btnCapture=(Button)findViewById(R.id.shutter);
	        btnCapture.setOnClickListener(new Button.OnClickListener(){
	            @Override
	            public void onClick(View arg0) {
	                mCamView.capture();
	            }			
	        });
		
	        btnCancel = (Button) findViewById(R.id.canceal);
	        btnCancel.setOnClickListener(new Button.OnClickListener(){
		@Override
		    public void onClick(View arg0) {
			MainActivity.exitCamera();
		}					
	    });
		
	    btnChangeFacing = (Button) findViewById(R.id.change);
	    btnChangeFacing.setOnClickListener(new Button.OnClickListener(){
		@Override
		public void onClick(View arg0) {
	                // 전면 -> 후면 or 후면 -> 전면으로 카메라 상태 전환
		    mCameraFacing = (mCameraFacing==Camera.CameraInfo.CAMERA_FACING_BACK) ? 
		                     Camera.CameraInfo.CAMERA_FACING_FRONT 
	                                 : Camera.CameraInfo.CAMERA_FACING_BACK;
				
	                // 변경된 방향으로 새로운 카메라 View 생성
		    mCamView = new CameraView(me, mCameraFacing);
	
	
	                // ContentView, Listener 재설정
		init();
		}					
	});
	}
	
	public static void exitCamera(){
	me.finish();
	}
	}
	
	class CameraView extends SurfaceView implements SurfaceHolder.Callback {
	
	private static SurfaceHolder mHolder;
	private static Camera mCamera;
	private static int mCameraFacing;
	
	public CameraView(Context context, int cameraFacing) {
	super(context);
	
	mHolder = getHolder();
	mHolder.addCallback(this);
	mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	
	mCameraFacing = cameraFacing;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		 mCamera.startPreview();
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {	
	try
	    {
	    mCamera = Camera.open(mCameraFacing);
	    Parameters parameters = mCamera.getParameters();
	    parameters.set("jpeg-quality", 70);
		parameters.setPictureFormat(PixelFormat.JPEG);
		parameters.setPictureSize(640, 480);
		mCamera.setParameters(parameters);
			
	    mCamera.setPreviewDisplay(mHolder);
	        
	}
	catch(IOException e)
	{
	    mCamera.release();
	    mCamera = null;
	  	
	    e.printStackTrace(System.out);
	}		
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	
	mCamera.stopPreview();
	mCamera.release();
	mCamera = null;
	}
	
	public void capture(){
	if(mCamera!=null)
	    mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
	}
	
	ShutterCallback shutterCallback = new ShutterCallback() { 
	public void onShutter () { 
	}
	};
	
	PictureCallback rawCallback = new PictureCallback(){
	public void onPictureTaken(byte[] data, Camera camera){
	}
	};
	
	PictureCallback jpegCallback = new PictureCallback(){
	public void onPictureTaken(final byte[] data, Camera camera){
		   
	    mCamera.stopPreview();
		new Thread(new Runnable(){
		       @Override
		       public void run() {
			   BufferedOutputStream bos = null;
			   try{
	           	   bos = new BufferedOutputStream
	                                (new FileOutputStream((String.format("capture.jpg", 
	                                                                     System.currentTimeMillis()))));
		           bos.write(data);
		           bos.close();
		   } catch(IOException e){
		           e.printStackTrace();
		           }
			   }  
		   }).start();
		   
		MainActivity.exitCamera();
	   }
};
}
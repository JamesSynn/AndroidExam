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

	private CameraView mCamView;     // ī�޶� �����並 �����ִ� SurfaceView 
    private Button btnCapture;       // '�Կ�' ��ư
	private Button btnCancel;        // '���' ��ư (���� ��Ƽ��Ƽ ����)
	private Button btnChangeFacing;  // 'ī�޶� ��ȯ' ��ư
	private int mCameraFacing;       // ���� or �ĸ� ī�޶� ���� ����
	private static MainActivity me; // CameraView���� Activity.finish()�� ��Ƽ��Ƽ�� �����Ű�� ���� ����
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	    
	mCamView = (CameraView)findViewById(R.id.preview);
	me = this;        
	mCameraFacing  = Camera.CameraInfo.CAMERA_FACING_BACK; // ���� ī�޶� ���´� �ĸ�ī�޶�� ����     
	    
	        // 'ī�޶� ��ȯ' ��ư�� �����Ͽ� ī�޶� ���� �����ϸ� 
	        // contentView ������ Listener ���� ���� �ٽ� ���־�� �ϹǷ�,
	        // �ش� �κе��� init()�޼ҵ�� ������ onClick()���� ��ȣ���ϵ��� ��
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
	                // ���� -> �ĸ� or �ĸ� -> �������� ī�޶� ���� ��ȯ
		    mCameraFacing = (mCameraFacing==Camera.CameraInfo.CAMERA_FACING_BACK) ? 
		                     Camera.CameraInfo.CAMERA_FACING_FRONT 
	                                 : Camera.CameraInfo.CAMERA_FACING_BACK;
				
	                // ����� �������� ���ο� ī�޶� View ����
		    mCamView = new CameraView(me, mCameraFacing);
	
	
	                // ContentView, Listener �缳��
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
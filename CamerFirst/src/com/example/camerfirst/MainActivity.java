package com.example.camerfirst;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	MyCameraSurface mSurface;
	Button mShutter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSurface = (MyCameraSurface)findViewById(R.id.preview);

		// ���� ��Ŀ�� ����
		findViewById(R.id.focus).setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				mShutter.setEnabled(false);
				//mSurface.mCamera.autoFocus(mAutoFocus);
			}
		});
		
		// ���� �Կ�
				mShutter = (Button)findViewById(R.id.shutter);
				mShutter.setOnClickListener(new Button.OnClickListener() {
					public void onClick(View v) {
						mSurface.mCamera.takePicture(null, null, mPicture);
					}
				});
	}
	
	// ��Ŀ�� �����ϸ� �Կ� �㰡
		AutoFocusCallback mAutoFocus = new AutoFocusCallback() {
			public void onAutoFocus(boolean success, Camera camera) {
				mShutter.setEnabled(success);
			}
		};
		
		// ���� ����.
		@SuppressWarnings("deprecation")
		PictureCallback mPicture = new PictureCallback() {
			public void onPictureTaken(byte[] data, Camera camera) {
				String path = "/sdcard/cameratest.jpg";

				File file = new File(path);
				try {
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(data);
					fos.flush();
					fos.close();
				} catch (Exception e) {
					Toast.makeText(MainActivity.this, "���� ���� �� ���� �߻� : " + 
							e.getMessage(), 0).show();
					return;
				}
				
				Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				Uri uri = Uri.parse("file://" + path);
				intent.setData(uri);
				sendBroadcast(intent);

				Toast.makeText(MainActivity.this, "���� ���� �Ϸ� : " + path, 0).show();
				mSurface.mCamera.startPreview();
			}
		};
		
}
/////////////////////////////////////////////////////////////////////////////
//�̸����� ȭ�� �����ֱ����� Ŭ����
////////////////////////////////////////////////////////////////////////////
//�̸����� ǥ�� Ŭ����
class MyCameraSurface extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;

	public MyCameraSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	// ǥ�� ������ ī�޶� �����ϰ� �̸����� ����
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			mCamera.release();
			mCamera = null;
		}
	}

 // ǥ�� �ı��� ī�޶� �ı��Ѵ�.
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	// ǥ���� ũ�Ⱑ ������ �� ������ �̸����� ũ�⸦ ���� �����Ѵ�.
 public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		Camera.Parameters params = mCamera.getParameters();
     List<Size> arSize = params.getSupportedPreviewSizes();
     if (arSize == null) {
			params.setPreviewSize(width, height);
     } else {
	        int diff = 10000;
	        Size opti = null;
	        for (Size s : arSize) {
	        	if (Math.abs(s.height - height) < diff) {
	        		diff = Math.abs(s.height - height);
	        		opti = s;
	        		
	        	}
	        }
			params.setPreviewSize(opti.width, opti.height);
     }
		mCamera.setParameters(params);
		mCamera.startPreview();
	}
}
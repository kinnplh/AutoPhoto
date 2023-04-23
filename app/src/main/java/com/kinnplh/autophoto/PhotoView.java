package com.kinnplh.autophoto;

import static android.os.Environment.getExternalStorageDirectory;

import static com.kinnplh.autophoto.MainActivity.delta;
import static com.kinnplh.autophoto.MainActivity.th;

import android.app.Service;
import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.function.BinaryOperator;

public class PhotoView extends LinearLayout implements SurfaceTextureListener {
    private TextureView textureView;

    /**
     * 相机类
     */
    public Camera myCamera;
    public static PhotoView ins;
    private Context context;

    private WindowManager mWindowManager;
    public int num = 0;
    private long loadTime;
    private Bitmap bitmap_get = null;

    public static String TAG = "PhotoView";



    public PhotoView(Context context) {
        super(context);
        ins = this;
        loadTime = System.currentTimeMillis();
//        LayoutInflater.from(context).inflate(R.layout.window, this);
        textureView = new TextureView(context);
        this.addView(textureView);
        this.context = context;
        setAlpha(0.5f);
        initView();
    }

    public void disableSelf(){
        if(myCamera != null){
            myCamera.stopPreview(); //停止预览
            myCamera.release();     // 释放相机资源
            myCamera = null;
        }
    }

    private void initView() {
//        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
        mWindowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (myCamera == null) {
            // 创建Camera实例
            //尝试开启前置摄像头
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    try {
                        Log.d("Demo", "tryToOpenCamera");
                        myCamera = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                // 设置预览在textureView上
                myCamera.setPreviewTexture(surface);
                myCamera.setDisplayOrientation(SetDegree(PhotoView.this));
                // 开始预览
//                myCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void getPreViewImage() {
        if (myCamera != null){
            myCamera.setPreviewCallback(new Camera.PreviewCallback(){
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    try{
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        if(image!=null){
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                            bitmap_get = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                            //**********************
                            //因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上
                            bitmap_get = rotateMyBitmap(bitmap_get);
                            //**********************************
                            stream.close();
                            myCamera.stopPreview();
                        }
                    }catch(Exception ex){
                        Log.e("Sys","Error:"+ex.getMessage());
                    }
                }
            });
        }
    }

    public Bitmap rotateMyBitmap(Bitmap mybmp){
        //*****旋转一下
        Matrix matrix = new Matrix();
        matrix.postRotate(270);

        Bitmap bitmap = Bitmap.createBitmap(mybmp.getWidth(), mybmp.getHeight(), Bitmap.Config.ARGB_8888);

        Bitmap nbmp2 = Bitmap.createBitmap(mybmp, 0,0, mybmp.getWidth(),  mybmp.getHeight(), matrix, true);

        saveImage(nbmp2);
        return nbmp2;
    };

    public void saveImage(Bitmap bmp) {
//        myFace(bmp);
        String fileName =loadTime + "_" + num +".jpg";

        File file = new File(context.getExternalFilesDir(null).getAbsolutePath(), fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int SetDegree(PhotoView photoView) {
        // 获得手机的方向
        int rotation = mWindowManager.getDefaultDisplay().getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        disableSelf();
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

}

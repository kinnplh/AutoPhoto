package com.kinnplh.autophoto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.graphics.PixelFormat;
import android.os.Bundle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static MainActivity self;
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 5469;
    public PhotoView photoView;
    private Button startOrStop;
    public static double th = 0.4;
    public static long delta = 2000;

    public EditText thInput;
    public EditText deltaInput;

    @TargetApi(23)
    public void testOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            Manifest.permission.CAMERA,};

    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            for(String per: PERMISSIONS){
                int permission = ActivityCompat.checkSelfPermission(activity,
                        per);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    String [] crt = {per};
                    ActivityCompat.requestPermissions(activity, PERMISSIONS, REQUEST_EXTERNAL_STORAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thInput = findViewById(R.id.th);
        deltaInput = findViewById(R.id.delta);

        startOrStop = findViewById(R.id.start_or_stop);
        startOrStop.setText("Start");
        startOrStop.setOnClickListener(view -> {
            if(photoView == null){
                try {
                    th = Double.parseDouble(thInput.getText().toString());
                    delta = Long.parseLong(deltaInput.getText().toString());
                } catch (NumberFormatException e){
                    Toast.makeText(this, "不合法输入", Toast.LENGTH_LONG).show();
                    return;
                }

                thInput.setEnabled(false);
                deltaInput.setEnabled(false);
                photoView = new PhotoView(this);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(100, 100,0,0,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                        PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.TOP | Gravity.LEFT;
//        ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(photoView, params);
                ((WindowManager) getSystemService(WINDOW_SERVICE)).addView(photoView, params);
                startOrStop.setText("Stop");

                if(AutoPhotoService.instance == null){
                    new AutoPhotoService(this);
                }
            } else {
                photoView.disableSelf();
                photoView = null;
                startOrStop.setText("Start");
                thInput.setEnabled(true);
                deltaInput.setEnabled(true);
                if(AutoPhotoService.instance != null){
                    AutoPhotoService.instance.stop();
                    AutoPhotoService.instance = null;
                }
            }
        });

        verifyStoragePermissions(this);
        self = this;
        if (Build.VERSION.SDK_INT >= 23){
            testOverlayPermission();
        }
    }

    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1101;
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS) {
            if (!hasPermission()) {
                //若用户未开启权限，则引导用户开启“Apps with usage access”权限
                startActivityForResult(
                        new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                        MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
            }
        }
    }


}
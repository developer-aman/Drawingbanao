package com.agtechnosolution.drawingbanao;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DrawingView drawingView;
    private ImageButton slideMenuBtn,saveBtn,undoBtn,redoBtn,editBtn;
    private RelativeLayout slidingMenu;
    private boolean isUp=false;
    public static final int REQUEST_CODE_WRI_PERMISSION = 1;
    public static final int REQUEST_CODE_READ_PERMISSION = 2;
//    public static final int REQUEST_CODE_OPEN_FILE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try{
            getSupportActionBar().hide();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        drawingView=findViewById(R.id.drawView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        drawingView.init(metrics);
        slideMenuBtn=findViewById(R.id.slide_up_btn);
        slidingMenu=findViewById(R.id.sliding_menu);
        editBtn=findViewById(R.id.edit_btn);
        saveBtn=findViewById(R.id.save_btn);
        undoBtn=findViewById(R.id.undo_btn);
        redoBtn=findViewById(R.id.redo_btn);
        slideMenuBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        undoBtn.setOnClickListener(this);
        redoBtn.setOnClickListener(this);
        editBtn.setOnClickListener(this);
    }

    public void slideUp(RelativeLayout view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(0,0,view.getHeight(),0);
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.setAnimation(animate);
    }

    public void slideDown(RelativeLayout view){
        view.setVisibility(View.INVISIBLE);
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.setAnimation(animate);
    }

    public void requestForWritePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRI_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_CODE_WRI_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    drawingView.save(this);
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Permission Denied");
                    alertDialog.setMessage("Allow this permission to save image.");
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.slide_up_btn:
                if(isUp){
                    slideDown(slidingMenu);
                    slideMenuBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.arrow_up_float));
                }else{
                    slideUp(slidingMenu);
                    slideMenuBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.arrow_down_float));
                }
                isUp = !isUp;
                break;

            case R.id.save_btn:
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestForWritePermission();
                } else
                    drawingView.save(this);
                break;

            case R.id.edit_btn:
                Toast.makeText(this,"Edit Feature Coming Soon.",Toast.LENGTH_LONG).show();
                break;

            case R.id.undo_btn:
                drawingView.undo();
                break;

            case R.id.redo_btn:
                drawingView.redo();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView=getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView=getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

}

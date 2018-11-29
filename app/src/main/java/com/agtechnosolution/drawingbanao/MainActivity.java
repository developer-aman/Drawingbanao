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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.agtechnosolution.drawingbanao.POJO.Drawing;

import java.io.File;
import java.io.FileNotFoundException;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DrawingView drawingView;
    private ImageButton slideMenuBtn,saveBtn,undoBtn,redoBtn,editBtn,eraseBtn,brushBtn;
    private RelativeLayout slidingMenu;
    private boolean isUp=true;
    private AlertDialog ad;
    Realm mRealm;
    RealmResults<Drawing> realmResults;
    ImagesRecylerAdapter adapter;



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
        eraseBtn=findViewById(R.id.erase_btn);
        brushBtn=findViewById(R.id.brush_btn);
        slideMenuBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        undoBtn.setOnClickListener(this);
        redoBtn.setOnClickListener(this);
        editBtn.setOnClickListener(this);
        eraseBtn.setOnClickListener(this);
        brushBtn.setOnClickListener(this);
    }

    public void slideUp(RelativeLayout view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(0,0,view.getHeight(),0);
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.setAnimation(animate);
    }

    public void slideDown(RelativeLayout view){
        view.setVisibility(View.GONE);
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.setAnimation(animate);
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
                drawingView.save(this);
                break;

            case R.id.edit_btn:
                final AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.CustomDialog);
                LayoutInflater inflater=LayoutInflater.from(this);
                View content=inflater.inflate(R.layout.dialog_images,null);
                RecyclerView imageRecycler=content.findViewById(R.id.images_recycler);
                ImageButton closeButton=content.findViewById(R.id.close_btn);
                TextView emptyTxt=content.findViewById(R.id.empty_txt);
                populateDialog();
                if(realmResults.isEmpty()){
                    imageRecycler.setVisibility(View.GONE);
                    emptyTxt.setVisibility(View.VISIBLE);
                }else {
                    if(!imageRecycler.isShown())
                        imageRecycler.setVisibility(View.VISIBLE);
                    emptyTxt.setVisibility(View.GONE);
                    adapter = new ImagesRecylerAdapter(realmResults);
                    imageRecycler.setLayoutManager(new GridLayoutManager(this, 4));
                    imageRecycler.setAdapter(adapter);
                }
                    imageRecycler.addOnItemTouchListener(new RecyclerItemClickListener(this, imageRecycler, new RecyclerItemClickListener.ClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            byte[] imgBytes=realmResults.get(position).getImage();
                            BitmapFactory.Options opt=new BitmapFactory.Options();
                            opt.inJustDecodeBounds=false;
                            Bitmap bmpClicked=BitmapFactory.decodeByteArray(imgBytes,0,imgBytes.length,opt);
                            ad.dismiss();
                            drawingView.editImage(bmpClicked);
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {
                            //Do nothing
                        }
                    }));
                builder.setView(content);
                ad=builder.show();
                ad.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad.dismiss();
                    }
                });
                break;


            case R.id.undo_btn:
                drawingView.undo();
                break;

            case R.id.redo_btn:
                drawingView.redo();
                break;

            case R.id.erase_btn:
                drawingView.erase();
                break;

            case R.id.brush_btn:
                drawingView.brushDrawing();
                break;
        }
    }

    public void populateDialog(){
        mRealm=Realm.getDefaultInstance();
        realmResults=mRealm.where(Drawing.class).findAll();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRealm!=null)
            mRealm.close();
    }
}

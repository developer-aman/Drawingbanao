package com.agtechnosolution.drawingbanao;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.agtechnosolution.drawingbanao.POJO.FingerPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by AnujPc on 26-11-2018.
 */

public class DrawingView extends View {

    public static int BRUSH_SIZE=18;
    public static final int DEFAULT_COLOR= Color.BLACK;
    public static final int DEFAULT_BG_COLOR= Color.WHITE;
    private static final float TOUCH_TOLERANCE= 4;
    private float pointX,pointY;
    private Path path;
    private Paint paint;
    private ArrayList<FingerPath> pathsList = new ArrayList<>();
    private ArrayList<FingerPath> redoList = new ArrayList<>();
    private int currentColor;
    private int backgroundColor=DEFAULT_BG_COLOR;
    private int strokeWidth;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);
    private int lastPath;
//    private boolean editMode=false;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(DEFAULT_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        //        paint.setAlpha(0xff);
    }

    public void init(DisplayMetrics metrics){
        int height=metrics.heightPixels;
        int width=metrics.widthPixels;
        bitmap=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        canvas=new Canvas(bitmap);
        currentColor=DEFAULT_COLOR;
        strokeWidth=BRUSH_SIZE;
    }

    public void clear(){
        backgroundColor=DEFAULT_BG_COLOR;
        pathsList.clear();
        invalidate();
    }

    public void undo(){
        if(!pathsList.isEmpty()) {
            lastPath=pathsList.size() - 1;
            redoList.add(pathsList.get(lastPath));
            pathsList.remove(lastPath);
            invalidate();
        }
    }

    public void redo(){
        if(!redoList.isEmpty()){
            lastPath=redoList.size() - 1;
            pathsList.add(redoList.get(lastPath));
            redoList.remove(lastPath);
            invalidate();
        }
    }

//    public void editImage(Bitmap alteredBmp, Bitmap gotbitmap){
//        editMode=true;
////       canvas=new Canvas(bitmap);
////        paint.setColor(DEFAULT_COLOR);
////        paint.setStrokeWidth(BRUSH_SIZE);
////        canvas.drawBitmap(bitmap,0,0,paint);
//        bitmap=gotbitmap;
//        canvasBitmapEdit=alteredBmp;
//
////        pathsList.clear();
////        Canvas newCanvas=new Canvas();
////        newCanvas.drawBitmap(bitmap,0,0,bitmapPaint);
//    }

    public String getDateTimeInString()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date=new Date();
        return dateFormat.format(date);
    }


    public void save(Context context){
        setDrawingCacheEnabled(true);
        Bitmap saveBitmap=getDrawingCache();
        if(saveBitmap!=null) {
            File dir=new File(Environment.getExternalStorageDirectory(),"/Drawings/");
            if(!dir.exists()){
                dir.mkdirs();
            }
            File output=new File(dir,"imgfile"+getDateTimeInString()+".jpg");
            OutputStream os= null;
            try {
                os = new FileOutputStream(output);
                saveBitmap.compress(Bitmap.CompressFormat.JPEG,80,os);
                os.flush();
                os.close();
                Toast.makeText(context,"Image saved at "+output.getAbsolutePath(),Toast.LENGTH_LONG).show();
                destroyDrawingCache();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(context,"Image not found",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
            canvas.save();
            canvas.drawColor(backgroundColor);
            for (FingerPath fp : pathsList) {
                paint.setColor(fp.color);
                paint.setStrokeWidth(fp.strokeWidth);
                canvas.drawPath(fp.path, paint);
            }
            canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
            canvas.restore();
    }

    //This function used when initial touch on screen
    private void touchStart(float x,float y){
        redoList.clear();
        path = new Path();
        FingerPath fingerPath = new FingerPath(currentColor,strokeWidth,path);
        pathsList.add(fingerPath);
        path.reset();
        path.moveTo(x,y);
        pointX=x;
        pointY=y;
    }

    //This function used when keeping finger down and moving on screen
    private void touchMove(float x,float y){
        float dx = Math.abs(x - pointX);
        float dy = Math.abs(y - pointY);

        if(dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            path.quadTo(pointX,pointY,(x + pointX)/2,(y + pointY)/2);
            pointX=x;
            pointY=y;
        }
    }

    //This function used when after drawing, finger is removed from the screen
    private void touchUp(){
        path.lineTo(pointX,pointY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN :
                touchStart(x,y);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                touchMove(x,y);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }

        return true;
    }
}

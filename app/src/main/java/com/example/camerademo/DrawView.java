package com.example.camerademo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class DrawView extends LinearLayout {
    private int phoneWidth,phoneHeight;
    private int width , height ;

    public DrawView(Context context) {
        super(context);
    }
    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        DisplayMetrics dm = new DisplayMetrics();

        paint.setColor(Color.WHITE);// 设置白色
        paint.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
        Log.d("jjb", "onDraw:width = "+ width + " height = " + height);
        if(width == 0 && height == 0){
            canvas.drawLine(width/3, 0, width/3, 0, paint);// 画竖线
            canvas.drawLine(width*2/3, 0, width*2/3, 0, paint);
            canvas.drawLine(0, (phoneHeight-height)/2+height/3, 0, (phoneHeight-height)/2+height/3, paint);// 画横线
            canvas.drawLine(0, (phoneHeight-height)/2+height*2/3, 0, (phoneHeight-height)/2+height*2/3, paint);
        }else {
            canvas.drawLine(width/3, (phoneHeight-height)/2, width/3, (phoneHeight-height)/2+height, paint);// 画竖线
            canvas.drawLine(width*2/3, (phoneHeight-height)/2, width*2/3, (phoneHeight-height)/2+height, paint);
            canvas.drawLine(0, (phoneHeight-height)/2+height/3, width, (phoneHeight-height)/2+height/3, paint);// 画横线
            canvas.drawLine(0, (phoneHeight-height)/2+height*2/3, width, (phoneHeight-height)/2+height*2/3, paint);
            Log.d("jjb", "onDraw: phoneheight = " + phoneHeight + " , " + height);
        }
    }
    public void setSize(int width,int height){
        this.width = width;
        this.height = height;
        invalidate();
    }
    public void getPhone(int phoneWidth,int phoneHeight){
        this.phoneHeight = phoneHeight;
        this.phoneWidth = phoneWidth;
    }

}

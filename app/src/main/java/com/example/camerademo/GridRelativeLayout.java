package com.example.camerademo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class GridRelativeLayout extends RelativeLayout {
    private Canvas myCanvas;
    private int horGrid = 100, verGrid = 100;//水平网格和竖直网格
    private int screenW, screenH;//屏幕宽和高
    private boolean initOver = false;//初始化标签

    public GridRelativeLayout(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public GridRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public GridRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.myCanvas = canvas;
        Paint paint = new Paint();
        paint.setColor(android.graphics.Color.YELLOW);//颜色
        paint.setStrokeWidth(3);//线宽
        int verNum = (int)(screenH / verGrid) + 1;
        int horNum = (int)(screenW / horGrid) + 1;
        if (initOver) {
            for (int i = 0; i < verNum; i++) {
                canvas.drawLine(0, i * verGrid - 1, screenW, i * verGrid - 1,
                        paint);
            }
            for (int i = 0; i < horNum; i++) {
                canvas.drawLine(i * horGrid - 1, 0, i * horGrid - 1, screenH,
                        paint);
            }
        }
    }

    /**设置网格线參数**/
    public void setInf(int vergrid, int horgrid, int screenW, int screenH) {
        this.verGrid = vergrid;
        this.horGrid = horgrid;
        this.screenW = screenW;
        this.screenH = screenH;
        initOver = true;
        postInvalidate();
    }

    /**擦除网格线**/
    public void clearLine()
    {
        initOver = false;
        postInvalidate();
    }
}
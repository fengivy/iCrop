package com.ivy.icrop;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ivy on 2016/4/6.
 */
public class OverLayView extends View {
    private Paint mPaintOver;
    private Paint mPaintShape;
    private Path mPathShape;
    private RectF mRectFView;
    private int overlayColor=Color.parseColor("#55000000");
    private int shapeColor=Color.parseColor("#ffffff");
    private int shapeBorderWidth=3;
    public static final int SHAPE_CIRCLE=0;
    public static final int SHAPE_SQUARE=1;
    public int shapeType=SHAPE_SQUARE;
    public OverLayView(Context context) {
        super(context);
        init();
    }

    public OverLayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverLayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OverLayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaintOver=new Paint();
        mPaintOver.setAntiAlias(true);
        mPaintOver.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintOver.setColor(overlayColor);
        mPathShape=new Path();
        mRectFView=new RectF();

        mPaintShape=new Paint();
        mPaintShape.setAntiAlias(true);
        mPaintShape.setColor(shapeColor);
        mPaintShape.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRectFView.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawOverLay(canvas);
        drawShape(canvas);
    }

    private void drawShape(Canvas canvas){
        if (shapeType==SHAPE_CIRCLE){
            mPaintShape.setStrokeWidth(shapeBorderWidth);
            canvas.drawPath(mPathShape, mPaintShape);
        }else if(shapeType==SHAPE_SQUARE){
            mPaintShape.setStrokeWidth(shapeBorderWidth);
            canvas.drawPath(mPathShape, mPaintShape);
        }
    }

    private void drawOverLay(Canvas canvas){
        canvas.save();
        setShapePath();
        canvas.clipRect(0, 0, getMeasuredWidth(), getMeasuredHeight());
        canvas.clipPath(mPathShape, Region.Op.DIFFERENCE);
        canvas.drawRect(mRectFView, mPaintOver);
        canvas.restore();
    }


    private void setShapePath(){
        mPathShape.reset();
        if (shapeType==SHAPE_CIRCLE){
            mPathShape.addCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, Math.min(getMeasuredWidth(),getMeasuredHeight()) / 3.0f, Path.Direction.CCW);
        }else if(shapeType==SHAPE_SQUARE){
            float squareWidth=Math.min(getMeasuredWidth(),getMeasuredHeight())/3.0f*2;
            float beginX=(getMeasuredWidth()-squareWidth)/2;
            float beginY=(getMeasuredHeight()-squareWidth)/2;
            mPathShape.addRect(beginX,beginY,beginX+squareWidth,beginY+squareWidth, Path.Direction.CW);
        }
    }

    @Nullable
    public RectF getCropPath(){
        float squareWidth=Math.min(getMeasuredWidth(),getMeasuredHeight())/3.0f*2;
        float beginX=(getMeasuredWidth()-squareWidth)/2;
        float beginY=(getMeasuredHeight()-squareWidth)/2;
        return new RectF(beginX,beginY,beginX+squareWidth,beginY+squareWidth);
    }
}

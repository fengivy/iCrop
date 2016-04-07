package com.ivy.icrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewTreeObserver;

/**
 * Created by ivy on 2016/4/6.
 */
public class ICropImageView extends TranslationImageView implements ViewTreeObserver.OnGlobalLayoutListener{
    //最后移动的位置
    private float lastMoveX,lastMoveY;
    private ScaleGestureDetector mScaleGestureDetector;
    private static float MAX_SCALE=5,MIN_SCALE=1;
    private float middleX,middleY;
    private float mMaxResultImageSizeX=0;
    private float mMaxResultImageSizeY=0;

    public ICropImageView(Context context) {
        super(context);
        initCrop();
    }

    public ICropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCrop();
    }

    public ICropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCrop();
    }

    public ICropImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initCrop();
    }

    private void initCrop() {
        mScaleGestureDetector=new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
               /* if (detector.getScaleFactor() > 1 && getCurrentScale() * detector.getScaleFactor() <= MAX_SCALE) {
                    postScale(detector.getScaleFactor(), middleX, middleY);
                } else if (detector.getScaleFactor() < 1 && getCurrentScale() * detector.getScaleFactor() >= MIN_SCALE) {
                    postScale(detector.getScaleFactor(), middleX, middleY);
                }else if(detector.getScaleFactor()>1&&getCurrentScale()*detector.getScaleFactor()>=MAX_SCALE){
                    postScale(MAX_SCALE/getCurrentScale(),middleX,middleY);
                }else{
                    postScale(MIN_SCALE/getCurrentScale(),middleX,middleY);
                }*/
                float scale=getCurrentScale();
                float scaleFactor=detector.getScaleFactor();
                /**
                 * 缩放的范围控制
                 */
                if ((scale < MAX_SCALE && scaleFactor > 1.0f)|| (scale > MIN_SCALE && scaleFactor < 1.0f))
                {
                    /**
                     * 最大值最小值判断
                     */
                    if (scaleFactor * scale < MIN_SCALE)
                    {
                        scaleFactor = MIN_SCALE / scale;
                    }
                    if (scaleFactor * scale > MAX_SCALE)
                    {
                        scaleFactor = MAX_SCALE / scale;
                    }
                    /**
                     * 设置缩放比例
                     */
                    postScale(scaleFactor, middleX, middleY);
                }
                return true;
            }
        });
    }

    private int lastPointerCount;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /**
         * 每当触摸点发生变化时，重置lastMoveX , lastMoveY
         */
        if (event.getPointerCount() != lastPointerCount)
        {
            lastMoveX = event.getX();
            lastMoveY = event.getY();
        }
        lastPointerCount=event.getPointerCount();
        int action=event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                lastMoveX = event.getX();
                lastMoveY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                translation(event.getX(),event.getY());
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        if (event.getPointerCount()>1){
            middleX = (event.getX(0) + event.getX(1)) / 2;
            middleY = (event.getY(0) + event.getY(1)) / 2;
        }
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    boolean isCheckTopAndBottom,isCheckLeftAndRight;
    private void translation(float x,float y){
        float dx=x-lastMoveX;
        float dy=y-lastMoveY;
        RectF rectF=getMatrixRectF();
        if (getDrawable() != null)
        {
            isCheckTopAndBottom = isCheckLeftAndRight = true;
            // 如果宽度小于屏幕宽度，则禁止左右移动
            if (rectF.width() < getWidth())
            {
                dx = 0;
                isCheckLeftAndRight = false;
            }
            // 如果高度小于屏幕高度，则禁止上下移动
            if (rectF.height() < getHeight())
            {
                dy = 0;
                isCheckTopAndBottom = false;
            }
            postTranslation(dx,dy);
            checkMatrixBounds();
        }
        lastMoveX=x;
        lastMoveY=y;
    }

    /**
     * 移动时，进行边界判断，主要判断宽或高大于屏幕的
     */
    private void checkMatrixBounds()
    {
        RectF rect = getMatrixRectF();

        float deltaX = 0, deltaY = 0;
        final float viewWidth = getWidth();
        final float viewHeight = getHeight();
        // 判断移动或缩放后，图片显示是否超出屏幕边界
        if (rect.top > 0 && isCheckTopAndBottom)
        {
            deltaY = -rect.top;
        }
        if (rect.bottom < viewHeight && isCheckTopAndBottom)
        {
            deltaY = viewHeight - rect.bottom;
        }
        if (rect.left > 0 && isCheckLeftAndRight)
        {
            deltaX = -rect.left;
        }
        if (rect.right < viewWidth && isCheckLeftAndRight)
        {
            deltaX = viewWidth - rect.right;
        }
        postTranslation(deltaX, deltaY);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    boolean once=true;
    @Override
    public void onGlobalLayout() {
        if (once)
        {
            Drawable d = getDrawable();
            if (d == null)
                return;
            int width = getWidth();
            int height = getHeight();
            // 拿到图片的宽和高
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();
            float scale = 1.0f;
            // 如果图片的宽或者高大于屏幕，则缩放至屏幕的宽或者高
            if (dw<=width&&dh<=height){
                scale=Math.max(width*1.0f/dw,height*1.0f/dh);
            }
            if (dw<width&&dh>=height){
                scale=width*1.0f/dw;
            }
            if (dh<height&&dw>=width){
                scale=height*1.0f/dh;
            }
            if(dw>=width&&dh>=height){
                scale=Math.max(width*1.0f/dw,height*1.0f/dh);
            }

            RectF initialImageRect = new RectF(0, 0, dw, dh);
            mInitialImageCorners = RectUtils.getCornersFromRect(initialImageRect);
            mInitialImageCenter = RectUtils.getCenterFromRect(initialImageRect);

            // 图片移动至屏幕中心
            MIN_SCALE=scale;
            postTranslation((width - dw) / 2, (height - dh) / 2);
            postScale(scale);
            once = false;


        }
    }

    @Nullable
    public Bitmap cropImage(RectF cropRect) {
        Bitmap viewBitmap = getViewBitmap();
        if (viewBitmap == null || viewBitmap.isRecycled()) {
            System.out.println("----------------bitmap null");
            return null;
        }

        //cancelAllAnimations();
        //setImageToWrapCropBounds(false);

        RectF currentImageRect = RectUtils.trapToRect(mCurrentImageCorners);
        if (currentImageRect.isEmpty()) {
            System.out.println("----------------currentImageRect null");
            return null;
        }

        float currentScale = getCurrentScale();
        //float currentAngle = getCurrentAngle();

        if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {
            float cropWidth = cropRect.width() / currentScale;
            float cropHeight = cropRect.height() / currentScale;

            if (cropWidth > mMaxResultImageSizeX || cropHeight > mMaxResultImageSizeY) {

                float scaleX = mMaxResultImageSizeX / cropWidth;
                float scaleY = mMaxResultImageSizeY / cropHeight;
                float resizeScale = Math.min(scaleX, scaleY);

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(viewBitmap,
                        (int) (viewBitmap.getWidth() * resizeScale),
                        (int) (viewBitmap.getHeight() * resizeScale), false);
                if (viewBitmap != resizedBitmap) {
                    viewBitmap.recycle();
                }
                viewBitmap = resizedBitmap;

                currentScale /= resizeScale;
            }
        }

        /*if (currentAngle != 0) {
            mTempMatrix.reset();
            mTempMatrix.setRotate(currentAngle, viewBitmap.getWidth() / 2, viewBitmap.getHeight() / 2);

            Bitmap rotatedBitmap = Bitmap.createBitmap(viewBitmap, 0, 0, viewBitmap.getWidth(), viewBitmap.getHeight(),
                    mTempMatrix, true);
            if (viewBitmap != rotatedBitmap) {
                viewBitmap.recycle();
            }
            viewBitmap = rotatedBitmap;
        }*/

        int top = (int) ((cropRect.top - currentImageRect.top) / currentScale);
        int left = (int) ((cropRect.left - currentImageRect.left) / currentScale);
        int width = (int) (cropRect.width() / currentScale);
        int height = (int) (cropRect.height() / currentScale);

        return Bitmap.createBitmap(viewBitmap, left, top, width, height);
    }

    @Nullable
    public Bitmap getViewBitmap() {
        if (getDrawable() == null/* || !(getDrawable() instanceof FastBitmapDrawable)*/) {
            return null;
        } else {
            return ((BitmapDrawable) getDrawable()).getBitmap();
        }
    }
}

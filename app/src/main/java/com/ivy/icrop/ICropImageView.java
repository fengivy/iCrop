package com.ivy.icrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewTreeObserver;

import java.util.Arrays;

/**
 * Created by ivy on 2016/4/6.
 */
public class ICropImageView extends TranslationImageView implements ViewTreeObserver.OnGlobalLayoutListener{
    private ScaleGestureDetector mScaleGestureDetector;
    private RotationGestureDetector mRotationGestureDetector;
    private GestureDetector mGestureDetector;
    private static float MAX_SCALE=5,MIN_SCALE=1;
    private float middleX,middleY;
    private float mMaxResultImageSizeX=0;
    private float mMaxResultImageSizeY=0;
    private Matrix mHelpMatrix;

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
        mHelpMatrix=new Matrix();
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
        mRotationGestureDetector=new RotationGestureDetector(getContext(), new RotationGestureDetector.OnDegreesChangeListener() {
            @Override
            public void onDegreesChange(double degress) {
                postRotation((float) degress);
            }
        });

        mGestureDetector=new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                postTranslation(-distanceX, -distanceY);
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                postScale(1.2f,e.getX(),e.getY());
                return super.onDoubleTap(e);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount()>1){
            middleX = (event.getX(0) + event.getX(1)) / 2;
            middleY = (event.getY(0) + event.getY(1)) / 2;
        }
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        mRotationGestureDetector.onTouchEvent(event);
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            setImageToWrapCropBounds();
        }
        return true;
    }

    /**
     * 移动时，进行边界判断，主要判断宽或高大于屏幕的
     */
    private void setImageToWrapCropBounds()
    {
        if(isImageWrapCropBounds(mCurrentImageCorners))
            return;
        float viewCenterX=getWidth()/2;
        float viewCenterY=getHeight()/2;
        float imageCenterX=mCurrentImageCenter[0];
        float imageCenterY=mCurrentImageCenter[1];
        float deltaX=viewCenterX-imageCenterX;
        float deltaY=viewCenterY-imageCenterY;
        float deltaScale=0;

        mHelpMatrix.reset();
        mHelpMatrix.setTranslate(deltaX, deltaY);
        final float[] tempCurrentImageCorners= Arrays.copyOf(mCurrentImageCorners,mCurrentImageCorners.length);
        mHelpMatrix.mapPoints(tempCurrentImageCorners);

        boolean willTranslation=isImageWrapCropBounds(tempCurrentImageCorners);
        System.out.println("-------------willtrans:"+willTranslation);
        if (willTranslation){
            final float[] imageIndents=calculateImageIndents();
            deltaX = -(imageIndents[0] + imageIndents[2]);
            deltaY = -(imageIndents[1] + imageIndents[3]);
        }else{
            RectF tempCropRect = new RectF(0,0,getWidth(),getHeight());
            mHelpMatrix.reset();
            mHelpMatrix.setRotate(getCurrentAngle());
            mHelpMatrix.mapRect(tempCropRect);

            final float[] currentImageSides = RectUtils.getRectSidesFromCorners(mCurrentImageCorners);

            deltaScale = Math.max(tempCropRect.width() / currentImageSides[0],
                    tempCropRect.height() / currentImageSides[1]);
            // Ugly but there are always couple pixels that want to hide because of all these calculations
            deltaScale *= 1.01;
            deltaScale = deltaScale * getCurrentScale() - getCurrentScale();
        }
        postTranslation(deltaX, deltaY);
        if (!willTranslation) {
            zoomInImage(getCurrentScale() + deltaScale, getWidth()/2, getHeight()/2);
        }

    }

    private void zoomInImage(float scale, int centerX, int centerY) {
        if (scale <= MAX_SCALE) {
            postScale(scale / getCurrentScale(), centerX, centerY);
        }
    }

    private float[] calculateImageIndents() {
        mHelpMatrix.reset();
        mHelpMatrix.setRotate(-getCurrentAngle());

        float[] unrotatedImageCorners = Arrays.copyOf(mCurrentImageCorners, mCurrentImageCorners.length);
        float[] unrotatedCropBoundsCorners = RectUtils.getCornersFromRect(new RectF(0,0,getWidth(),getHeight()));

        mHelpMatrix.mapPoints(unrotatedImageCorners);
        mHelpMatrix.mapPoints(unrotatedCropBoundsCorners);

        RectF unrotatedImageRect = RectUtils.trapToRect(unrotatedImageCorners);
        RectF unrotatedCropRect = RectUtils.trapToRect(unrotatedCropBoundsCorners);

        float deltaLeft = unrotatedImageRect.left - unrotatedCropRect.left;
        float deltaTop = unrotatedImageRect.top - unrotatedCropRect.top;
        float deltaRight = unrotatedImageRect.right - unrotatedCropRect.right;
        float deltaBottom = unrotatedImageRect.bottom - unrotatedCropRect.bottom;

        float indents[] = new float[4];
        indents[0] = (deltaLeft > 0) ? deltaLeft : 0;
        indents[1] = (deltaTop > 0) ? deltaTop : 0;
        indents[2] = (deltaRight < 0) ? deltaRight : 0;
        indents[3] = (deltaBottom < 0) ? deltaBottom : 0;
        mHelpMatrix.reset();
        mHelpMatrix.setRotate(getCurrentAngle());
        mHelpMatrix.mapPoints(indents);
        return indents;

    }

    private boolean isImageWrapCropBounds(float[] imageCorners) {
        mHelpMatrix.reset();
        mHelpMatrix.setRotate(-getCurrentAngle());

        float[] unRotatedImageCorners=Arrays.copyOf(imageCorners,imageCorners.length);
        mHelpMatrix.mapPoints(unRotatedImageCorners);



        float[] unrotatedCropBoundsCorners=RectUtils.getCornersFromRect(new RectF(0,0,getWidth(),getHeight()));
        mHelpMatrix.mapPoints(unrotatedCropBoundsCorners);
        return RectUtils.trapToRect(unRotatedImageCorners).contains(RectUtils.trapToRect(unrotatedCropBoundsCorners));
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
        Bitmap cropBitmap=viewBitmap;
        if (cropBitmap == null || cropBitmap.isRecycled()) {
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

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(cropBitmap,
                        (int) (cropBitmap.getWidth() * resizeScale),
                        (int) (cropBitmap.getHeight() * resizeScale), false);
                if (cropBitmap != resizedBitmap) {
                    //cropBitmap.recycle();
                }
                cropBitmap = resizedBitmap;

                currentScale /= resizeScale;
            }
        }
        float currentAngle=getCurrentAngle();
        if (currentAngle != 0) {
            mHelpMatrix.reset();
            mHelpMatrix.setRotate(currentAngle, cropBitmap.getWidth() / 2, cropBitmap.getHeight() / 2);

            Bitmap rotatedBitmap = Bitmap.createBitmap(cropBitmap, 0, 0, cropBitmap.getWidth(), cropBitmap.getHeight(),
                    mHelpMatrix, true);
            if (cropBitmap != rotatedBitmap) {
                //cropBitmap.recycle();
            }
            cropBitmap = rotatedBitmap;
        }

        int top = (int) ((cropRect.top - currentImageRect.top) / currentScale);
        int left = (int) ((cropRect.left - currentImageRect.left) / currentScale);
        int width = (int) (cropRect.width() / currentScale);
        int height = (int) (cropRect.height() / currentScale);

        return Bitmap.createBitmap(cropBitmap, left, top, width, height);
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

package com.ivy.icrop;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by ivy on 2016/4/6.
 */
public class TranslationImageView extends ImageView{
    private Matrix currentMatrix;
    private static final int RECT_CORNER_POINTS_COORDS = 8;
    private static final int MATRIX_VALUES_COUNT = 9;
    private static final int RECT_CENTER_POINT_COORDS = 2;
    private final float[] mMatrixValues = new float[MATRIX_VALUES_COUNT];
    protected final float[] mCurrentImageCorners = new float[RECT_CORNER_POINTS_COORDS];
    protected final float[] mCurrentImageCenter = new float[RECT_CENTER_POINT_COORDS];

    protected float[] mInitialImageCorners;
    protected float[] mInitialImageCenter;
    public TranslationImageView(Context context) {
        super(context);
        init();
    }



    public TranslationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TranslationImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TranslationImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        this.setScaleType(ScaleType.MATRIX);
        currentMatrix=new Matrix();
    }

    private void postScaleXY(float scaleX,float scaleY,float centerX,float centerY){
        currentMatrix.postScale(scaleX, scaleY, centerX, centerY);
        this.setImageMatrix(currentMatrix);
    }

    public void postScale(float scale){
        postScaleXY(scale, scale, this.getWidth() / 2, this.getHeight() / 2);
    }

    public void postScale(float scale,float centerX,float centerY){
        postScaleXY(scale, scale, centerX, centerY);
    }

    public void postTranslation(float x,float y){
        currentMatrix.postTranslate(x,y);
        this.setImageMatrix(currentMatrix);
    }

    public void postRotation(float degrees){
        currentMatrix.postRotate(degrees,getWidth()/2,getHeight()/2);
        this.setImageMatrix(currentMatrix);
    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     *
     * @return
     */
    protected RectF getMatrixRectF()
    {
        Matrix matrix = currentMatrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d)
        {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    protected Matrix getCurrentMatrix(){
        return currentMatrix;
    }

    @Override
    public void setImageMatrix(Matrix matrix) {
        super.setImageMatrix(matrix);
        updateCurrentImagePoints();
    }

    /**
     * @return - current image rotation angle.
     */
    public float getCurrentAngle() {
        return getMatrixAngle(currentMatrix);
    }

    /**
     * This method calculates rotation angle for given Matrix object.
     */
    public float getMatrixAngle(@NonNull Matrix matrix) {
        return (float) -(Math.atan2(getMatrixValue(matrix, Matrix.MSKEW_X),
                getMatrixValue(matrix, Matrix.MSCALE_X)) * (180 / Math.PI));
    }

    /**
     * @return - current image scale value.
     */
    public float getCurrentScale() {
        return getMatrixScale(currentMatrix);
    }

    /**
     * This method calculates scale value for given Matrix object.
     */
    public float getMatrixScale(@NonNull Matrix matrix) {
        return (float) Math.sqrt(Math.pow(getMatrixValue(matrix, Matrix.MSCALE_X), 2)
                + Math.pow(getMatrixValue(matrix, Matrix.MSKEW_Y), 2));
    }

    protected float getMatrixValue(@NonNull Matrix matrix, @IntRange(from = 0, to = MATRIX_VALUES_COUNT) int valueIndex) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[valueIndex];
    }

    private void updateCurrentImagePoints() {
        currentMatrix.mapPoints(mCurrentImageCorners, mInitialImageCorners);
        currentMatrix.mapPoints(mCurrentImageCenter, mInitialImageCenter);
    }
}

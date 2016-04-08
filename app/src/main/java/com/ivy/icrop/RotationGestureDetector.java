package com.ivy.icrop;

import android.content.Context;
import android.view.MotionEvent;

/**
 * Created by ivy on 2016/4/7.
 */
public class RotationGestureDetector {
    private static final int INVALID_POINTER_INDEX = -1;
    //先后落下的两个手指
    private float ax,ay,bx,by;
    private int pointerIndexOne=INVALID_POINTER_INDEX,pointerIndexTwo=INVALID_POINTER_INDEX;
    private double degrees=0;

    public RotationGestureDetector(Context context, OnDegreesChangeListener onDegreesChangeListener) {
        this.mOnDegreesChangeListener=onDegreesChangeListener;
    }

    private boolean  mIsFirstTouch = true;
    public void onTouchEvent(MotionEvent event) {
        int action=event.getActionMasked();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                ax=event.getX();
                ay=event.getY();
                pointerIndexOne=event.findPointerIndex(event.getPointerId(0));
                mIsFirstTouch=true;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                bx=event.getX();
                by=event.getY();
                pointerIndexTwo=event.findPointerIndex(event.getPointerId(event.getActionIndex()));
                mIsFirstTouch=true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointerIndexOne!=INVALID_POINTER_INDEX&&pointerIndexTwo!=INVALID_POINTER_INDEX&&event.getPointerCount()>pointerIndexTwo) {
                    if (mIsFirstTouch){
                        mIsFirstTouch=false;
                        degrees=0;
                    }else{
                        calculateRotate(event.getX(pointerIndexOne), event.getY(pointerIndexOne), event.getX(pointerIndexTwo), event.getY(pointerIndexTwo));
                    }
                    ax=event.getX(pointerIndexOne);
                    ay=event.getY(pointerIndexOne);
                    bx=event.getX(pointerIndexTwo);
                    by=event.getY(pointerIndexTwo);
                }
                break;
            case MotionEvent.ACTION_UP:
                pointerIndexOne = INVALID_POINTER_INDEX;
                break;
            case MotionEvent.ACTION_CANCEL:
                System.out.println("----------------!!!!!"+event.getPointerCount());
                pointerIndexTwo = INVALID_POINTER_INDEX;
                break;
        }
    }

    private void calculateRotate(float currentOneX,float currentOneY,float currentTwoX,float currentTwoY) {
        double currentDegrees=Math.toDegrees(Math.atan2(currentOneY-currentTwoY,currentOneX-currentTwoX));
        double oldDegrees=Math.toDegrees(Math.atan2(ay-by, ax - bx));
        double degress=currentDegrees-oldDegrees;
        if (mOnDegreesChangeListener!=null){
            mOnDegreesChangeListener.onDegreesChange(degress);
        }

    }


    public interface OnDegreesChangeListener{
        public void onDegreesChange(double degress);
    }

    public OnDegreesChangeListener mOnDegreesChangeListener;
    public void setOnDegreesChangeListener(OnDegreesChangeListener onDegreesChangeListener){
        this.mOnDegreesChangeListener=onDegreesChangeListener;
    }
}

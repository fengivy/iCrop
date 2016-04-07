package com.ivy.icrop;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ivy on 2016/4/6.
 */
public class ICropView extends FrameLayout{
    public static final int DEFAULT_COMPRESS_QUALITY = 90;
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private ICropImageView mICropImageView;
    private OverLayView mOverLayView;
    public ICropView(Context context) {
        super(context);
        init();
    }



    public ICropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ICropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ICropView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mICropImageView=new ICropImageView(getContext());
        mICropImageView.setLayoutParams(lp);
        this.addView(mICropImageView);
        mOverLayView=new OverLayView(getContext());
        mOverLayView.setLayoutParams(lp);
        this.addView(mOverLayView);
    }

    public OverLayView getOverLayView() {
        return mOverLayView;
    }

    public ICropImageView getICropImageView() {
        return mICropImageView;
    }

    public void cropAndSaveImage(Uri outputUri) {
        OutputStream outputStream = null;
        try {
            final Bitmap croppedBitmap = mICropImageView.cropImage(mOverLayView.getCropPath());
            if (croppedBitmap != null) {
                outputStream = getContext().getContentResolver().openOutputStream(outputUri);
                croppedBitmap.compress(DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, outputStream);
                croppedBitmap.recycle();

                //setResultUri(mOutputUri, mGestureCropImageView.getTargetAspectRatio());
                //finish();
            } else {
                System.out.println("----------------croppedBitmap null");
                //setResultException(new NullPointerException("CropImageView.cropImage() returned null."));
            }
        } catch (Exception e) {
            System.out.println("----------------Exception"+e.toString());
            //setResultException(e);
            //finish();
        } finally {
            close(outputStream);
        }
    }
    public void close(@Nullable Closeable c) {
        if (c != null && c instanceof Closeable) { // java.lang.IncompatibleClassChangeError: interface not implemented
            try {
                c.close();
            } catch (IOException e) {
                // silence
            }
        }
    }

}

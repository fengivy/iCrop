package com.ivy.icrop;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by ivy on 2016/4/6.
 */
public class TranslationImageView extends ImageView{
    public TranslationImageView(Context context) {
        super(context);
    }

    public TranslationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TranslationImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TranslationImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}

package com.ivy.icrop.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ivy.icrop.FileUtils;
import com.ivy.icrop.ICropImageView;
import com.ivy.icrop.ICropView;
import com.ivy.icrop.R;
import com.ivy.icrop.TranslationImageView;

import java.io.File;

/**
 * Created by ivy on 2016/4/6.
 */
public class TestActivity extends AppCompatActivity{
    ICropView mImageView;
    private float scale=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mImageView= (ICropView) this.findViewById(R.id.iv);
        mImageView.getICropImageView().setImageResource(R.drawable.aaa);
        this.findViewById(R.id.btn_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mImageView.getICropImageView().postScale(1.2f);
                String url = Environment.getExternalStorageDirectory() + "/icrop/"  + "aaa.jpg";
                FileUtils.createFile(url);
                Uri uri = Uri.fromFile(new File(url));
                mImageView.cropAndSaveImage(uri);
            }
        });
        this.findViewById(R.id.btn_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scale -= 0.2;
                mImageView.getICropImageView().postRotation(10f);
            }
        });
        this.findViewById(R.id.btn_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scale -= 0.2;
                mImageView.getICropImageView().postScale(0.8f);
            }
        });
    }
}

package com.wyp.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.wyp.avatarstudio.AvatarStudio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    CircleImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mImageView = (CircleImageView) findViewById(R.id.avataor);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action();
            }
        });
    }

    private void action() {
        new AvatarStudio.Builder(MainActivity.this)
                .needCrop(true)
                .setTextColor(Color.BLUE)
                .dimEnabled(true)
                .setAspect(1, 1)
                .setOutput(200, 200)
                .setText("打开相机", "从相册中选取", "取消")
                .show(new AvatarStudio.CallBack() {
                    @Override
                    public void callback(final String uri) {
                        // Picasso.with(MainActivity.this).load(new File(uri)).into(mImageView);
                        setAvataor(uri);
                    }
                });
    }

    private void setAvataor(final String uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(uri));
                    new Handler(Looper.getMainLooper())
                            .post(new Runnable() {
                                @Override
                                public void run() {
                                    mImageView.setImageBitmap(bitmap);
                                }
                            });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

package com.cropestate.fielduser.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.cropestate.fielduser.R;

import uk.co.senab.photoview.PhotoViewAttacher;


public class PictureActivity extends AppCompatActivity {
    public static Bitmap profilePicBitmap;

    private ImageView photoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        photoImageView = findViewById(R.id.photoImageView);
        photoImageView.getLayoutParams().width = getWindowManager().getDefaultDisplay().getWidth();
        photoImageView.getLayoutParams().height = getWindowManager().getDefaultDisplay().getHeight();
        photoImageView.setAdjustViewBounds(true);
        //photoImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(photoImageView);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        photoImageView.setImageBitmap(profilePicBitmap);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

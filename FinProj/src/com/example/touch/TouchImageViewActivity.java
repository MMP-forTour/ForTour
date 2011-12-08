package com.example.touch;

import com.finproj.R;
import com.finproj.TouchImageView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class TouchImageViewActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        TouchImageView img = new TouchImageView(this, null);
       // ImageView img = new ImageView(this);
        Bitmap snoop = BitmapFactory.decodeResource(getResources(), R.drawable.cover);
        img.setImageBitmap(snoop);
        img.setMaxZoom(4f);
        setContentView(img);
    }
}
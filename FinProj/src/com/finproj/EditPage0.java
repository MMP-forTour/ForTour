package com.finproj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EditPage0 extends Activity {
	TextView titre;
	private ImageView img;
	ImageView frame;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);          
        setContentView(R.layout.edit_mode);
        findviews();
        Bundle bundlee=this.getIntent().getExtras();
        Bitmap photoc = bundlee.getParcelable("data");
        if(bundlee!=null)
        	img.setImageBitmap(photoc);
        setButtonListener();
        
    }
	private void findviews(){
		titre=(TextView) findViewById(R.id.titre);
        Typeface font=Typeface.createFromAsset(getAssets(),"PEIXE.ttf");
        titre.setTypeface(font);
        img=(ImageView) findViewById(R.id.iv_photo);
	}
	private void setButtonListener(){
	
		
	}
}

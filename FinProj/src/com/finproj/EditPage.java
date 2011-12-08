package com.finproj;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EditPage extends Activity {
	private TextView titre;
	private ImageView img;
	private Button okay;
	private Bitmap cropPhoto, frameBitmap;
	private Drawable frame;
	private Bitmap mergedPhoto, btp;
	private File thisFile;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);          
        setContentView(R.layout.edit_mode1);
        findviews();
        Bundle bundlee=this.getIntent().getExtras();
        
        String tempFile=bundlee.getString("FILE");
        FileInputStream fs = null;
		try {
			fs = new FileInputStream(tempFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        BufferedInputStream bs = new BufferedInputStream(fs);  
        btp = BitmapFactory.decodeStream(bs);        
        cropPhoto=BitmapFactory.decodeStream(bs);       
        if(bundlee!=null)
        	img.setImageBitmap(btp);  
        try {
			bs.close();
			fs.close(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
         
       autoMerge();
        //setButtonListener();     
    }
	private void findviews(){
		titre=(TextView) findViewById(R.id.titre);
        Typeface font=Typeface.createFromAsset(getAssets(),"PEIXE.ttf");
        titre.setTypeface(font);
        img=(ImageView) findViewById(R.id.iv_photo);
        okay=(Button) findViewById(R.id.buttonOK);
        frame = getResources().getDrawable(R.drawable.photo_frame);
        frameBitmap = drawableToBitmap(frame);
	}
	private void setButtonListener(){
		okay.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View arg0) {
            }
        });
	}
	private void autoMerge(){
		mergedPhoto = mergeBitmap(btp);
		
		String extStorage = Environment.getExternalStorageDirectory().toString();
		 File file = new File(extStorage, "/bluetooth/myFile1.png");
		 thisFile=file;
 
		 try {
			 OutputStream outStream = new FileOutputStream(file);
			 mergedPhoto.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			 outStream.flush();
			 outStream.close();
			 Toast.makeText(EditPage.this, extStorage+"/bluetooth/myFile1.png", Toast.LENGTH_LONG).show();
			 
			 FileInputStream fs = null;
				try {
					fs = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
		        BufferedInputStream bs = new BufferedInputStream(fs);  
		        Bitmap btp = BitmapFactory.decodeStream(bs);		        
		        
		        img.setImageBitmap(btp);  
		        try {
					bs.close();
					fs.close(); 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
		         
		 }
		 catch (FileNotFoundException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
			 Toast.makeText(EditPage.this, "FileNotFound¿ù»~³á"+e.toString(), Toast.LENGTH_LONG).show();
		 }
		 catch (IOException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
			 Toast.makeText(EditPage.this, "IOException¿ù»~³á"+e.toString(),Toast.LENGTH_LONG).show();
		 }
	}
	private Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap.Config c = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap( drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),  c);
		Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
	
    private Bitmap mergeBitmap(Bitmap currentBitmap) {   	
        Bitmap mBmOverlay = Bitmap.createBitmap(frameBitmap.getWidth(), frameBitmap.getHeight(), frameBitmap.getConfig());
        Canvas canvas = new Canvas(mBmOverlay);
        canvas.drawBitmap(currentBitmap, 30, 30, null);
        canvas.drawBitmap(frameBitmap, new Matrix(), null);
        return mBmOverlay;
    }
}

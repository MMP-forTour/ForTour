package com.finproj;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class RotatePhoto extends Activity {
	ImageView img;
	Matrix matrix0;
	public static ImageButton rotateRight;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);               
        setContentView(R.layout.edit_mode0);
        findviews();
        
        //img = new TouchImageView(this, null);
        //ImageView img = new ImageView(this);
        matrix0=new Matrix();
        Bitmap snoop = BitmapFactory.decodeResource(getResources(), R.drawable.cover);
        img.setImageBitmap(snoop);
        img.setImageMatrix(matrix0);
        //img.setMaxZoom(4f);
        //setContentView(img);      
        setButtonListener();
        
        
        
    }
	private void findviews(){
        img=(ImageView) findViewById(R.id.img);
	}
	private void setButtonListener(){
		/*rotateRight.setOnClickListener(new Button.OnClickListener(){
    	   public void onClick(View arg0){
    		   matrix0.postRotate(90);
    	   }
        });	*/	
	}
}

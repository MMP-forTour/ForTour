package com.finproj;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class EditPhoto extends Activity {
	TouchImageView img;
	public static ImageButton rotateRight;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);               
        setContentView(R.layout.edit_mode);
        findviews();
        
        //img = new TouchImageView(this, null);
        //ImageView img = new ImageView(this);
        Bitmap snoop = BitmapFactory.decodeResource(getResources(), R.drawable.cover);
        img.setImageBitmap(snoop);
        img.setMaxZoom(4f);
        //setContentView(img);      
        setButtonListener();
        
        
        
    }
	private void findviews(){
        img=(TouchImageView) findViewById(R.id.img);
       // rotateRight=(ImageButton) findViewById(R.id.imageButton2);
	}
	private void setButtonListener(){
		/*rotateRight.setOnClickListener(new Button.OnClickListener(){
    	   public void onClick(View arg0){
    		TouchImageView.matrix.postRotate(90);
    		img.setImageMatrix(TouchImageView.matrix);   	    
    	   }
        });*/	
	}

}

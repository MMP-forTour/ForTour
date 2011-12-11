package com.finproj;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EditPage extends Activity {
	private TextView titre;
	private ImageView img;
	private Button buttonOK;
	private Bitmap frameBitmap, bm;
	private String bmUriPath;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);          
        setContentView(R.layout.edit_mode1);
        
        findviews();
        setButtonListener();
        
        Bundle b  = this.getIntent().getExtras();
        bmUriPath = b.getString( "FILE" );

        try {
			bm = MediaStore.Images.Media.getBitmap( this.getContentResolver(), Uri.parse( bmUriPath ) );
		} catch (FileNotFoundException e) {
			Toast.makeText( EditPage.this, "File Not Found: " + e.toString(), Toast.LENGTH_LONG ).show();
		} catch (IOException e) {
			Toast.makeText( EditPage.this, "IO Exception: " + e.toString(), Toast.LENGTH_LONG ).show();
		}

        img.setImageBitmap( mergeBitmap( bm ) );
    }
	
	private void findviews(){
		titre = (TextView) findViewById( R.id.titre );
        Typeface font = Typeface.createFromAsset( getAssets(), "PEIXE.ttf" );
        titre.setTypeface( font );
        
        img         = (ImageView) findViewById( R.id.iv_photo );
        buttonOK    = (Button) findViewById( R.id.buttonOK );
        frameBitmap = drawableToBitmap( getResources().getDrawable( R.drawable.photo_frame ) );
	}
	
	private void setButtonListener(){
		buttonOK.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				long rst = FinProj.mDbHelper.ftStoryAdd(	"Title",
															bmUriPath,
															( (EditText) findViewById( R.id.editTextEMStory ) ).getText().toString(),
															( (EditText) findViewById( R.id.editTextEMLocation ) ).getText().toString()
														);
				if( rst == -1 ) Toast.makeText( EditPage.this, "Save story fail.", Toast.LENGTH_LONG ).show();
				else {
					Toast.makeText( EditPage.this, "Save story success.", Toast.LENGTH_LONG ).show();
					finish();
				}
			}
		} );
	}
	
	private Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap.Config c = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap( drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),  c);
		Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
	
    private Bitmap mergeBitmap( Bitmap currentBitmap ) {   	
        Bitmap mBmOverlay = Bitmap.createBitmap( frameBitmap.getWidth(), frameBitmap.getHeight(), frameBitmap.getConfig() );
        Canvas canvas = new Canvas( mBmOverlay );
        canvas.drawBitmap( currentBitmap, 18, 18, null );
        canvas.drawBitmap( frameBitmap, new Matrix(), null );
        return mBmOverlay;
    }
}

package tw.edu.ntu.fortour;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.finproj.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class EditPage extends Activity {
	private ImageView imageViewOPImage;
	private Button buttonOPOK;
	private Bitmap bm;
	private String bmUriPath;
	private ImageUtil imgUtil;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);          
        setContentView( R.layout.one_photo );
        
        imgUtil = new ImageUtil();
        
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

        imageViewOPImage.setImageBitmap( imgUtil.mergeBitmap( bm ) );
    }
	
	private void findviews(){        
		imageViewOPImage  	= (ImageView) findViewById( R.id.imageViewOPImage );
        buttonOPOK    		= (Button) findViewById( R.id.buttonOPOK );
        imgUtil.frameBitmap = imgUtil.drawableToBitmap( getResources().getDrawable( R.drawable.photo_frame ) );
	}
	
	private void setButtonListener(){
		buttonOPOK.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				long rst = ForTour.mDbHelper.ftStoryAdd(	( (EditText) findViewById( R.id.editTextOPTitle ) ).getText().toString(),
															bmUriPath,
															( (EditText) findViewById( R.id.editTextOPStory ) ).getText().toString(),
															( (EditText) findViewById( R.id.editTextOPLocation ) ).getText().toString()
														);
				
				if( rst == -1 ) Toast.makeText( EditPage.this, "Save story fail.", Toast.LENGTH_LONG ).show();
				else {
					Toast.makeText( EditPage.this, "Save story success.", Toast.LENGTH_LONG ).show();
					finish();
				}
			}
		} );
	}
}

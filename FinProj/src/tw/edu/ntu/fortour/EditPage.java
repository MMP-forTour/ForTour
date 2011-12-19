package tw.edu.ntu.fortour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class EditPage extends Activity {
	private ImageView imageViewOPImage;
	private ImageButton buttonOPOK;
	private Bitmap bm;
	private Uri bmUriPath;
	private ImageUtil imgUtil;
	private String mFileName;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);          
        setContentView( R.layout.one_photo );
        
        imgUtil = new ImageUtil();
        
        findviews();
        setButtonListener();
        
        Bundle b  = this.getIntent().getExtras();
        mFileName = b.getString( "FILE" );
        
        bmUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
									   		ForTour.WORK_DIR + "/" + mFileName ) );

        try {
			bm = MediaStore.Images.Media.getBitmap( this.getContentResolver(), bmUriPath );
		} catch (FileNotFoundException e) {
			Toast.makeText( EditPage.this, "File Not Found: " + e.toString(), Toast.LENGTH_LONG ).show();
		} catch (IOException e) {
			Toast.makeText( EditPage.this, "IO Exception: " + e.toString(), Toast.LENGTH_LONG ).show();
		}

        imageViewOPImage.setImageBitmap( imgUtil.mergeBitmap( getResources().getDrawable( R.drawable.photo_frame ), bm ) );
    }
	
	private void findviews(){        
		imageViewOPImage  	= (ImageView) findViewById( R.id.imageViewOPImage );
        buttonOPOK    		= (ImageButton) findViewById( R.id.buttonOPOK );
	}
	
	private void setButtonListener(){
		buttonOPOK.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				long rst = ForTour.mDbHelper.ftStoryAdd(	( (EditText) findViewById( R.id.editTextOPTitle ) ).getText().toString(),
															mFileName,
															( (EditText) findViewById( R.id.editTextOPStory ) ).getText().toString(),
															( (EditText) findViewById( R.id.editTextOPLocation ) ).getText().toString()
														);
				
				if( rst == -1 ) Toast.makeText( EditPage.this, "Save story fail.", Toast.LENGTH_LONG ).show();
				else {
					try {
						FileOutputStream thumbFile = new FileOutputStream(
														new File( Environment.getExternalStorageDirectory(),
																   ForTour.WORK_DIR + "/" + ForTour.THUMB_DIR + "/" + mFileName
														)
													 );
						Bitmap.createScaledBitmap( bm, imgUtil.THUMB_SIZE, imgUtil.THUMB_SIZE, true ).compress( Bitmap.CompressFormat.PNG, 90, thumbFile );
					}
					catch( FileNotFoundException e ) { }
					
					Toast.makeText( EditPage.this, "Save story success.", Toast.LENGTH_LONG ).show();
					finish();
				}
			}
		} );
	}
	
	private void discardStoryImages() {
		ImageUtil.deleteImage( new File( Environment.getExternalStorageDirectory(),
				   			    ForTour.WORK_DIR + "/" + ForTour.THUMB_DIR + "/" + mFileName ) );
		ImageUtil.deleteImage( new File( Environment.getExternalStorageDirectory(),
   			    ForTour.WORK_DIR + "/" + mFileName ) );
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 ) {
			discardStoryImages();
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if( bm != null && bm.isRecycled() ) bm.recycle();
		
		try {
			imgUtil.finalize();
		}
		catch( Throwable e ) { }
	}
}

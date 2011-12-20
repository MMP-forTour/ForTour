package tw.edu.ntu.fortour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
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
	private Button buttonOPRecord;
	private MediaRecorder mMediaRecorder;
	private ProgressDialog mProgressDlg;
	private boolean hasRecord;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);          
        setContentView( R.layout.one_photo );
        
        hasRecord = false;
        
        imgUtil = new ImageUtil();
        
        Bundle b  = this.getIntent().getExtras();
        mFileName = b.getString( "FILE" );
        
        bmUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
									   		ForTour.DIR_WORK + "/" + mFileName ) );

        /* NOTE: Should after all parameters done. eg: mFileName */
        findviews();
        setButtonListener();
        
        try {
			bm = MediaStore.Images.Media.getBitmap( this.getContentResolver(), bmUriPath );
		} catch (FileNotFoundException e) {
			Toast.makeText( EditPage.this, "File Not Found: " + e.toString(), Toast.LENGTH_LONG ).show();
		} catch (IOException e) {
			Toast.makeText( EditPage.this, "IO Exception: " + e.toString(), Toast.LENGTH_LONG ).show();
		}

        imageViewOPImage.setImageBitmap( imgUtil.imageBorderMerge( getResources().getDrawable( R.drawable.photo_frame ), bm ) );
        
        Toast.makeText( EditPage.this, "Long press Record to record media.", Toast.LENGTH_LONG ).show();
    }
	
	private void findviews(){        
		imageViewOPImage  	= (ImageView) findViewById( R.id.imageViewOPImage );
        buttonOPOK    		= (ImageButton) findViewById( R.id.buttonOPOK );
        buttonOPRecord		= (Button) findViewById( R.id.buttonOPRecord );
	}
	
	private void setButtonListener(){
		buttonOPOK.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				long rst = ForTour.mDbHelper.ftStoryAdd(	( (EditText) findViewById( R.id.editTextOPTitle ) ).getText().toString(),
															mFileName,
															( (EditText) findViewById( R.id.editTextOPStory ) ).getText().toString(),
															( (EditText) findViewById( R.id.editTextOPLocation ) ).getText().toString(),
															( ( hasRecord != false ) ? 1 : 0 )
														);
				
				if( rst == -1 ) Toast.makeText( EditPage.this, "Save story fail.", Toast.LENGTH_LONG ).show();
				else {
					try {
						FileOutputStream thumbFile = new FileOutputStream(
														new File( Environment.getExternalStorageDirectory(),
																   ForTour.DIR_WORK + "/" + ForTour.DIR_THUMB + "/" + mFileName
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
		
		buttonOPRecord.setOnTouchListener( new OnTouchListener() {
			File mMediaFile;
			String mMediaFileName = mFileName.replace( ForTour.EXT_PHOTO, ForTour.EXT_RECORD );
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch( event.getAction() ) {
					case MotionEvent.ACTION_DOWN:
						mProgressDlg = ProgressDialog.show( EditPage.this,
															getString( R.string.stringRecording ),
															getString( R.string.stringReleaseToStop ) );
						try {
							mMediaFile = new File( Environment.getExternalStorageDirectory(), ForTour.DIR_WORK + "/" + mMediaFileName );
							
							mMediaRecorder = new MediaRecorder();
							mMediaRecorder.setAudioSource( MediaRecorder.AudioSource.MIC );
							mMediaRecorder.setOutputFormat( MediaRecorder.OutputFormat.THREE_GPP );
							mMediaRecorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB );
							mMediaRecorder.setOutputFile( mMediaFile.getAbsolutePath() );
							
							mMediaRecorder.prepare();
							mMediaRecorder.start();
						}
						catch( IllegalStateException e ) { }
						catch( IOException e ) { }
						break;
					case MotionEvent.ACTION_UP:
						mProgressDlg.dismiss();
						if( mMediaFile != null ) {
							mMediaRecorder.stop();
							mMediaRecorder.release();
							
							hasRecord = true;
							
							Toast.makeText( EditPage.this, "Save media success.", Toast.LENGTH_LONG ).show();
						}
						else {
							Toast.makeText( EditPage.this, "Save media fail.", Toast.LENGTH_LONG ).show();
						}
						break;
					default:
						break;
				}
				
				return true;
			}
		} );
	}
	
	private void discardStoryImages() {
		Util.deleteFile( new File( Environment.getExternalStorageDirectory(),
				   			    ForTour.DIR_WORK + "/" + ForTour.DIR_THUMB + "/" + mFileName ) );
		Util.deleteFile( new File( Environment.getExternalStorageDirectory(),
   			    ForTour.DIR_WORK + "/" + mFileName ) );
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
		
		ImageUtil.freeBitmap( bm );
		
		try {
			imgUtil.finalize();
		}
		catch( Throwable e ) { }
	}
}

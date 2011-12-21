package tw.edu.ntu.fortour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
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
	private String mFileName, mMediaFileName;
	private Button buttonOPRecord, buttonOPLocation;
	private MediaRecorder mMediaRecorder;
	private ProgressDialog mProgressDlg;
	private boolean hasRecord;
	private LocationManager mLocationManager;
	private double locLongitute, locLatitude;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);          
        setContentView( R.layout.one_photo );
        
        hasRecord = false;
        locLongitute = -1;
        locLatitude = -1;
        
        mLocationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        
        imgUtil = new ImageUtil();
        
        Bundle b  = this.getIntent().getExtras();
        mFileName = b.getString( "FILE" );
        
        mMediaFileName = mFileName.replace( ForTour.EXT_PHOTO, ForTour.EXT_RECORD );
        
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
    }
	
	private void findviews(){        
		imageViewOPImage  	= (ImageView) findViewById( R.id.imageViewOPImage );
        buttonOPOK    		= (ImageButton) findViewById( R.id.buttonOPOK );
        buttonOPRecord		= (Button) findViewById( R.id.buttonOPRecord );
        buttonOPLocation	= (Button) findViewById( R.id.buttonOPLocation );
	}
	
	private void setButtonListener(){
		buttonOPOK.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				long rst = ForTour.mDbHelper.ftStoryAdd(	( (EditText) findViewById( R.id.editTextOPTitle ) ).getText().toString(),
															mFileName,
															( (EditText) findViewById( R.id.editTextOPStory ) ).getText().toString(),
															( (EditText) findViewById( R.id.editTextOPLocation ) ).getText().toString(),
															( ( hasRecord != false ) ? 1 : 0 ),
															locLatitude,
															locLongitute
														);
				
				if( rst == -1 ) Toast.makeText( EditPage.this, getString( R.string.stringSaveStoryFail ), Toast.LENGTH_LONG ).show();
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
					
					Toast.makeText( EditPage.this, getString( R.string.stringSaveStorySuccess ), Toast.LENGTH_LONG ).show();
					finish();
				}
			}
		} );
		
		buttonOPRecord.setOnTouchListener( new OnTouchListener() {
			File mMediaFile;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch( event.getAction() ) {
					case MotionEvent.ACTION_DOWN:
						mProgressDlg = ProgressDialog.show( EditPage.this,
															getString( R.string.stringRecording ),
															getString( R.string.stringReleaseButtonToStop ) );
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
							
							AlertDialog.Builder builder = new AlertDialog.Builder( EditPage.this );
							builder.setTitle( getString( R.string.stringSave ) + " " + getString( R.string.stringStoryMedia ) );
							builder.setMessage( getString( R.string.stringNote ) + ": " + getString( R.string.stringHoldDownButtonToRecord ) );
							builder.setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									hasRecord = true;
									Toast.makeText( EditPage.this, "Save media success.", Toast.LENGTH_LONG ).show();
								}
							});
							builder.setNegativeButton( android.R.string.no, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Util.deleteFile( mMediaFile );
									Toast.makeText( EditPage.this, "Discard save.", Toast.LENGTH_LONG ).show();
								}
							});
							
							builder.show();
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
		
		buttonOPLocation.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if( mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ||
					mLocationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER ) ) {
					
					Intent intent = new Intent();
					intent.setClass( EditPage.this, LocationMap.class );
					startActivityForResult( intent, ForTour.LOCATION_MAP_PICK );
				}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder( EditPage.this );
					builder.setTitle( R.string.stringEnableLocationServices );
					builder.setMessage( R.string.stringDoYouWantToEnableIt );
					builder.setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							startActivity( new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS ) );
						}
					} );
					builder.setNegativeButton( android.R.string.no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							Toast.makeText( EditPage.this, getString( R.string.stringServiceCanNotBeUsedNow ), Toast.LENGTH_LONG ).show();
						}
					} );
					builder.show();
				}
			}
		} );
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch( requestCode ) {
			case ForTour.LOCATION_MAP_PICK:
				if( resultCode == Activity.RESULT_OK ) {
					Bundle extras = data.getExtras();
			        if( extras != null ) {
			        	String locLong = extras.getString( LocationMap.KEY_LONGITUDE );
			        	String locLati = extras.getString( LocationMap.KEY_LATITUDE );

			        	try {
				        	if( locLong != null ) locLongitute = Double.parseDouble( locLong ) / 1E6;
				        	if( locLati != null ) locLatitude = Double.parseDouble( locLati ) / 1E6;
			        	}
			        	catch( NumberFormatException nfe ) {}
			        }
			        
			        if( locLatitude == -1 || locLongitute == -1 ) {
			        	locLatitude = -1;
			        	locLongitute = -1;
			        }
				}
				break;
			default:
				break;
		}
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
			Util.deleteFile( new File( Environment.getExternalStorageDirectory(), ForTour.DIR_WORK + "/" + mMediaFileName ) );
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

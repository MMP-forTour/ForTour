package tw.edu.ntu.fortour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EditPage extends Activity {
	private ImageView imageViewOPImage;
	private MediaPlayer mMediaPlayer;
	private EditText editTextOPStory, editTextOPLocation;
	private ImageButton buttonOPOK, buttonOPSticker, buttonOPHelp, buttonOPPlay;
	private Bitmap bm;
	private Uri bmUriPath, mpUriPath;;
	private ImageUtil imgUtil;
	private String mFileName, mMediaFileName, ftID = null;
	private ImageButton buttonOPRecord, buttonOPLocation;
	private MediaRecorder mMediaRecorder;
	private ProgressDialog mProgressDlg;
	private boolean hasRecord;
	private LocationManager mLocationManager;
	private EditText editTextOPDate, editTextOPTime;
	private double locLatitude, locLongitute;
	private int mMoodIndex = 0;
	
	private Date mNowTime = new Date();
	private SimpleDateFormat sdfDate = new SimpleDateFormat( "yyyy/MM/dd" );
	private SimpleDateFormat sdfTime = new SimpleDateFormat( "HH:mm" );

	private final int DATE_DIALOG = 1;      
    private final int TIME_DIALOG = 2;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);     
        setContentView( R.layout.one_photo );
        findviews();
        setButtonListener();
        setDateTimePicker();
        
        hasRecord = false;
        
        locLatitude = -1;
        locLongitute = -1;
        
        mLocationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        
        imgUtil = new ImageUtil();
        
		Bundle extras = getIntent().getExtras();
        if( extras != null ) {
            mFileName = extras.getString( "FILE" );
            ftID = extras.getString("_ID");
        }
        if( ftID!=null ) {
        	Log.i("DBTEST", "ftID = "+ftID);
        	Cursor c = ForTour.mDbHelper.ftStoryFetchByID( ftID );
            c.moveToFirst();
            mFileName = c.getString( 0 );
            mpUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
					 ForTour.DIR_WORK + "/" + mFileName.replace( ForTour.EXT_PHOTO , ForTour.EXT_RECORD ) ) );

            editTextOPStory.setText( c.getString( 1 ) );
            editTextOPLocation.setText( c.getString( 2 ) );
            //textViewOPTime.setText( new Date(Long.parseLong(c.getString( 4 ))).toLocaleString() );

            locLatitude   = c.getDouble( 5 );
            locLongitute  = c.getDouble( 6 );
            mLocationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
            mMoodIndex = c.getInt( 7 );
            buttonOPSticker.setImageResource( ImageUtil.imageMoodFiles[ mMoodIndex ] );
            
            if( c.getInt( 3 ) != 0 ) {
            	buttonOPPlay.setVisibility( View.VISIBLE );
            }
        }
        
        mMediaFileName = mFileName.replace( ForTour.EXT_PHOTO, ForTour.EXT_RECORD );
        
        bmUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
									   		ForTour.DIR_WORK + "/" + mFileName ) );

        /* NOTE: Should after all parameters done. eg: mFileName */
        
        
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
		imageViewOPImage  	= (ImageView)   findViewById( R.id.imageViewOPImage );
        buttonOPOK    		= (ImageButton) findViewById( R.id.buttonOPOK );
        buttonOPRecord		= (ImageButton) findViewById( R.id.buttonOPRecord );
        buttonOPLocation	= (ImageButton) findViewById( R.id.buttonOPLocation );
        buttonOPSticker		= (ImageButton) findViewById( R.id.emotion_sticker );
        buttonOPHelp 		= (ImageButton) findViewById( R.id.ques);
        buttonOPPlay		= (ImageButton) findViewById( R.id.buttonOPPlay);
        editTextOPStory		= (EditText) findViewById( R.id.editTextOPStory );
        editTextOPLocation	= (EditText) findViewById( R.id.editTextOPLocation );
        
	}
	
	private void setButtonListener(){
		/* TODO: Check file exists first. */
        buttonOPPlay.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mMediaPlayer = new MediaPlayer();
				
				mProgressDlg = ProgressDialog.show( EditPage.this, 
													getString( R.string.stringNowPlaying ),
													getString( R.string.stringStoryMedia ) );
				mProgressDlg.setCancelable( true );
				mProgressDlg.setOnCancelListener( new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface arg0) {
						if( mMediaPlayer.isPlaying() ) mMediaPlayer.stop();
					}
				} );
				
				try {
					mMediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
					mMediaPlayer.setDataSource( getApplicationContext(), mpUriPath );;
					mMediaPlayer.prepare();
					mMediaPlayer.start();
					
					mMediaPlayer.setOnCompletionListener( new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							mProgressDlg.dismiss();
							mp.release();
						}
					} );
				}
				catch( Exception e ) {
					Toast.makeText( EditPage.this, "Unable To Play Media: " + e.toString(), Toast.LENGTH_LONG ).show();
				}
			}
		} );
		
		buttonOPOK.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				long rst = ForTour.mDbHelper.ftStoryAdd(	mFileName,
															editTextOPStory.getText().toString(),
															editTextOPLocation.getText().toString(),
															( ( hasRecord != false ) ? 1 : 0 ),
															locLatitude,
															locLongitute,
															mMoodIndex
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
						if(!SetPreference.alreadySet){
							Intent i = new Intent();
							i.setClass(EditPage.this, SetPreference.class);
							Bundle bundle = new Bundle();
							bundle.putString( "FILE", mFileName );
							i.putExtras(bundle);
							startActivity(i);
						}
						else{
							finish();
						}
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
		
		buttonOPSticker.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popdialogue();
			}
		} );
		
		buttonOPHelp.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View arg0){
            	Intent intent = new Intent();
            	intent.setClass( EditPage.this, EditPageInfo.class );
            	startActivity( intent );
            	overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
        	}
        });
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch( requestCode ) {
			case ForTour.LOCATION_MAP_PICK:
				if( resultCode == Activity.RESULT_OK ) {
					Bundle extras = data.getExtras();
			        if( extras != null ) {
			        	String locLati = extras.getString( LocationMap.KEY_LATITUDE );
			        	String locLong = extras.getString( LocationMap.KEY_LONGITUDE );

			        	try {
				        	if( locLati != null ) locLatitude = Double.parseDouble( locLati ) / 1E6;
				        	if( locLong != null ) locLongitute = Double.parseDouble( locLong ) / 1E6;
			        	}
			        	catch( NumberFormatException nfe ) {}
			        }
			        
			        if( locLatitude == -1 || locLongitute == -1 ) {
			        	locLatitude = -1;
			        	locLongitute = -1;
			        }
				}
				break;

			case ForTour.PASS_ONE_PHOTO:
				if( resultCode == Activity.RESULT_OK ) {
					Bundle extras = data.getExtras();
			        if( extras != null ) {
			            mFileName = extras.getString( "FILE" );
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
	
	private void setDateTimePicker(){
    	View.OnClickListener dateBtnListener =   
                new BtnOnClickListener(DATE_DIALOG);  
    	View.OnClickListener timeBtnListener =   
                new BtnOnClickListener(TIME_DIALOG);      
    	editTextOPDate =(EditText) findViewById(R.id.editTextOPDate);
    	editTextOPTime =(EditText) findViewById(R.id.editTextOPTime);
    	
    	editTextOPDate.setText( sdfDate.format( mNowTime ) );
    	editTextOPTime.setText( sdfTime.format( mNowTime ) );
    	
    	editTextOPDate.setOnClickListener(dateBtnListener);
    	editTextOPTime.setOnClickListener(timeBtnListener);
    }
    
    protected Dialog onCreateDialog(int id) {  
        //Get date and time
        Calendar calendar = Calendar.getInstance();
        Dialog dialog = null;  
        switch(id) {  
            case DATE_DIALOG:  
                DatePickerDialog.OnDateSetListener dateListener =   
                    new DatePickerDialog.OnDateSetListener() {  
                        public void onDateSet(DatePicker datePicker,   
                                int year, int month, int dayOfMonth) {  
                            
                             //Calendar starts from month 0, so add 1 to month
                            editTextOPDate.setText(year + "/"+(month+1) + "/" + dayOfMonth );  
                        }  
                    };  
                dialog = new DatePickerDialog(this,  
                        dateListener,  
                        calendar.get(Calendar.YEAR),  
                        calendar.get(Calendar.MONTH),  
                        calendar.get(Calendar.DAY_OF_MONTH));  
                break;  
            case TIME_DIALOG:  
                TimePickerDialog.OnTimeSetListener timeListener =   
                    new TimePickerDialog.OnTimeSetListener() {  
                          
                        public void onTimeSet(TimePicker timerPicker,  
                                int hourOfDay, int minute) {  
                            
                            editTextOPTime.setText(hourOfDay + ":" + minute );  
                        }  
                    };  
                    dialog = new TimePickerDialog(this, timeListener,  
                            calendar.get(Calendar.HOUR_OF_DAY),  
                            calendar.get(Calendar.MINUTE),  
                            false);   //24h or pm/am
                break;  
            default:  
                break;  
        }  
        return dialog;  
    }  

    private class BtnOnClickListener implements View.OnClickListener {  
          
        private int dialogId = 0;   //'0'->no dialog
  
        public BtnOnClickListener(int dialogId) {  
            this.dialogId = dialogId;  
        }  
        public void onClick(View view) {  
            showDialog(dialogId);  
        }  
          
    }
    
    private GridView gridView; 
	  
	ProgressDialog mydialog;

	private void popdialogue(){
		final AlertDialog builder = new AlertDialog.Builder( EditPage.this ).create();
		
	    LayoutInflater inflater = LayoutInflater.from(this);  
	    View selectView = inflater.inflate(R.layout.picture_dialog,(ViewGroup) findViewById(R.id.layout_root));
	    gridView = (GridView) selectView.findViewById(R.id.gridview);  
	    
	    PictureAdapter adapter= new PictureAdapter( ImageUtil.imageMoodTitles , ImageUtil.imageMoodFiles, this);	       
	    gridView.setAdapter(adapter);  
	    gridView.setOnItemClickListener( new OnItemClickListener(){
	    	@Override
	    	public void onItemClick(AdapterView<?> parent, View v, int position, long id){
	    		mMoodIndex = position;
	    		
				ImageButton mMoodButton = (ImageButton) findViewById( R.id.emotion_sticker );
				mMoodButton.setImageResource( ImageUtil.imageMoodFiles[ position ] );
	    		
	    		if( builder != null ) builder.dismiss();
	    	}
	    });
	    
	    builder.setCancelable(false);
	    builder.setTitle( R.string.stringHowAboutYourMood );  
	    builder.setView(selectView);
	    builder.setCancelable(true);
	    builder.show(); 
	}
}

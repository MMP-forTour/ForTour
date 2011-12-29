package tw.edu.ntu.fortour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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
	private EditText editTextOPStory, editTextOPLocation, editTextOPDate, editTextOPTime;
	private ImageButton buttonOPOK, buttonOPSticker, buttonOPHelp, buttonOPRecord, buttonOPLocation;
	private Bitmap bm;
	private Uri bmUriPath;
	private ImageUtil imgUtil;
	private String mFileName, mMediaFileName;
	private MediaRecorder mMediaRecorder;
	private ProgressDialog mProgressDlg;
	private LocationManager mLocationManager;
	private long ftStorySavetime;
	private double locLatitude, locLongitute;
	
	private boolean hasRecord = false;
	private String ftID = null;
	private int mMoodIndex = 0;
	private boolean pastEdit = false;	
	private Date mNowTime = new Date();

	private final int DATE_DIALOG = 1;      
    private final int TIME_DIALOG = 2;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        setContentView( R.layout.one_photo );
        
        findviews();
        
        locLatitude = -1;
        locLongitute = -1;
        
        mLocationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        
        imgUtil = new ImageUtil();
        
		Bundle extras = getIntent().getExtras();
        if( extras != null ) {
            mFileName = extras.getString( "FILE" );
            ftID = extras.getString("_ID");
        }
        
        if( ftID != null ) {
        	pastEdit = true;
        	
        	mLocationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        	
        	Cursor c = ForTour.mDbHelper.ftStoryFetchByID( ftID );
            c.moveToFirst();
            
            mFileName = c.getString( 0 );
            
            editTextOPStory.setText( c.getString( 1 ) );
            editTextOPLocation.setText( c.getString( 2 ) );
            
            ftStorySavetime = c.getLong( 4 );
            locLatitude   = c.getDouble( 5 );
            locLongitute  = c.getDouble( 6 );
            mMoodIndex = c.getInt( 7 );
            
            c.close();
            
            buttonOPSticker.setImageResource( ImageUtil.imageMoodFiles[ mMoodIndex ] );
        }
        
        mMediaFileName = mFileName.replace( ForTour.EXT_PHOTO, ForTour.EXT_RECORD );
        
        bmUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
									   		ForTour.DIR_WORK + "/" + mFileName ) );

        /* NOTE: All the buttons MUST AFTER ALL INITIAL READY, due to UPDATE mode */
        setButtonListener();
        setDateTimePicker();
        
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
        editTextOPStory		= (EditText) findViewById( R.id.editTextOPStory );
        editTextOPLocation	= (EditText) findViewById( R.id.editTextOPLocation );
        
	}
	
	private void setButtonListener(){
		buttonOPOK.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if( !pastEdit ) {
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
							
							Intent i = new Intent();
							i.setClass(EditPage.this, SetPreference.class);
							Bundle bundle = new Bundle();
							bundle.putString( "FILE", mFileName );
							i.putExtras(bundle);
							startActivity(i);
						}
						catch( FileNotFoundException e ) { }
						
						Toast.makeText( EditPage.this, getString( R.string.stringSaveStorySuccess ), Toast.LENGTH_LONG ).show();
						finish();
					}
				}
				else {
					/* TODO: UPDATE mode */
				}
			}
		} );
		
		buttonOPRecord.setOnTouchListener( new OnTouchListener() {
			File mMediaFileTemp, mMediaFile;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch( event.getAction() ) {
					case MotionEvent.ACTION_DOWN:
						mProgressDlg = ProgressDialog.show( EditPage.this,
															getString( R.string.stringRecording ),
															getString( R.string.stringReleaseButtonToStop ) );
						try {
							mMediaFile = new File( Environment.getExternalStorageDirectory(), ForTour.DIR_WORK + "/" + mMediaFileName );
							mMediaFileTemp = new File( Environment.getExternalStorageDirectory(), ForTour.DIR_WORK + "/" + ForTour.DIR_TEMP + "/" + mMediaFileName );
							
							mMediaRecorder = new MediaRecorder();
							mMediaRecorder.setAudioSource( MediaRecorder.AudioSource.MIC );
							mMediaRecorder.setOutputFormat( MediaRecorder.OutputFormat.THREE_GPP );
							mMediaRecorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB );
							mMediaRecorder.setOutputFile( mMediaFileTemp.getAbsolutePath() );
							
							mMediaRecorder.prepare();
							mMediaRecorder.start();
						}
						catch( IllegalStateException e ) { }
						catch( IOException e ) { }
						break;
					case MotionEvent.ACTION_UP:
						mProgressDlg.dismiss();
						if( mMediaFileTemp != null ) {
							mMediaRecorder.stop();
							mMediaRecorder.release();
							
							if( !pastEdit ) {
								AlertDialog.Builder builder = new AlertDialog.Builder( EditPage.this );
								builder.setTitle( getString( R.string.stringSave ) + " " + getString( R.string.stringStoryMedia ) );
								builder.setMessage( getString( R.string.stringNote ) + ": " + getString( R.string.stringHoldDownButtonToRecord ) );
								builder.setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										if( mMediaFileTemp.renameTo( mMediaFile ) ) {
											hasRecord = true;
											Util.deleteFile( mMediaFileTemp );
											Toast.makeText( EditPage.this, "Save media success.", Toast.LENGTH_LONG ).show();
										}
										else {
											Toast.makeText( EditPage.this, "Save media fail", Toast.LENGTH_LONG ).show();
										}
									}
								});
								builder.setNegativeButton( android.R.string.no, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Util.deleteFile( mMediaFileTemp );
										Toast.makeText( EditPage.this, "Discard save", Toast.LENGTH_LONG ).show();
									}
								});
								
								builder.show();
							}
							else {
								/* TODO: update mode */
							}
						}
						else {
							Toast.makeText( EditPage.this, "Save media fail", Toast.LENGTH_LONG ).show();
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
			        	String locName = extras.getString( LocationMap.KEY_LOCNAME );

			        	if( locName != null ) {
			        		editTextOPLocation.setText( locName );
			        	}
			        	
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
	
	private void discardStory() {
		Util.deleteFile( new File( Environment.getExternalStorageDirectory(),
				   			    ForTour.DIR_WORK + "/" + ForTour.DIR_THUMB + "/" + mFileName ) );
		Util.deleteFile( new File( Environment.getExternalStorageDirectory(),
   			    				ForTour.DIR_WORK + "/" + mFileName ) );
		Util.deleteFile( new File( Environment.getExternalStorageDirectory(),
								ForTour.DIR_WORK + "/" + mMediaFileName ) );
	}
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder( EditPage.this );
		builder.setIcon( android.R.drawable.ic_dialog_alert );
		builder.setTitle( android.R.string.dialog_alert_title );
		builder.setMessage( R.string.stringDiscardSavingStory );
		builder.setNegativeButton( android.R.string.no, null );
		builder.setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				if( !pastEdit ) discardStory();
				finish();
			}
		} );
		builder.show();
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
    	
    	if( !pastEdit ) {
	    	editTextOPDate.setText( Util.sdfDate.format( mNowTime ) );
	    	editTextOPTime.setText( Util.sdfTime.format( mNowTime ) );
    	}
    	else {
    		editTextOPDate.setText( Util.sdfDate.format( new Date( ftStorySavetime ) ) );
	    	editTextOPTime.setText( Util.sdfTime.format( new Date( ftStorySavetime ) ) );
    	}
    	
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
                            editTextOPDate.setText( String.format( "%d/%02d/%02d", year, month+1, dayOfMonth) );
                            /* should save selected date */
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
                            
                            editTextOPTime.setText( String.format( "%02d:%02d", hourOfDay, minute) );  
                            /* should save selected time and merge as millisecond */
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

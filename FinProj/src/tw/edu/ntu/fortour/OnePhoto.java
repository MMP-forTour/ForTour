package tw.edu.ntu.fortour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class OnePhoto extends Activity{
	private String ftID, mFileName;
	private Bitmap bm; 
	private ImageUtil imgUtil;
	private Uri bmUriPath, mpUriPath;
	private MediaPlayer mMediaPlayer;
	private ProgressDialog mProgressDlg;
	private double locLatitude, locLongitute;
	private String mMediaFileName, ftStoryLocation;
	
	private TextView textViewOPStory, textViewOPTime, textViewOPLocation;
    private EditText editTextOPStory, editTextOPLocation, editTextOPDate, editTextOPTime;
    private ImageButton buttonOPMood, buttonOPOK, buttonOPRecord, buttonOPLocation, buttonOPPlay, buttonOPHelp;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_photo);
        
        ftID = this.getIntent().getExtras().getString( "_ID" );
        
        imgUtil = new ImageUtil();
        
        Cursor c = ForTour.mDbHelper.ftStoryFetchByID( ftID );
        
        textViewOPStory	   = (TextView) findViewById( R.id.textViewOPStory );
        textViewOPTime	   = (TextView) findViewById( R.id.textViewOPTime );
        textViewOPLocation = (TextView) findViewById( R.id.textViewOPLocation );
        
        editTextOPStory    = (EditText) findViewById( R.id.editTextOPStory );
        editTextOPLocation = (EditText) findViewById( R.id.editTextOPLocation );
        editTextOPDate	   = (EditText) findViewById( R.id.editTextOPDate );
        editTextOPTime     = (EditText) findViewById( R.id.editTextOPTime );
        
        ImageView imageViewOPImage	= (ImageView) findViewById( R.id.imageViewOPImage );
        
        buttonOPOK			= (ImageButton) findViewById( R.id.buttonOPOK );
        buttonOPPlay		= (ImageButton) findViewById( R.id.buttonOPPlay );
        buttonOPRecord		= (ImageButton) findViewById( R.id.buttonOPRecord );
        buttonOPLocation	= (ImageButton) findViewById( R.id.buttonOPLocation );
        buttonOPMood		= (ImageButton) findViewById( R.id.emotion_sticker );
        buttonOPHelp		= (ImageButton) findViewById( R.id.ques );

        mFileName = c.getString( 0 );
        
        mMediaFileName = mFileName.replace( ForTour.EXT_PHOTO , ForTour.EXT_RECORD );
        
        mpUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
			     							 ForTour.DIR_WORK + "/" + mMediaFileName ) );

        textViewOPStory.setText( c.getString( 1 ) );
        
        ftStoryLocation = c.getString( 2 ).trim();
        
        if( !"".equals( ftStoryLocation ) ) textViewOPLocation.setText( "@ " + ftStoryLocation );
        else textViewOPLocation.setText( ftStoryLocation );
        
        Calendar ftStorySaveTime = Calendar.getInstance();
        ftStorySaveTime.setTimeInMillis( c.getLong( 4 ) );
        textViewOPTime.setText( Util.sdfDate.format( ftStorySaveTime.getTime() ) + " " + Util.sdfTime.format( ftStorySaveTime.getTime() ) );

        locLatitude   = c.getDouble( 5 );
        locLongitute  = c.getDouble( 6 );
        
        if( c.getInt( 3 ) != 0 ) {
        	buttonOPPlay.setVisibility( View.VISIBLE );
        }
        
        buttonOPHelp.setVisibility( View.GONE );
        
        textViewOPStory.setVisibility( View.VISIBLE );
        editTextOPStory.setVisibility( View.GONE );
        
        textViewOPLocation.setVisibility( View.VISIBLE );
        editTextOPLocation.setVisibility( View.GONE );
        
        textViewOPTime.setVisibility( View.VISIBLE );
        
        editTextOPDate.setVisibility( View.GONE );
        editTextOPTime.setVisibility( View.GONE );
        
        buttonOPOK.setVisibility( View.GONE );
        
        buttonOPRecord.setVisibility( View.INVISIBLE );
        buttonOPMood.setImageResource( ImageUtil.imageMoodFiles[ c.getInt( 7 ) ] );

        buttonOPPlay.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMediaPlayer = new MediaPlayer();
				
				mProgressDlg = ProgressDialog.show( OnePhoto.this, 
													getString( R.string.stringNowPlaying ),
													getString( R.string.stringStoryMedia ) );
				mProgressDlg.setIcon( android.R.drawable.ic_media_play );
				mProgressDlg.setCancelable( true );
				mProgressDlg.setOnCancelListener( new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface arg0) {
						if( mMediaPlayer.isPlaying() ) mMediaPlayer.stop();
					}
				} );
				
				try {
					mMediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
					mMediaPlayer.setDataSource( getApplicationContext(), mpUriPath );
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
					Toast.makeText( OnePhoto.this, "Unable To Play Media: " + e.getLocalizedMessage(), Toast.LENGTH_LONG ).show();
				}
			}
		} );
        
        buttonOPLocation.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if( locLatitude != -1 && locLongitute != -1 ) {
					Intent i = new Intent();
					Bundle b = new Bundle();
					
					b.putString( LocMap.KEY_LATITUDE, String.valueOf( (int) ( locLatitude * 1E6 ) ) );
					b.putString( LocMap.KEY_LONGITUDE, String.valueOf( (int) ( locLongitute * 1E6 ) ) );
					b.putString( LocMap.KEY_LOCNAME, ftStoryLocation );
					
					i.putExtras( b );
					i.setClass( OnePhoto.this, LocMap.class );
					
					startActivity( i );
				}
				else {
					Toast.makeText( OnePhoto.this, getString( R.string.stringNoLocationInformation ), Toast.LENGTH_LONG ).show();
				}
			}
		} );
        
        try {
        	bmUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
			   									ForTour.DIR_WORK + "/" + mFileName ) );
			bm = MediaStore.Images.Media.getBitmap( this.getContentResolver(), bmUriPath );
		} catch (FileNotFoundException e) {
			Toast.makeText( OnePhoto.this, "File Not Found: " + e.getLocalizedMessage(), Toast.LENGTH_LONG ).show();
		} catch (IOException e) {
			Toast.makeText( OnePhoto.this, "IO Exception: " + e.getLocalizedMessage(), Toast.LENGTH_LONG ).show();
		}
        
		imageViewOPImage.setImageBitmap( imgUtil.imageBorderMerge( getResources().getDrawable( R.drawable.photo_frame ), bm ) );
		
        c.close();
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, getString( R.string.stringEdit ) ).setIcon( android.R.drawable.ic_menu_edit );
        menu.add(0, 1, 1, getString( R.string.stringDelete ) ).setIcon( android.R.drawable.ic_menu_delete );
        //menu.add(0, 2, 2, getString( R.string.stringDetail ) ).setIcon( android.R.drawable.ic_menu_info_details );
        menu.add(0, 3, 3, getString( R.string.stringShare ) ).setIcon( android.R.drawable.ic_menu_share );
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case 0:
            	Intent i0 = new Intent();
				Bundle b = new Bundle();
				
				b.putString("_ID", ftID);
				
				i0.putExtras( b );
				i0.setClass( OnePhoto.this, EditPage.class );
				startActivityForResult( i0, ForTour.EDIT_ONE_PHOTO );
				overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
				
                break;
            case 1:
            	AlertDialog.Builder builder = new AlertDialog.Builder( OnePhoto.this );
				builder.setTitle( android.R.string.dialog_alert_title );
				builder.setMessage( getString( R.string.stringDoYouWantToDeleteIt ) );
				
				builder.setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						boolean rst = ForTour.mDbHelper.ftStoryDelByID( ftID );

						if( !rst ) Toast.makeText( OnePhoto.this, getString( R.string.stringDeleteStoryFail ), Toast.LENGTH_LONG ).show();
						else {
							Util.deleteFile( new File( Environment.getExternalStorageDirectory(),
					   			    					ForTour.DIR_WORK + "/" + ForTour.DIR_THUMB + "/" + mFileName ) );
							Util.deleteFile( new File( Environment.getExternalStorageDirectory(),
					   			    					ForTour.DIR_WORK + "/" + mFileName ) );
							Util.deleteFile( new File( Environment.getExternalStorageDirectory(),
														ForTour.DIR_WORK + "/" + mMediaFileName ) );
							Toast.makeText( OnePhoto.this, getString( R.string.stringDeleteStorySuccess ), Toast.LENGTH_LONG ).show();
							finish();
							overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
						}
					}
				} );
				builder.setNegativeButton( android.R.string.no, null );
				
				builder.show();
                break;
            case 3:
            	share();
                break;
            default:
            	break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch( requestCode ) {
		case ForTour.EDIT_ONE_PHOTO:
			if( resultCode == Activity.RESULT_OK ) {
				// NOTE: Any good idea to refresh?
				Intent i = getIntent();
				i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				finish();
				startActivity( i );
			}
    	}
    }
    
    private void share() {
    	Intent intent = new Intent(android.content.Intent.ACTION_SEND); 
    	intent.setType("image/png");
    	intent.putExtra( Intent.EXTRA_STREAM, bmUriPath ); 
    	startActivity( Intent.createChooser( intent, getString( R.string.stringShare ) ) );
    }
}

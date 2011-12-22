package tw.edu.ntu.fortour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;

import android.app.Activity;
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
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_photo);
        
        ftID = this.getIntent().getExtras().getString( "_ID" );
        
        imgUtil = new ImageUtil();
        
        Cursor c = ForTour.mDbHelper.ftStoryFetchByID( ftID );
        c.moveToFirst();
        
        TextView textViewOPStory	= (TextView) findViewById( R.id.textViewOPStory );
        TextView textViewOPTime		= (TextView) findViewById( R.id.textViewOPTime );
        TextView textViewOPLocation	= (TextView) findViewById( R.id.textViewOPLocation );
        
        EditText editTextOPStory    = (EditText) findViewById( R.id.editTextOPStory );
        EditText editTextOPLocation = (EditText) findViewById( R.id.editTextOPLocation );
        EditText editTextOPDate	    = (EditText) findViewById( R.id.editTextOPDate );
        EditText editTextOPTime     = (EditText) findViewById( R.id.editTextOPTime );
        
        ImageView imageViewOPImage	= (ImageView) findViewById( R.id.imageViewOPImage );
        
        ImageButton buttonOPOK			= (ImageButton) findViewById( R.id.buttonOPOK );
        ImageButton buttonOPPlay		= (ImageButton) findViewById( R.id.buttonOPRecord );
        ImageButton buttonOPLocation	= (ImageButton) findViewById( R.id.buttonOPLocation );
        ImageButton buttonOPMood		= (ImageButton) findViewById( R.id.emotion_sticker );
        ImageButton ques				= (ImageButton) findViewById( R.id.ques );

        mFileName = c.getString( 0 );
        
        mpUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
			     							 ForTour.DIR_WORK + "/" + mFileName.replace( ForTour.EXT_PHOTO , ForTour.EXT_RECORD ) ) );

        textViewOPStory.setText( c.getString( 1 ) );
        textViewOPLocation.setText( "@" + " " + c.getString( 2 ) );
        textViewOPTime.setText( new Date(Long.parseLong(c.getString( 4 ))).toLocaleString() );

        locLatitude   = c.getDouble( 5 );
        locLongitute  = c.getDouble( 6 );
        
        if( c.getInt( 3 ) == 0 ) {
        	buttonOPPlay.setVisibility( View.INVISIBLE );
        }
        ques.setVisibility( View.GONE );
        
        textViewOPStory.setVisibility( View.VISIBLE );
        editTextOPStory.setVisibility( View.GONE );
        
        textViewOPLocation.setVisibility( View.VISIBLE );
        editTextOPLocation.setVisibility( View.GONE );
        
        textViewOPTime.setVisibility( View.VISIBLE );
        
        editTextOPDate.setVisibility( View.GONE );
        editTextOPTime.setVisibility( View.GONE );
        
        buttonOPOK.setVisibility( View.GONE );
        buttonOPMood.setImageResource( ImageUtil.imageMoodFiles[ c.getInt( 7 ) ] );
        
        /* TODO: Check file exists first. */
        buttonOPPlay.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mMediaPlayer = new MediaPlayer();
				
				mProgressDlg = ProgressDialog.show( OnePhoto.this, 
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
					Toast.makeText( OnePhoto.this, "Unable To Play Media: " + e.toString(), Toast.LENGTH_LONG ).show();
				}
			}
		} );
        
        buttonOPLocation.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if( locLatitude != -1 && locLongitute != -1 ) {
					Intent i = new Intent();
					Bundle b = new Bundle();
					
					b.putString( LocationMap.KEY_LATITUDE, String.valueOf( (int) ( locLatitude * 1E6 ) ) );
					b.putString( LocationMap.KEY_LONGITUDE, String.valueOf( (int) ( locLongitute * 1E6 ) ) );
					
					i.putExtras( b );
					i.setClass( OnePhoto.this, LocationMap.class );
					
					startActivity( i );
				}
			}
		} );
        
        try {
        	bmUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
			   									ForTour.DIR_WORK + "/" + mFileName ) );
			bm = MediaStore.Images.Media.getBitmap( this.getContentResolver(), bmUriPath );
		} catch (FileNotFoundException e) {
			Toast.makeText( OnePhoto.this, "File Not Found: " + e.toString(), Toast.LENGTH_LONG ).show();
		} catch (IOException e) {
			Toast.makeText( OnePhoto.this, "IO Exception: " + e.toString(), Toast.LENGTH_LONG ).show();
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
}

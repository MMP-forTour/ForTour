package tw.edu.ntu.fortour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class OnePhoto extends Activity{
	private String ftID;
	private Bitmap bm; 
	private ImageUtil imgUtil;
	private Uri bmUriPath, mpUriPath;
	private MediaPlayer mMediaPlayer;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_photo_view);
        
        ftID = this.getIntent().getExtras().getString( "_ID" );
        
        imgUtil = new ImageUtil();
        
        Cursor c = ForTour.mDbHelper.ftStoryFetchByID( ftID );
        c.moveToFirst();
        
        TextView textViewOPTitle	= (TextView) findViewById( R.id.textViewOPTitle );
        TextView textViewOPStory	= (TextView) findViewById( R.id.textViewOPStory );
        TextView textViewOPTime		= (TextView) findViewById( R.id.textViewOPTime );
        TextView textViewOPLocation	= (TextView) findViewById( R.id.textViewOPLocation );
        
        EditText editTextOPTitle    = (EditText) findViewById( R.id.editTextOPTitle );
        EditText editTextOPStory    = (EditText) findViewById( R.id.editTextOPStory );
        EditText editTextOPLocation = (EditText) findViewById( R.id.editTextOPLocation );
        
        ImageView imageViewOPImage	= (ImageView) findViewById( R.id.imageViewOPImage );
        
        ImageButton buttonOPOK			= (ImageButton) findViewById( R.id.buttonOPOK );
        Button buttonOPPlay				= (Button) findViewById( R.id.buttonOPPlay );
        
        mpUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
			     							 ForTour.DIR_WORK + "/" + c.getString( 2 ).replace( ForTour.EXT_PHOTO , ForTour.EXT_RECORD ) ) );

        /* TODO: Check file exists first. */
        buttonOPPlay.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mMediaPlayer = new MediaPlayer();
				try {
					mMediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
					mMediaPlayer.setDataSource( getApplicationContext(), mpUriPath );;
					mMediaPlayer.prepare();
					mMediaPlayer.start();
				}
				catch( IllegalArgumentException e ) {
					Toast.makeText( OnePhoto.this, "Unable To Play Media: " + e.toString(), Toast.LENGTH_LONG ).show();
				}
				catch( IllegalStateException e ) {
					Toast.makeText( OnePhoto.this, "Unable To Play Media: " + e.toString(), Toast.LENGTH_LONG ).show();
				}
				catch( IOException e ) {
					Toast.makeText( OnePhoto.this, "Unable To Play Media: " + e.toString(), Toast.LENGTH_LONG ).show();
				}
			}
		} );
        
        Typeface font = Typeface.createFromAsset( getAssets(), "PEIXE.ttf" );
        textViewOPTitle.setTypeface( font );
        textViewOPTitle.setTextSize( 30 );
        
        textViewOPTitle.setText( c.getString( 1 ) );
        textViewOPStory.setText( c.getString( 3 ) );
        textViewOPLocation.setText( "@" + " " + c.getString( 4 ) );
        textViewOPTime.setText( new Date(Long.parseLong(c.getString( 6 ))).toLocaleString() );

        textViewOPTitle.setVisibility( View.VISIBLE );
        editTextOPTitle.setVisibility( View.GONE );
        
        textViewOPStory.setVisibility( View.VISIBLE );
        editTextOPStory.setVisibility( View.GONE );
        
        textViewOPLocation.setVisibility( View.VISIBLE );
        editTextOPLocation.setVisibility( View.GONE );
        
        textViewOPTime.setVisibility( View.VISIBLE );
        
        buttonOPOK.setVisibility( View.GONE );
        
        try {
        	bmUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
			   									ForTour.DIR_WORK + "/" + c.getString( 2 ) ) );
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
    	
    	if( mMediaPlayer != null ) mMediaPlayer.release();
    	
    	try {
			imgUtil.finalize();
		}
    	catch( Throwable e ) { }
    }
}

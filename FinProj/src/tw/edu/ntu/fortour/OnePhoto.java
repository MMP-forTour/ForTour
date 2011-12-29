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
	
	private TextView textViewOPStory, textViewOPTime, textViewOPLocation;
    private EditText editTextOPStory, editTextOPLocation, editTextOPDate,
    				 editTextOPTime;
    private ImageButton buttonOPMood, buttonOPOK, buttonOPRecord,
    					buttonOPLocation, buttonOPPlay, buttonOPHelp;
    
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
        
        mpUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
			     							 ForTour.DIR_WORK + "/" + mFileName.replace( ForTour.EXT_PHOTO , ForTour.EXT_RECORD ) ) );

        textViewOPStory.setText( c.getString( 1 ) );
        
        String ftStoryLocation = c.getString( 2 ).trim();
        if( !"".equals( ftStoryLocation ) ) ftStoryLocation = "@ " + ftStoryLocation;
        textViewOPLocation.setText( ftStoryLocation );
        
        Date ftStorySaveTime = new Date( c.getLong( 4 ) ); 
        textViewOPTime.setText( Util.sdfDate.format( ftStorySaveTime ) + " " + Util.sdfTime.format( ftStorySaveTime ) );

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
        
        /* TODO: Check file exists first. */
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
					
					b.putString( LocationMap.KEY_LATITUDE, String.valueOf( (int) ( locLatitude * 1E6 ) ) );
					b.putString( LocationMap.KEY_LONGITUDE, String.valueOf( (int) ( locLongitute * 1E6 ) ) );
					
					i.putExtras( b );
					i.setClass( OnePhoto.this, LocationMap.class );
					
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
        //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
        menu.add(0, 0, 0, getString( R.string.stringEdit ) ).setIcon( android.R.drawable.ic_menu_edit );
        menu.add(0, 1, 1, getString( R.string.stringDelete ) ).setIcon( android.R.drawable.ic_menu_delete );
        menu.add(0, 2, 2, getString( R.string.stringShare ) ).setIcon( android.R.drawable.ic_menu_share );
        menu.add(0, 3, 3, getString( R.string.stringSettings ) ).setIcon( android.R.drawable.ic_menu_preferences );
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i = new Intent();
    	
    	//依據itemId來判斷使用者點選哪一個item
        switch(item.getItemId()) {
            case 0:
				Bundle b = new Bundle();
				
				b.putString("_ID", ftID);
				b.putString("FILE", mFileName);
				
				i.putExtras( b );
				i.setClass( OnePhoto.this, EditPage.class );
				startActivity(i);
				//startActivityForResult( i, ForTour.EDIT_ONE_PHOTO );
                break;
            case 1:

                break;
            case 2:
            	share();
                break;
            case 3:
            	i.setClass( OnePhoto.this, SetPreference.class );				
				startActivity( i );
                break;
            default:
            	break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void share() {
    	Intent intent = new Intent(android.content.Intent.ACTION_SEND); 
    	intent.setType("image/png");
    	intent.putExtra( Intent.EXTRA_STREAM, bmUriPath ); 
    	startActivity( Intent.createChooser( intent, getString( R.string.stringShare ) ) );
    }
    
}

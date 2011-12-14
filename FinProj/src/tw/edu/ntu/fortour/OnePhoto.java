package tw.edu.ntu.fortour;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import com.finproj.R;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class OnePhoto extends Activity{
	private String ftID;
	private Bitmap bm; 
	private ImageUtil imgUtil;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_photo);
        
        ftID = this.getIntent().getExtras().getString( "_ID" );
        
        imgUtil = new ImageUtil();
        imgUtil.frameBitmap = imgUtil.drawableToBitmap( getResources().getDrawable( R.drawable.photo_frame ) );
        
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
        
        Button buttonOPOK			= (Button) findViewById( R.id.buttonOPOK );
        
        Typeface font = Typeface.createFromAsset( getAssets(), "PEIXE.ttf" );
        textViewOPTitle.setTypeface( font );
        textViewOPTitle.setTextSize( 30 );
        
        textViewOPTitle.setText( c.getString( 1 ) );
        textViewOPStory.setText( c.getString( 3 ) );
        textViewOPLocation.setText( "@" + " " + c.getString( 4 ) );
        textViewOPTime.setText( new Date(Long.parseLong(c.getString( 5 ))).toLocaleString() );

        textViewOPTitle.setVisibility( View.VISIBLE );
        editTextOPTitle.setVisibility( View.GONE );
        
        textViewOPStory.setVisibility( View.VISIBLE );
        editTextOPStory.setVisibility( View.GONE );
        
        textViewOPLocation.setVisibility( View.VISIBLE );
        editTextOPLocation.setVisibility( View.GONE );
        
        textViewOPTime.setVisibility( View.VISIBLE );
        
        buttonOPOK.setVisibility( View.GONE );
        
        try {
			bm = MediaStore.Images.Media.getBitmap( this.getContentResolver(), Uri.parse( c.getString( 2 ) ) );
		} catch (FileNotFoundException e) {
			Toast.makeText( OnePhoto.this, "File Not Found: " + e.toString(), Toast.LENGTH_LONG ).show();
		} catch (IOException e) {
			Toast.makeText( OnePhoto.this, "IO Exception: " + e.toString(), Toast.LENGTH_LONG ).show();
		}

        imageViewOPImage.setImageBitmap( imgUtil.mergeBitmap( bm ) );
        
        c.close();
    }
}

package com.finproj;

import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class OnePhoto extends Activity{
	private String ftID;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_photo);
        
        ftID = this.getIntent().getExtras().getString( "_ID" );
        
        Cursor c = FinProj.mDbHelper.ftStoryFetchByID( ftID );
        c.moveToFirst();
        
        EditText edittextOPStory    = (EditText) findViewById( R.id.edittextOPStory );
        TextView edittextOPLocation = (TextView) findViewById( R.id.editTextOPLocation );
        TextView edittextOPTime     = (TextView) findViewById( R.id.editTextOPTime );
        
        edittextOPStory.setText( c.getString( 3 ) );
        edittextOPLocation.setText( c.getString( 4 ) );
        edittextOPTime.setText( new Date(Long.parseLong(c.getString( 5 ))).toLocaleString() );
        
        c.close();
    }
}

package com.finproj;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ListPage extends ListActivity {
	private static final int LENGTH_TITLE = 10;
	private static final int LENGTH_STORY = 12;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_mode);
        
        updateListView();
        
		ListView listView = getListView();
		listView.setTextFilterEnabled( true );
		listView.setOnItemClickListener( ftStoryClick );
    }
    
    private OnItemClickListener ftStoryClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Cursor selectedItem = (Cursor) parent.getItemAtPosition( position );
			
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			intent.setClass( ListPage.this, OnePhoto.class );
			bundle.putString( "_ID", selectedItem.getString( 0 ) );
			intent.putExtras( bundle );
			startActivity( intent );
		}
	};
	
	private void updateListView() {
		Cursor c = FinProj.mDbHelper.ftStoryFetchAll();
		if( c.getCount() != 0 ) { 
	        startManagingCursor( c );
	        
	        String[] from = new String[] {
	        		DbAdapter.KEY_TITLE,
	        		DbAdapter.KEY_STORY,
	        		DbAdapter.KEY_TIME
	        };
			int[] to = new int[] {
					R.id.textViewLMRTitle,
					R.id.textViewLMRStory,
					R.id.textViewLMRTime
			};
			
			SimpleCursorAdapter contacts = new mySimpleCursorAdaptor( this, R.layout.list_mode_row, c, from, to );
			setListAdapter( contacts );
		}
		else {
			Toast.makeText( ListPage.this, "There is no data now.", Toast.LENGTH_LONG ).show();
		}
	}
	
	public class mySimpleCursorAdaptor extends SimpleCursorAdapter {
		public mySimpleCursorAdaptor(Context context, int layout, Cursor c,
									  String[] from, int[] to) {
			super(context, layout, c, from, to);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			super.bindView(view, context, cursor);
			
			ImageView ftImage   = (ImageView) view.findViewById( R.id.imageViewLMRImage );
			TextView ftTitle    = (TextView) view.findViewById( R.id.textViewLMRTitle );
			TextView ftStroy    = (TextView) view.findViewById( R.id.textViewLMRStory );
			TextView ftTime     = (TextView) view.findViewById( R.id.textViewLMRTime );
			
			try {
				// TODO: A better way to generate thumbnails
				Bitmap bm = MediaStore.Images.Media.getBitmap( ListPage.this.getContentResolver(), Uri.parse( cursor.getString( 2 ) ) );
				ftImage.setImageBitmap( Bitmap.createScaledBitmap( bm, 50, 50, true ) );
			}
			catch (FileNotFoundException e) { }
			catch (IOException e) { }
			
			if( cursor.getString( 1 ).length() > LENGTH_TITLE ) ftTitle.setText( cursor.getString( 1 ).substring( 0, LENGTH_TITLE ) + "..." );
			else ftTitle.setText( cursor.getString( 1 ) );
			
			if( cursor.getString( 3 ).length() > LENGTH_STORY ) ftStroy.setText( cursor.getString( 3 ).substring( 0, LENGTH_STORY ) + "..." );
			else ftStroy.setText( cursor.getString( 3 ) );
			
			ftTime.setText( new Date(Long.parseLong(cursor.getString( 5 ))).toLocaleString() );
		}
	}
}

package tw.edu.ntu.fortour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class ListPage extends ListActivity {
	private static final int LENGTH_TITLE = 5;
	
	protected static final int MENU_EXPORT = Menu.FIRST;
	
	private SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
	
	private Bitmap bm;
	
	private Uri bmUriPath;

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
	
	public boolean onCreateOptionsMenu( Menu menu ) {
		super.onCreateOptionsMenu(menu);
		
		menu.add( 0, MENU_EXPORT, 0, "Export Mode" );
		
		return true;
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch( item.getItemId() ) {
			case MENU_EXPORT:
				CheckBox checkBoxLMRCheckbox = (CheckBox) findViewById( R.id.checkBoxLMRCheckbox );
				if( checkBoxLMRCheckbox.getVisibility() == View.VISIBLE ) {
					checkBoxLMRCheckbox.setVisibility( View.INVISIBLE );
					item.setTitle( "Export Mode" );
				}
				else {
					checkBoxLMRCheckbox.setVisibility( View.VISIBLE );
					item.setTitle( "List Mode" );
				}
				break;
		}
		
		return true;
	}
	
	private void updateListView() {
		Cursor c = ForTour.mDbHelper.ftStoryFetchAll();
		startManagingCursor( c );
		if( c.getCount() != 0 ) { 	        
	        String[] from = new String[] {
	        		DbAdapter.KEY_IMAGE,
	        		DbAdapter.KEY_TITLE,
	        		DbAdapter.KEY_TIME
	        };
			int[] to = new int[] {
					R.id.imageViewLMRImage,
					R.id.textViewLMRTitle,
					R.id.textViewLMRTime
			};
			
			SimpleCursorAdapter contacts = new SimpleCursorAdapter( this, R.layout.list_mode_row, c, from, to );
			contacts.setViewBinder( new ViewBinder() {
				
				@Override
				public boolean setViewValue(View view, Cursor cursor, int index) {
					ImageView ftImage   = (ImageView) view.findViewById( R.id.imageViewLMRImage );
					TextView ftTitle    = (TextView) view.findViewById( R.id.textViewLMRTitle );
					TextView ftTime     = (TextView) view.findViewById( R.id.textViewLMRTime );
					
					switch( view.getId() ) {
						case R.id.imageViewLMRImage:
							try {
								// First Try Thumb
								bmUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
																	ForTour.DIR_WORK + "/" + ForTour.DIR_THUMB + "/" + cursor.getString( 2 ) ) );
								bm = MediaStore.Images.Media.getBitmap( ListPage.this.getContentResolver(), bmUriPath );
								ftImage.setImageBitmap( bm );
							}
							catch (FileNotFoundException e) {
								// Second Try original image
								try {
									bmUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
																		ForTour.DIR_WORK + "/" + cursor.getString( 2 ) ) );
									bm = MediaStore.Images.Media.getBitmap( ListPage.this.getContentResolver(), bmUriPath );
									ftImage.setImageBitmap( bm );
								}
								catch (FileNotFoundException e1) { }
								catch (IOException e1) { }
							}
							catch (IOException e) { }
							break;
						case R.id.textViewLMRTitle:
							if( cursor.getString( 1 ).length() > LENGTH_TITLE ) ftTitle.setText( cursor.getString( 1 ).substring( 0, LENGTH_TITLE ) + "..." );
							else ftTitle.setText( cursor.getString( 1 ) );
							break;
						case R.id.textViewLMRTime:
							ftTime.setText( sdf.format( new Date( Long.parseLong( cursor.getString( 6 ) ) ) ) );
							break;
						default:
							break;
					}
					return true;
				}
			});
			setListAdapter( contacts );
		}
		else {
			Toast.makeText( ListPage.this, "There is no data now.", Toast.LENGTH_LONG ).show();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		ImageUtil.freeBitmap( bm );
	}
}

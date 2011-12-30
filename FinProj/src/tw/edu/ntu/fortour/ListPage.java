package tw.edu.ntu.fortour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class ListPage extends ListActivity {
	private static final int LENGTH_TITLE = 5;
	private static final int LOAD_LIMIT   = 8;
	
	private Bitmap bm;
	private Uri bmUriPath;
	private ListView mListView;
	private Cursor mCursor;
	private SimpleCursorAdapter mSimpleCursorAdapter;
	
	private int loadAmount			= LOAD_LIMIT;
	private boolean noMoreData		= false;
	private boolean inLoading		= false;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_mode);
        
        updateListView();
        
		mListView = getListView();
		mListView.setTextFilterEnabled( true );
		mListView.setOnScrollListener( ftStoryScroll );
		mListView.setOnItemClickListener( ftStoryClick );
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
			/* need update if there is edit */
		}
	};
	
	private OnScrollListener ftStoryScroll = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) { }
		
		@Override
		public void onScroll(	AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if( firstVisibleItem + visibleItemCount == totalItemCount ) {
				if( !noMoreData ) {
					if( !inLoading ) {
						( new asycLoading() ).execute();
					}
				}
			}
		}
	};
	
	private void updateListView() {
		mCursor = ForTour.mDbHelper.ftStoryFetchPartial( loadAmount );
		startManagingCursor( mCursor );
		
		if( mCursor.getCount() != 0 ) { 	        
	        String[] from = new String[] {
	        		DbAdapter.KEY_IMAGE,
	        		DbAdapter.KEY_STORY,
	        		DbAdapter.KEY_STORYTIME
	        };
			int[] to = new int[] {
					R.id.imageViewLMRImage,
					R.id.textViewLMRStory,
					R.id.textViewLMRTime
			};
			
			if( loadAmount == LOAD_LIMIT ) {
				mSimpleCursorAdapter = new SimpleCursorAdapter( this, R.layout.list_mode_row, mCursor, from, to );
				mSimpleCursorAdapter.setViewBinder( mViewBinder );
				setListAdapter( mSimpleCursorAdapter );
			}
			else {
				mSimpleCursorAdapter.changeCursor( mCursor );
				mSimpleCursorAdapter.notifyDataSetChanged();
			}
			
			if( loadAmount > mCursor.getCount() ) noMoreData = true;
		}
		else {
			Toast.makeText( ListPage.this, getString( R.string.stringThereIsNoDataNow ), Toast.LENGTH_LONG ).show();
		}
	}
	
	private class asycLoading extends AsyncTask<Void, Void, Void> {
		ProgressDialog mProgressDialog = new ProgressDialog( ListPage.this );
		
		@Override
		protected Void doInBackground(Void... params) {
			runOnUiThread( new Runnable() {
				public void run() {
					updateListView();
				}
			} );
			return null;
		}
	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			loadAmount += LOAD_LIMIT;

			mProgressDialog.setCancelable( true );
			mProgressDialog.setTitle( R.string.stringLoading );
			mProgressDialog.setMessage( getString( R.string.stringPleaseWait ) );
			mProgressDialog.show();
			
			inLoading = true;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			if( mProgressDialog != null ) mProgressDialog.dismiss();
			
			inLoading = false;
		}
	}
	
	private ViewBinder mViewBinder = new ViewBinder() {
		
		@Override
		public boolean setViewValue(View view, Cursor cursor, int index) {
			ImageView ftImage   = (ImageView) view.findViewById( R.id.imageViewLMRImage );
			TextView ftStory    = (TextView) view.findViewById( R.id.textViewLMRStory );
			TextView ftTime     = (TextView) view.findViewById( R.id.textViewLMRTime );
			LinearLayout ftLayout = (LinearLayout) ((View)view.getParent()).findViewById(R.id.ftLinearLayout);
			
			if (((View) view.getParent()).getId() == R.id.ftLinearLayout) {
				int colorPos = cursor.getPosition() % 2;
				if (colorPos == 0)
					ftLayout.setBackgroundResource(R.drawable.listitem_r);
				else
					ftLayout.setBackgroundResource(R.drawable.listitem_g);
			}
			
			switch( view.getId() ) {
				case R.id.imageViewLMRImage:
					try {
						// First Try Thumb
						bmUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
															ForTour.DIR_WORK + "/" + ForTour.DIR_THUMB + "/" + cursor.getString( 1 ) ) );
						bm = MediaStore.Images.Media.getBitmap( ListPage.this.getContentResolver(), bmUriPath );
						ftImage.setImageBitmap( bm );
					}
					catch (FileNotFoundException e) {
						// Second Try original image
						try {
							bmUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
																ForTour.DIR_WORK + "/" + cursor.getString( 1 ) ) );
							bm = MediaStore.Images.Media.getBitmap( ListPage.this.getContentResolver(), bmUriPath );
							ftImage.setImageBitmap( bm );
						}
						catch (FileNotFoundException e1) { }
						catch (IOException e1) { }
						finally {
							ImageUtil.freeBitmap( bm );
						}
					}
					catch (IOException e) { }
					finally {
						ImageUtil.freeBitmap( bm );
					}
					break;
				case R.id.textViewLMRStory:
					if( cursor.getString( 2 ).length() > LENGTH_TITLE ) ftStory.setText( cursor.getString( 2 ).substring( 0, LENGTH_TITLE ) + "..." );
					else ftStory.setText( cursor.getString( 2 ) );
					break;
				case R.id.textViewLMRTime:
					ftTime.setText( Util.sdfDate.format( Util.setCalendarInMSec( cursor.getLong( 3 ) ).getTime() ) );
					break;

				default:
					break;
			}
			return true;
		}
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		ImageUtil.freeBitmap( bm );
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
        /*menu.add(0, 0, 0, getString( R.string.stringEdit ) ).setIcon( android.R.drawable.ic_menu_edit );
        menu.add(0, 1, 1, getString( R.string.stringShare ) ).setIcon( android.R.drawable.ic_menu_share );
        menu.add(0, 2, 2, getString( R.string.stringSettings ) ).setIcon( android.R.drawable.ic_menu_preferences );*/
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //依據itemId來判斷使用者點選哪一個item
        switch(item.getItemId()) {
            case 0://Edit, 選擇多個刪除
            	
                break;
            case 1://Share, 選擇多個share...?

                break;
            case 2://Setting
            	Intent i = new Intent();
            	i.setClass( ListPage.this, SetPreference.class );				
				startActivity( i );
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }


}

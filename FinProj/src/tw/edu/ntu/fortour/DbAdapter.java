package tw.edu.ntu.fortour;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbAdapter {
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private final Context mCtx;
	
	private static final String DATABASE_NAME		= "fortour.db";
	private static final String DATABASE_TABLE	= "ftdata";
	private static final int DATABASE_VERSION		= 1;
	
	public static final String KEY_ROWID		= "_id";
	public static final String KEY_TITLE		= "ftTitle";
	public static final String KEY_IMAGE		= "ftImage";
	public static final String KEY_STORY		= "ftStory";
	public static final String KEY_LOCATION	= "ftLocation";
	public static final String KEY_HAS_RECORD	= "ftHasRecord";
	public static final String KEY_TIME		= "ftTime";
	public static final String KEY_LATITUDE	= "ftLatitude";
	public static final String KEY_LONGITUDE	= "ftLongitude";
	
	private static final String DATABASE_CREATE =
			"CREATE TABLE " + DATABASE_TABLE + " ( " +
					KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_TITLE + " TEXT NULL, " +
					KEY_IMAGE + " TEXT NOT NULL, " +
					KEY_STORY + " TEXT NULL," +
					KEY_LOCATION + " TEXT NULL," +
					KEY_HAS_RECORD + " INTEGER NOT NULL," +
					KEY_TIME + " LONG," +
					KEY_LATITUDE + " DOUBLE NULL," +
					KEY_LONGITUDE + " DOUBLE NULL" +
					" );";
	
	private static final String DATABASE_UPGRADE =
			"DROP TABLE IF EXISTS " + DATABASE_TABLE + ";";
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper( Context context ) {
			super( context, DATABASE_NAME, null, DATABASE_VERSION );
		}

		@Override
		public void onCreate( SQLiteDatabase db ) {
			db.execSQL( DATABASE_CREATE );
		}

		@Override
		public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
			db.execSQL( DATABASE_UPGRADE );
			onCreate( db );
		}
	}
	
	public DbAdapter( Context ctx ) {
		this.mCtx = ctx;
	}
	
	public DbAdapter open() throws SQLException {
		mDbHelper	= new DatabaseHelper( mCtx );
		mDb			= mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		mDbHelper.close();
	}
	
	public long ftStoryAdd( final String ftTitle, final String ftImage,
							 final String ftStory, final String ftLocation,
							 final int ftHasRecord, final double ftLatitude,
							 final double ftLongitude ) {
		
		ContentValues initValues = new ContentValues();
		initValues.put( KEY_TITLE, ftTitle );
		initValues.put( KEY_IMAGE, ftImage );
		initValues.put( KEY_STORY, ftStory );
		initValues.put( KEY_LOCATION, ftLocation );
		initValues.put( KEY_HAS_RECORD, ftHasRecord );
		initValues.put( KEY_TIME, ( new Date() ).getTime() );
		initValues.put( KEY_LATITUDE, ftLatitude );
		initValues.put( KEY_LONGITUDE, ftLongitude );
		
		return mDb.insert( DATABASE_TABLE, null, initValues );
	}
	
	public boolean ftStoryDel( final long contactId ) {
		return ( mDb.delete( DATABASE_TABLE, KEY_ROWID + "=" + contactId, null ) > 0 );
	}
	
	public Cursor ftStoryFetchAll() {
		return mDb.query(	DATABASE_TABLE , 
							new String[] { KEY_ROWID, KEY_TITLE, KEY_IMAGE, KEY_STORY, KEY_LOCATION, KEY_HAS_RECORD, KEY_TIME },
							null, null, null, null, KEY_ROWID + " DESC" );
	}
	
	public Cursor ftStoryFetchByID( final String ftID ) {
		return mDb.query(	DATABASE_TABLE , 
							new String[] { KEY_ROWID, KEY_TITLE, KEY_IMAGE, KEY_STORY, KEY_LOCATION, KEY_HAS_RECORD, KEY_TIME, KEY_LATITUDE, KEY_LONGITUDE },
							"_id=?",
							new String[] { ftID },
							null, null, null );
	}
}

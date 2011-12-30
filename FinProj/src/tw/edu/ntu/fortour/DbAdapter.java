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
	public static final String KEY_IMAGE		= "ftImage";
	public static final String KEY_STORY		= "ftStory";
	public static final String KEY_LOCATION	= "ftLocation";
	public static final String KEY_HAS_RECORD	= "ftHasRecord";
	public static final String KEY_STORYTIME	= "ftStoryTime";
	public static final String KEY_LATITUDE	= "ftLatitude";
	public static final String KEY_LONGITUDE	= "ftLongitude";
	public static final String KEY_MOODIMAGE	= "ftMoodImage";
	
	private static final String DATABASE_CREATE =
			"CREATE TABLE " + DATABASE_TABLE + " ( " +
					KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_IMAGE + " TEXT NOT NULL, " +
					KEY_STORY + " TEXT NULL," +
					KEY_LOCATION + " TEXT NULL," +
					KEY_HAS_RECORD + " INTEGER NOT NULL," +
					KEY_STORYTIME + " LONG," +
					KEY_LATITUDE + " DOUBLE NULL," +
					KEY_LONGITUDE + " DOUBLE NULL," +
					KEY_MOODIMAGE + " INTEGER DEFAULT 0" +
					" );";

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
			/* TODO: DB upgrade */
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
	
	public long ftStoryAdd( final String ftImage,
							 final String ftStory, final String ftLocation,
							 final int ftHasRecord, final double ftLatitude,
							 final double ftLongitude, final int ftMoodImage ) {
		
		ContentValues initValues = new ContentValues();
		initValues.put( KEY_IMAGE, ftImage );
		initValues.put( KEY_STORY, ftStory );
		initValues.put( KEY_LOCATION, ftLocation );
		initValues.put( KEY_HAS_RECORD, ftHasRecord );
		initValues.put( KEY_LATITUDE, ftLatitude );
		initValues.put( KEY_LONGITUDE, ftLongitude );
		initValues.put( KEY_MOODIMAGE, ftMoodImage );
		
		initValues.put( KEY_STORYTIME, ( new Date() ).getTime() );
		
		return mDb.insert( DATABASE_TABLE, null, initValues );
	}
	
	public boolean ftStoryUpdByID( final String ftID, final String ftImage,
								 final String ftStory, final String ftLocation,
								 final int ftHasRecord, final double ftLatitude,
								 final double ftLongitude, final int ftMoodImage ) {
		
		ContentValues initValues = new ContentValues();
		initValues.put( KEY_IMAGE, ftImage );
		initValues.put( KEY_STORY, ftStory );
		initValues.put( KEY_LOCATION, ftLocation );
		initValues.put( KEY_HAS_RECORD, ftHasRecord );
		initValues.put( KEY_LATITUDE, ftLatitude );
		initValues.put( KEY_LONGITUDE, ftLongitude );
		initValues.put( KEY_MOODIMAGE, ftMoodImage );
		
		return ( mDb.update( DATABASE_TABLE, initValues, KEY_ROWID + "=?", new String[] { ftID } ) > 0  );
	}
	
	public boolean ftStoryDelByID( final String ftID ) {
		return ( mDb.delete( DATABASE_TABLE, KEY_ROWID + "=?", new String[] { ftID } ) > 0 );
	}
	
	public Cursor ftStoryFetchAll() {
		return mDb.query(	DATABASE_TABLE , 
							new String[] { KEY_ROWID, KEY_IMAGE, KEY_STORY, KEY_STORYTIME },
							null, null, null, null, KEY_ROWID + " DESC" );
	}
	
	public Cursor ftStoryFetchPartial( final int amount ) {
		final String queryString =	"SELECT " + KEY_ROWID + ", " + KEY_IMAGE + ", " + KEY_STORY + ", " + KEY_STORYTIME + " " +
									"FROM " + DATABASE_TABLE + " " +
									"ORDER BY " + KEY_ROWID + " DESC " +
									"LIMIT 0, " + amount + " "; 
		
		return mDb.rawQuery( queryString , null );
	}
	
	public Cursor ftStoryFetchByID( final String ftID ) {
		Cursor cursor =  mDb.query(	DATABASE_TABLE , 
									new String[] { KEY_IMAGE, KEY_STORY,
												   KEY_LOCATION, KEY_HAS_RECORD, KEY_STORYTIME, 
												   KEY_LATITUDE, KEY_LONGITUDE, KEY_MOODIMAGE },
									KEY_ROWID + "=?",
									new String[] { ftID },
									null, null, null );
		
		if( cursor != null ) cursor.moveToFirst();
		
		return cursor;
	}
}

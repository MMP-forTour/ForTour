package tw.edu.ntu.fortour;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class Util {
	protected static SimpleDateFormat sdfDate   = new SimpleDateFormat( "yyyy/MM/dd" );
	protected static SimpleDateFormat sdfTime   = new SimpleDateFormat( "HH:mm" );
	protected static SimpleDateFormat sdfString = new SimpleDateFormat( "yyyy/MM/dd HH:mm" );
	
	public static long datetimeStringToMSec( final String datePart, final String timePart ) {
		try {
			Date date = Util.sdfString.parse( datePart + " " + timePart );
			return ( date.getTime() );
		}
		catch( Exception e ) {
			return ( ( new Date() ).getTime() );
		}
	}
	
	public static boolean checkFile( final File file ) {
		return file.exists();
	}
	
	public static void deleteFile( File file ) {
		if( checkFile( file ) ) file.delete(); 
	}
	
	public static boolean isOnline( final Object obj ) {
		ConnectivityManager cm = (ConnectivityManager) obj;
		NetworkInfo ni = cm.getActiveNetworkInfo();
		
		if( ni != null && ni.isConnectedOrConnecting() ) return true;
		
		return false;
	}
	
	protected class asycIntentProgress extends AsyncTask<Object, Integer, Void> {
		private ProgressDialog mProgressDialog;
		private Context mContext;
		private String mTitle, mMessage;
		
		public asycIntentProgress( Context ctx, final String title, final String message ) {
			mContext = ctx;
			mTitle   = title;
			mMessage = message;
			mProgressDialog = new ProgressDialog( ctx );
		}
		
		@Override
		protected Void doInBackground(Object... obj) {
			mContext.startActivity( (Intent)( obj[0] ) );
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			mProgressDialog = new ProgressDialog( mContext );
			mProgressDialog.setTitle( mTitle );
			mProgressDialog.setMessage( mMessage );
			mProgressDialog.show();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			if( mProgressDialog != null ) mProgressDialog.dismiss();
		}
	}
}

package tw.edu.ntu.fortour;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class Util {
	public static void deleteFile( File file ) {
		if( file.exists() ) file.delete(); 
	}
	
	public static boolean isOnline( final Object obj ) {
		ConnectivityManager cm = (ConnectivityManager) obj;
		NetworkInfo ni = cm.getActiveNetworkInfo();
		
		if( ni != null && ni.isConnectedOrConnecting() ) return true;
		
		return false;
	}
	
	protected class asycTaskProgress extends AsyncTask<Void, Integer, Void> {
		protected ProgressDialog mProgressDialog;
		
		@Override
		protected Void doInBackground(Void... arg0) {
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			if( mProgressDialog != null ) mProgressDialog.dismiss();
		}
	}
}

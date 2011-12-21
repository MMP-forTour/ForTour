package tw.edu.ntu.fortour;

import java.io.File;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
}

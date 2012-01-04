package tw.edu.ntu.fortour;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Util {
	protected static SimpleDateFormat sdfDate   = new SimpleDateFormat( "yyyy/MM/dd" );
	protected static SimpleDateFormat sdfTime   = new SimpleDateFormat( "HH:mm" );
	protected static SimpleDateFormat sdfString = new SimpleDateFormat( "yyyy/MM/dd HH:mm" );
	
	public static long datetimeStringToMSec( final String datePart, final String timePart ) {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime( Util.sdfString.parse( datePart + " " + timePart ) );
			return ( calendar.getTimeInMillis() );
		}
		catch( Exception e ) {
			return ( Calendar.getInstance().getTimeInMillis() );
		}
	}
	
	public static Calendar setCalendarInMSec( final long timeInMSec ) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis( timeInMSec );
		return cal;
	}
	
	public static String getFileName( final String fileExt ) {
		return String.valueOf(System.currentTimeMillis()) + fileExt;
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
}

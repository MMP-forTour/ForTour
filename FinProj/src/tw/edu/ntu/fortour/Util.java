package tw.edu.ntu.fortour;

import java.io.File;

public class Util {
	public static void deleteFile( File file ) {
		if( file.exists() ) file.delete(); 
	}
}

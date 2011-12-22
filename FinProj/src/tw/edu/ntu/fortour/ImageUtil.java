package tw.edu.ntu.fortour;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class ImageUtil {
	protected Bitmap imageBorderBitmap, imageBorderDrawable, imageBorderOverlay;
	
	private final int imageBorderWidth;
	private final int imageBorderHeight;
	private final int imageBorderAnchorLeft;
	private final int imageBorderAnchorTop;
	
	protected int THUMB_SIZE = 72;
	
	protected static int[] imageMoodFiles = new int[] {    
		R.drawable.no_mood,
		R.drawable.pic1, R.drawable.pic2, R.drawable.pic3,   
	    R.drawable.pic4, R.drawable.pic5, R.drawable.pic6,   
	    R.drawable.pic7, R.drawable.pic8, R.drawable.pic9,
	    R.drawable.pic10, R.drawable.pic11, R.drawable.pic12,   
	    R.drawable.pic13, R.drawable.pic14, R.drawable.pic15,   
	    R.drawable.pic16, R.drawable.pic17
	};
	
	protected static String[] imageMoodTitles = new String[]{
		"no_mood",
		"pic1", "pic2", "pic3", "pic4", "pic5", "pic6", "pic7", "pic8", "pic9"
		, "pic10", "pic11", "pic12", "pic13", "pic14", "pic15", "pic16", "pic17"
	};  
	
	public ImageUtil() {
		/* TODO: A better way to merge border images. */
		imageBorderWidth      = 427;
		imageBorderHeight     = 500;
		imageBorderAnchorLeft = 25;
		imageBorderAnchorTop  = 20;
	}
	
	private Bitmap imageBorderToDrawable( final Drawable drawable ) {
		Bitmap.Config c = ( drawable.getOpacity() != PixelFormat.OPAQUE ) ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		
		try {
			imageBorderDrawable = Bitmap.createBitmap( imageBorderWidth, imageBorderHeight,  c);
			Canvas canvas = new Canvas( imageBorderDrawable );
			drawable.setBounds( 0, 0, imageBorderWidth, imageBorderHeight );
	        drawable.draw( canvas );
		}
		catch( IllegalArgumentException iae ) { }
		
        return imageBorderDrawable;
    }
    
    public Bitmap imageBorderMerge( Drawable drawable, final Bitmap currentBitmap ) {
    	imageBorderBitmap = imageBorderToDrawable( drawable );
    	
    	try {
	        imageBorderOverlay = Bitmap.createBitmap( imageBorderBitmap.getWidth(), imageBorderBitmap.getHeight(), imageBorderBitmap.getConfig() );
	        Canvas canvas = new Canvas( imageBorderOverlay );
	        canvas.drawBitmap( currentBitmap, imageBorderAnchorLeft, imageBorderAnchorTop, null );
	        canvas.drawBitmap( imageBorderBitmap, new Matrix(), null );
    	}
    	catch( IllegalArgumentException iae ) {}
        
    	return imageBorderOverlay;
    }
    
    public static void freeBitmap( Bitmap bitmap ) {
    	if( bitmap != null && bitmap.isRecycled() ) bitmap.recycle();
    }
    
    @Override
    protected void finalize() throws Throwable {
    	super.finalize();
    	
    	freeBitmap( imageBorderBitmap );
    	freeBitmap( imageBorderDrawable );
    	freeBitmap( imageBorderOverlay );
    }
}

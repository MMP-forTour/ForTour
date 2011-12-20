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

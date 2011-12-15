package tw.edu.ntu.fortour;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class ImageUtil {
	protected Bitmap frameBitmap;
	
	private final int frameWidth;
	private final int frameHeight;
	private final int frameInsideLeft;
	private final int frameInsideTop;
	
	protected int THUMB_SIZE = 72;
	
	public ImageUtil() {
		/* TODO: A better way to merge border images. */
		frameWidth      = 427;
		frameHeight     = 500;
		frameInsideLeft = 25;
		frameInsideTop  = 20;
	}
	
	public static void deleteImage( File file ) {
		if( file.exists() ) file.delete(); 
	}
	
	public Bitmap drawableToBitmap( final Drawable drawable ) {
		Bitmap.Config c = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		//Bitmap bitmap = Bitmap.createBitmap( drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),  c);
		Bitmap bitmap = Bitmap.createBitmap( frameWidth, frameHeight,  c);
		Canvas canvas = new Canvas(bitmap);
        //drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.setBounds( 0, 0, frameWidth, frameHeight );
        drawable.draw(canvas);
        return bitmap;
    }
    
    public Bitmap mergeBitmap( Drawable drawable, final Bitmap currentBitmap ) {
    	frameBitmap = drawableToBitmap( drawable );
    	
        Bitmap mBmOverlay = Bitmap.createBitmap( frameBitmap.getWidth(), frameBitmap.getHeight(), frameBitmap.getConfig() );
        Canvas canvas = new Canvas( mBmOverlay );
        canvas.drawBitmap( currentBitmap, frameInsideLeft, frameInsideTop, null );
        canvas.drawBitmap( frameBitmap, new Matrix(), null );
        return mBmOverlay;
    }
    
    @Override
    protected void finalize() throws Throwable {
    	super.finalize();
    	
    	if( frameBitmap != null && frameBitmap.isRecycled() ) frameBitmap.recycle();
    }
}

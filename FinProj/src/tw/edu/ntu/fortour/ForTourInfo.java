package tw.edu.ntu.fortour;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;



public class ForTourInfo extends Activity implements OnTouchListener{
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_info);
        LinearLayout bg = (LinearLayout) findViewById(R.id.main_info);
        bg.setOnTouchListener(this);
        
        TextView version = (TextView) findViewById( R.id.textViewVersion );
        try {
			version.setText( "Version: " + getPackageManager().getPackageInfo( getPackageName(), 0 ).versionName );
		} catch (NameNotFoundException e) {
			version.setVisibility( View.INVISIBLE );
		}
    }

	@Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
	    // TODO Auto-generated method stub
		finish();
		overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
	    return false;
    }
}
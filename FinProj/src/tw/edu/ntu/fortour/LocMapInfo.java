package tw.edu.ntu.fortour;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;



public class LocMapInfo extends Activity implements OnTouchListener{
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loc_map_info);
        LinearLayout bg = (LinearLayout) findViewById(R.id.loc_map_info);
        bg.setOnTouchListener(this);
    }

	@Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
		finish();
		overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
	    return false;
    }
}
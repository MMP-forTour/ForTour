package tw.edu.ntu.fortour;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;



public class EditPageInfo extends Activity implements OnTouchListener{
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_photo_info);
        LinearLayout bg = (LinearLayout) findViewById(R.id.one_photo_info);
        bg.setOnTouchListener(this);
    }

	@Override
    public boolean onTouch(View v, MotionEvent event) {
	    // TODO Auto-generated method stub
		finish();
		overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
	    return false;
    }
}
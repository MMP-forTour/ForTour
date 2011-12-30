package tw.edu.ntu.fortour;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;



public class EditPageInfo extends Activity implements OnTouchListener{
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_photo_info);
        Button close = (Button) findViewById(R.id.btn_close);
        close.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View arg0){
            	onBackPressed();
        	}
        });
    }
 
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    	
    	finish();
		overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
    }

	@Override
    public boolean onTouch(View v, MotionEvent event) {
	    // TODO Auto-generated method stub
		finish();
	    return false;
    }
}
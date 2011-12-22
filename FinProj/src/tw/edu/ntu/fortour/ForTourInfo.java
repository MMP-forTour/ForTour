package tw.edu.ntu.fortour;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class ForTourInfo extends Activity {
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_info);
        Button close = (Button) findViewById(R.id.btn_close);
        close.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View arg0){
            	finish();
            	overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
        	}
        });
    }
 
}
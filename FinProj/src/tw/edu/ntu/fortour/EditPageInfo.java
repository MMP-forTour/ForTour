package tw.edu.ntu.fortour;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class EditPageInfo extends Activity {
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_photo_info);
        Button close = (Button) findViewById(R.id.btn_close);
        close.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View arg0){
            	finish();
        	}
        });
    }
 
}
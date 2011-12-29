package tw.edu.ntu.fortour;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class ForTourInfo extends Activity {
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_info);
        Button close = (Button) findViewById(R.id.btn_close);
        close.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View arg0){
            	onBackPressed();
        	}
        });
        TextView version = (TextView) findViewById( R.id.textViewVersion );
        try {
			version.setText( "Version: " + getPackageManager().getPackageInfo( getPackageName(), 0 ).versionName );
		} catch (NameNotFoundException e) {
			version.setVisibility( View.INVISIBLE );
		}
    }
 
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    	
    	finish();
		overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
    }
}
package tw.edu.ntu.fortour;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

public class SetPreference extends PreferenceActivity 
implements OnPreferenceChangeListener, OnPreferenceClickListener {
	static final String TAG = "Settings";
	static final String SYNC_DROPBOX = "sync_db";
	
	final static private String APP_KEY = "t80g3k4j17kbyjb";
	final static private String APP_SECRET = "g2f6spmfn1kazv5";

	// If you'd like to change the access type to the full Dropbox instead of
	// an app folder, change this value.
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	
    // You don't need to change these, leave them alone.
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    //final static private String LOGIN_STATUS = "LogInStatus";
    //final static private String LOGIN_PREFS_NAME = "mLoggedIn";

    static DropboxAPI<AndroidAuthSession> mApi;
    //private boolean mLoggedIn;
    private final static String PHOTO_DIR = "/Photos/"; 
    private ImageButton btn_done;
    CheckBoxPreference sync_db;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        
        // Basic Android widgets
        addPreferencesFromResource(R.xml.settings);
        setContentView(R.layout.settings_main);
        sync_db = (CheckBoxPreference)findPreference(SYNC_DROPBOX); 
        sync_db.setOnPreferenceChangeListener(this);  
        sync_db.setOnPreferenceClickListener(this); 
        btn_done = (ImageButton) findViewById(R.id.btn_done);
        
        checkAppKeySetup();
        
        // Display the proper UI state if logged in or not
        //setLoggedIn(mApi.getSession().isLinked());
        //Log.i("btn_done_before", "login="+mApi.getSession().isLinked());
        
        btn_done.setOnClickListener(new View.OnClickListener() {	
			public void onClick(View v) {
				finish();
			}
		});
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = mApi.getSession();

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();
                
                // Store it locally in our app for later use
                TokenPair tokens = session.getAccessTokenPair();
                storeKeys(tokens.key, tokens.secret);
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i(TAG, "Error authenticating", e);
            }
        }
    }
    
    /**
     * Convenience function to change UI state based on being logged in
     */
    private void setLoggedIn(boolean loggedIn) {
    	if (loggedIn) {
    		sync_db.setChecked(true);
    	} else {
    		sync_db.setChecked(false);
    	}
    }

    
    private void logOut() {
        // Remove credentials from the session
        mApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        setLoggedIn(false);
    }
    
    private void checkAppKeySetup() {
        // Check to make sure that we have a valid app key
        if (APP_KEY.startsWith("CHANGE") ||
                APP_SECRET.startsWith("CHANGE")) {
            showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
            finish();
            return;
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            finish();
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }

	@Override
    public boolean onPreferenceClick(Preference preference) {
	    // TODO Auto-generated method stub
	    return false;
    }

	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	    // TODO Auto-generated method stub
    	String key = preference.getKey();
        if (SYNC_DROPBOX.equals(key)) {
        	//mLoggedIn = (Boolean) newValue;
            if (mApi.getSession().isLinked()) {
            	logOut();
            }
            else
            	// Start the remote authentication
                mApi.getSession().startAuthentication(SetPreference.this);
        }
	    return true;
    }
	
	//This is what get called when save a picture
    public static void uploadDB(String fileName, Context ctx) {
    	if (mApi.getSession().authenticationSuccessful()) {
    		Log.i(TAG, "upload!");
    		Uri uri = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
    				  ForTour.DIR_WORK + "/" + fileName ) );
    		File file;
            try {
	            file = new File(new URI(uri.toString()));
	            if (uri != null) {
	            	UploadPicture upload = new UploadPicture(ctx, mApi, PHOTO_DIR, file);
	            	upload.execute();
	            }
            } catch (URISyntaxException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
    	}
    	else {
    		//Toast error = Toast.makeText(this, "Couldn't authenticate with Dropbox", Toast.LENGTH_LONG);
            //error.show();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
        //menu.add(0, 0, 0, "Info");
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //依據itemId來判斷使用者點選哪一個item
        switch(item.getItemId()) {
            case 0:

                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

}

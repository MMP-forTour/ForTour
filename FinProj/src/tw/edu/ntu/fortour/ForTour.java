package tw.edu.ntu.fortour;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class ForTour extends Activity {
    private ImageButton add, view, set;
    private Uri mImageCaptureUri, mImageDirayUri;
    private String mFilename;
    
    protected static DbAdapter mDbHelper;

    private static final int MENU_HELP = Menu.FIRST;

	private static final int PICK_FROM_CAMERA    = 0x100001;
	private static final int CROP_FROM_CAMERA    = 0x100002;
	private static final int PICK_FROM_FILE      = 0x100003;
	protected static final int LOCATION_MAP_PICK = 0x100004;
	protected static final int EDIT_ONE_PHOTO    = 0x100005;
	protected static final int PASS_ONE_PHOTO    = 0x100005;
	
	protected static final String EXT_PHOTO  = ".png";
	protected static final String EXT_RECORD = ".3gp";
	
	protected static final String DIR_WORK   = "ForTour";
	protected static final String DIR_TEMP   = ".tmp";
	protected static final String DIR_THUMB  = ".thumbs";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mDbHelper = new DbAdapter( this );
        mDbHelper.open();
        
        checkWorkDirs();
        findviews();
        setCamera();
        setButtonListener();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	menu.add( 0, MENU_HELP, 0, getString( R.string.stringHelp ) ).setIcon( android.R.drawable.ic_menu_help );
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	
    	switch( item.getItemId() ) {
		case MENU_HELP:
			Intent intent = new Intent();
        	intent.setClass( ForTour.this, ForTourInfo.class );
        	startActivity( intent );
        	overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
			break;
		default:
			break;
		}
    	
    	return true;
    }
    
    private void checkDir( boolean isExternal, final String dirPath ) {
    	File dir;
    	
    	if( isExternal ) dir = new File( Environment.getExternalStorageDirectory(), dirPath );
    	else dir = new File( dirPath );
    	
    	if( !dir.exists() ) {
    		if( !dir.mkdir() ) {
    			Toast.makeText( this, "Working Directories Creation Fail.", Toast.LENGTH_LONG ).show();
    		}
    	}
    }
    
    private void checkWorkDirs() {
    	checkDir( true, DIR_WORK + "/" );
    	checkDir( true, DIR_WORK + "/" + DIR_TEMP + "/" );
    	checkDir( true, DIR_WORK + "/" + DIR_THUMB + "/" );
    }
    
    private void findviews(){
    	add = (ImageButton) findViewById(R.id.button1);
    	view = (ImageButton) findViewById(R.id.button2);
    	set = (ImageButton) findViewById(R.id.button3);
    }
    
    private void setCamera(){
    	final String [] items			= new String [] {"Take from camera", "Select from gallery"};				
		ArrayAdapter<String> adapter	= new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
		AlertDialog.Builder builder		= new AlertDialog.Builder(this);
		
		builder.setTitle("Select Image");
		builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
			public void onClick( DialogInterface dialog, int item ) {
				mFilename = String.valueOf(System.currentTimeMillis()) + EXT_PHOTO;
				
				if (item == 0) {
					//pick from camera
					Intent intent 	 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);					
					mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
									   DIR_WORK + "/" + DIR_TEMP + "/" + mFilename ));
					intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
					
					try {
						intent.putExtra("return-data", true);					
						startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}
				} else { 
					//pick from file
					Intent intent = new Intent();					
	                intent.setType("image/*");
	                intent.setAction(Intent.ACTION_GET_CONTENT);	                
	                startActivityForResult(Intent.createChooser(intent, "Complete Action With"), PICK_FROM_FILE);
				}
			}
		} );
		
		final AlertDialog dialog = builder.create();
		add.setOnClickListener(new View.OnClickListener() {	
			public void onClick(View v) {
				dialog.show();
			}
		});
    }
    
    private void setButtonListener(){
        view.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View arg0){
        		Intent intent = new Intent();
        		intent.setClass( ForTour.this, ListPage.class );        		
        		startActivity( intent );
        	}
        });
        set.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View arg0){
        		Intent intent = new Intent();
        		intent.setClass( ForTour.this, SetPreference.class );
        		startActivity( intent );
        	}
        });
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode != RESULT_OK) return;
	   
	    switch (requestCode) {
		    case PICK_FROM_CAMERA:
		    	doCrop();    	
		    	break;
		    	
		    case PICK_FROM_FILE: 
		    	mImageCaptureUri = data.getData();
		    	doCrop();
		    	break;
	    
		    case CROP_FROM_CAMERA:
		        Bundle extras = data.getExtras();
		        if (extras != null) {	 					
					//open the editPage
					Intent intent1 = new Intent();
					intent1.setClass(ForTour.this, EditPage.class);
					Bundle bundle = new Bundle();
					bundle.putString( "FILE", mFilename );
					intent1.putExtras(bundle);
					startActivityForResult(intent1, PASS_ONE_PHOTO);  
		        }

		        // Delete the template photo
		        Util.deleteFile( new File( mImageCaptureUri.getPath() ) );
		        break;
	    }
	}
    
    private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();   	
    	
		Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        
        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );        
        int size = list.size();        
        
        if (size == 0) {	        
        	Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();       	
            return;
        } else {
        	mImageDirayUri = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
        											  DIR_WORK + "/" + mFilename ) );
        	
        	intent.setData(mImageCaptureUri);        
            intent.putExtra("outputX", ImageUtil.imageInnerWidth );
            intent.putExtra("outputY", ImageUtil.imageInnerHeight );
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            intent.putExtra( MediaStore.EXTRA_OUTPUT, mImageDirayUri );
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
      
        	if (size == 1) {
        		Intent i = new Intent(intent);
	        	ResolveInfo res	= list.get(0);        	
	        	i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
	        	startActivityForResult(i, CROP_FROM_CAMERA);
        	} 
        	else {
		        for (ResolveInfo res : list) {
		        	final CropOption co = new CropOption();		        	
		        	co.title 	= getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
		        	co.icon		= getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
		        	co.appIntent= new Intent(intent);		        	
		        	co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));		        	
		            cropOptions.add(co);
		        }
	        
		        CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);
		        
		        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        builder.setTitle("Choose Crop App");
		        builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
		            public void onClick( DialogInterface dialog, int item ) {
		                startActivityForResult( cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
		            }
		        });
	        
		        builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
		            public void onCancel( DialogInterface dialog ) {		               
		                if (mImageCaptureUri != null ) {
		                    getContentResolver().delete(mImageCaptureUri, null, null );
		                    mImageCaptureUri = null;
		                }
		            }
		        } );
		        
		        AlertDialog alert = builder.create();		        
		        alert.show();
        	}
        }
	}
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	if( mDbHelper != null ) mDbHelper.close();
    }
}
package com.finproj;

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
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

public class FinProj extends Activity {
    Button add, view, set;
    
    protected static DbAdapter mDbHelper;
    
    private Uri mImageCaptureUri, mImageDirayUri;
    private String mFilename;
	
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	
	private static final String WORK_DIR = "ForTour";
	private static final String TEMP_DIR = ".tmp";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.main);
        
        mDbHelper = new DbAdapter( this );
        mDbHelper.open();
        
        checkWorkDirs();
        findviews();
        setCamera();
        setButtonListener();
    }
    
    private void checkWorkDirs() {
    	File workDir = new File( Environment.getExternalStorageDirectory(), WORK_DIR + "/" + TEMP_DIR + "/" );
    	if( !workDir.exists() ) {
    		if( !workDir.mkdirs() ) {
    			Toast.makeText( this, "Working Directories Creation Fail.", Toast.LENGTH_LONG ).show();
    		}
    	}
    }
    
    private void findviews(){
    	add = (Button) findViewById(R.id.button1);
    	view = (Button) findViewById(R.id.button2);
    	set = (Button) findViewById(R.id.button3);
    }
    
    private void setCamera(){
    	final String [] items			= new String [] {"Take from camera", "Select from gallery"};				
		ArrayAdapter<String> adapter	= new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
		AlertDialog.Builder builder		= new AlertDialog.Builder(this);
		
		mFilename = String.valueOf(System.currentTimeMillis()) + ".png";
		
		builder.setTitle("Select Image");
		builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
			public void onClick( DialogInterface dialog, int item ) { 
				if (item == 0) {
					//pick from camera
					Intent intent 	 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);					
					mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
									   WORK_DIR + "/" + TEMP_DIR + "/" + mFilename ));
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
        		
        	}
        });
        set.setOnClickListener(new Button.OnClickListener(){
        	public void onClick(View arg0){
        		
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
					intent1.setClass(FinProj.this, EditPage.class);
					Bundle bundle = new Bundle();
					bundle.putString( "FILE", mImageDirayUri.toString() );
					intent1.putExtras(bundle);
					startActivity(intent1);	            
		        }

		        // Delete the temp photo
		        File f = new File(mImageCaptureUri.getPath());		        
		        if (f.exists()) f.delete();
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
        											  WORK_DIR + "/" + mFilename ) );
        	
        	intent.setData(mImageCaptureUri);        
            intent.putExtra("outputX", 360);
            intent.putExtra("outputY", 360);
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
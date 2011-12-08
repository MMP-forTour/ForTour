package com.finproj;



import java.io.File;
import java.io.IOException;
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
    
    private File thisFile;
    private Uri mImageCaptureUri;
	
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.main);
        findviews();
        setCamera();
        setButtonListener();
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
		
		builder.setTitle("Select Image");
		builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
			public void onClick( DialogInterface dialog, int item ) { 
				if (item == 0) {
					//pick from camera
					Intent intent 	 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);					
					mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
									   "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".png"));
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
	                startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
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
		         // File tempFile = getTempFile();
		         // new logic to get the photo from a URI
		            if (data.getAction() != null) {
		            	//processPhotoUpdate(thisFile);
		           }

		            //open the editPage
		            Intent intent1 = new Intent();
					intent1.setClass(FinProj.this, EditPage.class);
					Bundle bundle = new Bundle();
					bundle.putString("FILE", thisFile.toString());
					intent1.putExtras(bundle);
					startActivity(intent1);	            
		        }

		        // Delete the temp photo. ("tmp_avatar_" ... + ".png"))
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
        	intent.setData(mImageCaptureUri);        
            intent.putExtra("outputX", 540);
            intent.putExtra("outputY", 540);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            //intent.putExtra("noFaceDetection", true);            
            intent.putExtra("return-data", false);           
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
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
    private Uri getTempUri() {
    	return Uri.fromFile(getTempFile());
    }

    private File getTempFile() {
    	if (isSDCARDMounted()) {
    		String TEMP_PHOTO_FILE="/bluetooth/myFile.PNG";
    		File f = new File(Environment.getExternalStorageDirectory(),TEMP_PHOTO_FILE);
    		try {
    			f.createNewFile();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			Toast.makeText(this, "fileIOIssue", Toast.LENGTH_LONG).show();
    		}
    		thisFile = f;  		    		
    		return f;
    	}
    	else {
    		return null;
    	}
    }

    private boolean isSDCARDMounted(){
    	String status = Environment.getExternalStorageState();
    	if (status.equals(Environment.MEDIA_MOUNTED))
    		return true;
    	return false;
    	
	}
    /*
	 *  processes a temp photo file from 
	 */
	/*private void processPhotoUpdate(File tempFile) {
		ProcessProfilePhotoTask task = new ProcessProfilePhotoTask(){

			protected void onPostExecute(Bitmap result) {
				//android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(result.getWidth(),result.getHeight());
				//params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
				//photo.setLayoutParams(params);
				//photo.setImageBitmap(result);
				//imageView1.setImageBitmap(result);
			}
			
		};
		task.execute(tempFile);
		
	}*/
   
}//end class FinProj
package com.mjumel.mystories;

import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mjumel.mystories.adapters.NothingSelectedSpinnerAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;
import com.mjumel.mystories.tools.ImageWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

public class EventNewFragment extends Fragment {
	
	private DrawerActivity activity;

	private Spinner spnCats;
	private Button btnRemember;
	private ImageView ivImage;
	private EditText etComment;
	private RatingBar rbRating;
	private TextView imageText;
	
	private Uri mediaUri;
	private Uri mImageCaptureUri;
	private String uId;
	private List<Event> eventList = null;
	
	private Intent pictureActionIntent = null;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Gen.appendLog("EventNewFragment::onCreate> Starting");
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
    	activity = (DrawerActivity) getActivity();
    	
		eventList = activity.getEventList();
		uId = activity.getUserId();
    	
    	mediaUri = (Uri)getExtra(Intent.EXTRA_STREAM);
    	activity.getIntent().putExtra(Intent.EXTRA_STREAM, (String)null);
    	
		Gen.appendLog("EventNewFragment::onCreate> mediaUri = " + mediaUri);
		Gen.appendLog("EventNewFragment::onCreate> uid = " + uId);
		Gen.appendLog("EventNewFragment::onCreate> eventsList nb = " + (eventList == null ? 0 : eventList.size()));
		
    	Gen.appendLog("EventNewFragment::onCreate> Ending");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("EventNewFragment::onCreateView> Starting");
		
		View view = inflater.inflate(R.layout.fragment_new_event,container, false);
		spnCats = (Spinner) view.findViewById(R.id.event_cats);
		ivImage = (ImageView) view.findViewById(R.id.event_imageView);
		etComment = (EditText) view.findViewById(R.id.event_comment);
		btnRemember = (Button) view.findViewById(R.id.event_save);
		rbRating  = (RatingBar) view.findViewById(R.id.event_rating);
		imageText = (TextView) view.findViewById(R.id.event_imageView_text);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity, R.array.nav_spinner_cats, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnCats.setPrompt("Select your category");
		spnCats.setAdapter(
		  new NothingSelectedSpinnerAdapter(
		        adapter,
		        R.layout.cats_spinner_row_nothing_selected,
		        // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
		        activity));
		
		if (mediaUri != null)
			setImage(mediaUri);
		
		ivImage.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                startDialog();
            }
        });
		
		btnRemember.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	new PostEventTask().execute(new String[] {});
            }
         });
		
		Gen.appendLog("EventNewFragment::onCreateView> Ending");
		return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_new_event, menu);
    }
    
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Gen.appendLog("EventNewFragment::onActivityResult> Starting");
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (resultCode != Activity.RESULT_OK) {
    		Gen.appendLog("EventNewFragment::onActivityResult> RESULT_KO (Cancelled?)");
    		return;
    	}
    	
    	mediaUri = null;
    	switch(requestCode) {
	    	case(MyStoriesApp.SELECT_PICTURE):
	    		Gen.appendLog("EventNewFragment::onActivityResult> Case SELECT_PICTURE");
	    		mediaUri = data.getData();
	    		break;
	    	case(MyStoriesApp.GALLERY_PICTURE):
	    		Gen.appendLog("EventNewFragment::onActivityResult> Case GALLERY_PICTURE");
                if (data != null) {
                	Gen.appendLog("EventNewFragment::onActivityResult> Url = " + data.getData());
                	//mediaLocalPath = ImageWorker.getPath(getActivity(), data.getData());
                	mediaUri = data.getData();
                }
	    		break;
	    	case(MyStoriesApp.CAMERA_REQUEST):
	    		Gen.appendLog("EventNewFragment::onActivityResult> Case CAMERA_REQUEST");
	    		mediaUri = ImageWorker.getCameraImagePath(activity, data);
                break;
    	}
    	if (mediaUri != null) {
    		setImage(mediaUri);
    	} else {
    		Toast.makeText(activity, "Problem getting back the image", Toast.LENGTH_SHORT).show();
    		Gen.appendError("EventNewFragment::onActivityResult> Problem getting back the image");
    	}
    }
    
    private void startDialog() {
    	Gen.appendLog("EventNewFragment::startDialog> Starting");
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(activity);
        myAlertDialog.setTitle("Get Pictures Option");
        myAlertDialog.setMessage("Where do you want to get your picture from?");

        myAlertDialog.setPositiveButton(R.string.alert1_gallery,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        pictureActionIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
                        pictureActionIntent.setType("image/*");
                        pictureActionIntent.putExtra("return-data", true);
                        startActivityForResult(pictureActionIntent, MyStoriesApp.GALLERY_PICTURE);
                    }
                });

        myAlertDialog.setNegativeButton(R.string.alert1_camera,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //pictureActionIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        //startActivityForResult(pictureActionIntent, CAMERA_REQUEST);
                        
                        // Other Method
                        String storageState = Environment.getExternalStorageState();

                        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                            pictureActionIntent = new Intent(
                                    MediaStore.ACTION_IMAGE_CAPTURE);

                            String filename = System.currentTimeMillis() + ".jpg";
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, filename);
                            mImageCaptureUri = activity.getContentResolver().insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    values);

                            pictureActionIntent.putExtra(
                                    android.provider.MediaStore.EXTRA_OUTPUT,
                                    mImageCaptureUri);

                            try {
                                startActivityForResult(pictureActionIntent, MyStoriesApp.CAMERA_REQUEST);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(activity, "SD Card required", Toast.LENGTH_SHORT).show();
                            Gen.appendLog("EventNewFragment::startDialog> SD Card is required", "E");
                        }

                    }
                });
        myAlertDialog.show();
    }
    
    private void setImage(Uri uri)
    {
    	Gen.appendLog("EventNewFragment::setImage> Starting with uri=" + uri.toString());
    	mediaUri = uri;
		try {
			ExifInterface ei = new ExifInterface(mediaUri.toString());
			int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			Gen.appendLog("EventNewFragment::setImage> Image orientation = " + orientation);
		} catch (IOException e) {
			Gen.appendLog("EventNewFragment::setImage> Image orientation error");
			Gen.appendLog("EventNewFragment::setImage> " + e.getLocalizedMessage());
			e.printStackTrace();
		}     //Since API Level 5
    	
		imageText.setVisibility(TextView.GONE);
    	ImageLoader.getInstance().displayImage(mediaUri.toString(), ivImage);
    	Gen.appendLog("EventNewFragment::setImage>Loading uri=" + ImageLoader.getInstance().getLoadingUriForView(ivImage));
    }
    
    private Object getExtra(String id)
    {
    	if (activity.getIntent().getExtras() != null)
			return activity.getIntent().getExtras().get(id);
    	else
    		return null;
    }
    
    private void hideKeyboard()
    {
    	// Hide keyboard from other fragments
    	InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    	//check if no view has focus:
        View v = activity.getCurrentFocus();
        if(v==null)
            return;
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
    
    
    /***************************************************************************************
	 *
	 *                                Event-based functions
	 * 
	 ***************************************************************************************/
	
    
    /***************************************************************************************
	 *
	 *                                PostEventTask Class
	 * 
	 ***************************************************************************************/
	class PostEventTask extends AsyncTask<String, Integer, JSONObject>
    {
		 private ProgressDialog pg;
		 private String imagePath = null;
		
         protected void onPreExecute() {
        	 pg = ProgressDialog.show(activity, "", "Posting event...", true);
        	 hideKeyboard();
         } 

         protected JSONObject doInBackground(String ...params) {
        	 imagePath = mediaUri==null?null:ImageWorker.getPath(activity, mediaUri);
        	 
        	 Gen.appendLog("EventNewFragment$PostEventTask::doInBackground> uId = " + uId);
             Gen.appendLog("EventNewFragment$PostEventTask::doInBackground> comment = " + etComment.getText().toString());
             Gen.appendLog("EventNewFragment$PostEventTask::doInBackground> rating = " + rbRating.getProgress());
             Gen.appendLog("EventNewFragment$PostEventTask::doInBackground> imagePath = " + imagePath);
             Gen.appendLog("EventNewFragment$PostEventTask::doInBackground> cat = " + spnCats.getSelectedItemPosition());
             
             JSONObject event = Communication.newEvent(
             		uId, 
             		etComment.getText().toString(), 
             		rbRating.getProgress(), 
             		imagePath,
             		spnCats.getSelectedItemPosition()
             );
             
             return event;
        } 

        protected void onPostExecute(JSONObject result) {  
        	pg.dismiss();
        	if (result.optString("error_msg", null) != null) {
        		Gen.appendError("EventNewFragment$PostEventTask::onPostExecute> Error while creating event");
        		Gen.appendError("EventNewFragment$PostEventTask::onPostExecute> " + result.opt("error_msg"));
        		Toast.makeText(activity, "Error while creating event", Toast.LENGTH_SHORT).show();
        	}
        	else {
        		Event event = new Event(
        			etComment.getText().toString(), 
               		rbRating.getProgress(), 
               		spnCats.getSelectedItemPosition(), 
               		result.optString("path_thumb", null),
               		result.optString("path_resized", null),
               		result.optString("path_original", null),
               		uId,
               		(String)null,
               		result.optString("event_id")
               	);
               	if (eventList != null) {
               		Gen.appendLog("EventNewFragment$PostEventTask::onPostExecute> Adding new event and going back to list");
               		Toast.makeText(activity, "Event created successfully", Toast.LENGTH_SHORT).show();
               		eventList.add(0, event);
               		activity.sendEventList(eventList);
               		activity.getFragmentManager().popBackStackImmediate();
               	}
            }
        }
         
        @Override
 		protected void onCancelled() {
       	  pg.dismiss();
 		}
    }
}

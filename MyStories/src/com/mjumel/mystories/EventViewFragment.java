package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mjumel.mystories.adapters.NothingSelectedSpinnerAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;
import com.mjumel.mystories.tools.ImageWorker;
import com.nostra13.universalimageloader.core.ImageLoader;

public class EventViewFragment extends Fragment {
	
	private final static String ACTIONBAR_TITLE = "myevents";
	
	private EventViewActivity activity;

	private View view;
	private Spinner spnCats;
	private ImageView ivImage;
	private EditText etComment;
	private TextView etCommentRO;
	private RatingBar rbRating, rbRatingMini;
	
	private List<Event> eventList = null;
	private List<Story> storyList = null;
	private int position;
	private Event event;
	private String mediaPath;
	private String uId;
	
	private Menu mMenu = null;
	private boolean editMode = false;
	private boolean fullScreenMode = false;
	
	private Intent pictureActionIntent = null;
	
    public EventViewFragment()
    {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
    	activity = (EventViewActivity) getActivity();
    	
		storyList = activity.getStoryList();
		eventList = activity.getEventList();
		
		if (getArguments() != null) {
			if (getArguments().containsKey("position")) {
				position = getArguments().getInt("position");
				event = eventList.get(position);
		    	mediaPath = event.getResizedMediaPath();
			}
		}
		
		Gen.appendLog("EventViewFragment::onCreate> uid = " + uId);
		Gen.appendLog("EventViewFragment::onCreate> eventId = " + event.getEventId());
		Gen.appendLog("EventViewFragment::onCreate> mediaPath = " + mediaPath);
		Gen.appendLog("EventViewFragment::onCreate> eventsList nb = " + (eventList == null ? 0 : eventList.size()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("EventViewFragment::onCreateView> Starting");

		view = inflater.inflate(R.layout.fragment_view_event,container, false);
		spnCats = (Spinner) view.findViewById(R.id.view_event_cats);
		ivImage = (ImageView) view.findViewById(R.id.view_event_imageView);
		etComment = (EditText) view.findViewById(R.id.view_event_comment);
		etCommentRO = (TextView) view.findViewById(R.id.view_event_textView);
		rbRating  = (RatingBar) view.findViewById(R.id.view_event_rating);
		rbRatingMini  = (RatingBar) view.findViewById(R.id.view_event_mini_rating);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity, R.array.nav_spinner_cats, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnCats.setPrompt("Select your category");
		spnCats.setAdapter(
		  new NothingSelectedSpinnerAdapter(
		        adapter,
		        R.layout.cats_spinner_row_nothing_selected,
		        // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
		        activity));
		
		if (mediaPath != null)
			ImageLoader.getInstance().displayImage(mediaPath, ivImage);
		else
			ivImage.setVisibility(ImageView.GONE);
		
		etComment.setText(event.getComment());
		etCommentRO.setText(event.getComment());
		rbRating.setProgress(event.getRating());
		rbRatingMini.setProgress(event.getRating());
		spnCats.setSelection(event.getCategoryId());
		
		rbRating.setOnRatingBarChangeListener(ratingChanged);
		ivImage.setOnClickListener(imageClicked);
		
		Gen.appendLog("EventViewFragment::onCreateView> Ending");
		return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	Gen.appendLog("EventViewFragment::onCreateOptionsMenu> Starting");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_view_event, menu);
        mMenu = menu;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.view_event_edit:
	        case R.id.view_event_cancel:
	        	goEditMode();
	            return true;
	        case R.id.view_event_save:
	        	new PostEventTask().execute(new String[] {});
	            return true;
	        /*case R.id.view_event_back:
	        	goFullScreen();
	        	return true;*/
	        case R.id.view_event_delete:
	        	deleteDialog();
	        	return true;
	        case R.id.view_event_link:
        		linkDialog();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	Gen.appendLog("EventListFragment::onResume> Starting");
    	activity.setTitle(ACTIONBAR_TITLE);
    	Gen.appendLog("EventListFragment::onResume> Ending");
    }
    
    public static EventViewFragment newInstance(List<Event> eventList, int pos) {
    	Gen.appendLog("EventViewFragment::newInstance> Starting (pos=" + pos + ")");
    	EventViewFragment fragment = new EventViewFragment();
    	Bundle args = new Bundle();
    	args.putInt("position", pos);
    	fragment.setArguments(args);
    	return fragment;
    }
    
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Gen.appendLog("EventNewFragment::onActivityResult> Starting");
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (resultCode != Activity.RESULT_OK) {
    		Gen.appendLog("EventNewFragment::onActivityResult> RESULT_KO (Cancelled?)");
    		return;
    	}
    	
    	Uri mediaUri = null;
    	switch(requestCode) {
	    	case(MyStoriesApp.SELECT_PICTURE):
	    		Gen.appendLog("EventNewFragment::onActivityResult> Case SELECT_PICTURE");
	    		mediaUri = data.getData();
	    		break;
	    	case(MyStoriesApp.GALLERY_PICTURE):
	    		Gen.appendLog("EventNewFragment::onActivityResult> Case GALLERY_PICTURE");
                if (data != null) {
                	Gen.appendLog("EventNewFragment::onActivityResult> Url = " + data.getData());
                	//mediaLocalPath = ImageWorker.getPath(activity, data.getData());
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
    
    private void setImage(Uri uri)
    {
    	Gen.appendLog("EventNewFragment::setImage> Starting with uri=" + uri.toString());
    	
    	mediaPath = uri.toString();
    	ImageLoader.getInstance().displayImage(mediaPath, ivImage);
    	
    	Gen.appendLog("EventNewFragment::setImage>Loading uri=" + ImageLoader.getInstance().getLoadingUriForView(ivImage));
    }
    
    private void goFullScreen()
    {
    	fullScreenMode = !fullScreenMode;
    	
    	if (fullScreenMode) {
    		spnCats.setVisibility(Spinner.GONE);
    		etComment.setVisibility(EditText.GONE);
    		etCommentRO.setVisibility(TextView.GONE);
    	} else {
    		spnCats.setVisibility(Spinner.VISIBLE);
    		etComment.setVisibility(EditText.INVISIBLE);
    		etCommentRO.setVisibility(TextView.VISIBLE);
    	}
    	
    	if (mMenu != null) {
    		mMenu.findItem(R.id.view_event_link).setVisible(!fullScreenMode);
    		mMenu.findItem(R.id.view_event_edit).setVisible(!fullScreenMode);
    		mMenu.findItem(R.id.view_event_delete).setVisible(!fullScreenMode);
    		mMenu.findItem(R.id.view_event_cancel).setVisible(fullScreenMode);
    		mMenu.findItem(R.id.view_event_save).setVisible(fullScreenMode);
		}
    	
    	rbRatingMini.setVisibility(RatingBar.VISIBLE);
    	rbRating.setVisibility(RatingBar.GONE);
    	//ivImage.setClickable(false);
    	ivImage.invalidate();
    }
    
    private void goEditMode()
    {
    	editMode = !editMode;
    	
    	if (editMode) {
    		rbRating.setVisibility(RatingBar.VISIBLE);
    		rbRatingMini.setVisibility(RatingBar.GONE);
    		etComment.setVisibility(EditText.VISIBLE);
    		etCommentRO.setVisibility(TextView.INVISIBLE);
    		ivImage.setVisibility(ImageView.VISIBLE);
    	} else {
    		rbRating.setVisibility(RatingBar.GONE);
    		rbRatingMini.setVisibility(RatingBar.VISIBLE);
    		etComment.setVisibility(EditText.INVISIBLE);
    		etCommentRO.setVisibility(TextView.VISIBLE);
    		if (mediaPath != null)
    			ivImage.setVisibility(ImageView.VISIBLE);
    		else
    			ivImage.setVisibility(ImageView.GONE);
    	}
    	
    	if (mMenu != null)
    	{
    		mMenu.findItem(R.id.view_event_link).setVisible(!editMode);
    		mMenu.findItem(R.id.view_event_edit).setVisible(!editMode);
    		mMenu.findItem(R.id.view_event_delete).setVisible(!editMode);
    		mMenu.findItem(R.id.view_event_cancel).setVisible(editMode);
    		mMenu.findItem(R.id.view_event_save).setVisible(editMode);
		}
    	
		spnCats.setClickable(editMode);
		etComment.setClickable(editMode);
		ivImage.setClickable(editMode);
    }
    
    
    
    /***************************************************************************************
	 *
	 *                                Event-based functions
	 * 
	 ***************************************************************************************/
	private OnRatingBarChangeListener ratingChanged = new OnRatingBarChangeListener() {
		@Override
		public void onRatingChanged(RatingBar ratingBar, float rating,
				boolean fromUser) {
			rbRatingMini.setProgress(ratingBar.getProgress());				
		}
		
	};
	
	private OnClickListener imageClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
            if (editMode) {
            	chooseSourceDialog();
            } else 
            	goFullScreen();
        }
    };
    
    private void chooseSourceDialog() {
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
                            Uri mImageCaptureUri = activity.getContentResolver().insert(
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
    
    private void deleteDialog() {
    	Gen.appendLog("EventViewFragment::deleteDialog> Starting");
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(activity);
        myAlertDialog.setTitle("Delete event");
        myAlertDialog.setMessage("This event will be removed from all the stories it is in!");

        myAlertDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    	new DeleteEventTask().execute(new String[] {});
                    }
                });
        myAlertDialog.setNegativeButton("No", null);
        myAlertDialog.show();
    }
    
    private void linkDialog() {
    	Gen.appendLog("EventViewFragment::linkDialog> Starting");
    	
    	if (storyList == null) {
    		Gen.appendError("EventViewFragment::linkDialog> storyList is empty");
    		Toast.makeText(getActivity(), "You don't have any story", Toast.LENGTH_SHORT).show();
    		return;
    	}

    	String[] stories = new String[storyList.size()];
    	for (int i=0;i<storyList.size();i++)
    		stories[i] = storyList.get(i).getTitle();
    	
    	Gen.appendLog("EventViewFragment::linkDialog> Here");
    	AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(getActivity());
        myAlertDialog.setTitle("Stories to link to");
        myAlertDialog.setMultiChoiceItems(stories, null,
                new DialogInterface.OnMultiChoiceClickListener() {
		        @Override
		        public void onClick(DialogInterface dialog, int pos,
		            boolean isChecked) {
		        	storyList.get(pos).setSelected(isChecked);
		        }
        });

        myAlertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
                    	new LinkEventTask().execute();
                    }
                });
        myAlertDialog.setNegativeButton("Cancel", null);
        myAlertDialog.show();
    }
    
    
    /***************************************************************************************
	 *
	 *                                PostEventTask Class
	 * 
	 ***************************************************************************************/
	class PostEventTask extends AsyncTask<String, Integer, JSONObject>
    {
		private ProgressDialog pg;
		private String imagePath;
		 
        protected void onPreExecute() {
        	pg = ProgressDialog.show(activity, "", "Posting event...", true);
        } 

        protected JSONObject doInBackground(String ...params) {
        	Gen.appendLog("EventViewFragment$PostEventTask::doInBackground> old imagepath  = " + event.getResizedMediaPath());
        	Gen.appendLog("EventViewFragment$PostEventTask::doInBackground> new imagepath  = " + mediaPath);
        	if (mediaPath == null) {
        		imagePath = null;
        		event.setOriginalMediaPath(imagePath);
                event.setResizedMediaPath(imagePath);
                event.setThumbMediaPath(imagePath);
        	} else if (mediaPath.compareTo(event.getResizedMediaPath() == null ? "" : event.getResizedMediaPath()) == 0) {
        		imagePath = "nochange";
        	} else {
        		imagePath = ImageWorker.getPath(activity, Uri.parse(mediaPath));
        		event.setOriginalMediaPath(mediaPath);
                event.setResizedMediaPath(mediaPath);
                event.setThumbMediaPath(mediaPath);
        	}
        	
            event.setComment(etComment.getText().toString());
            event.setRating(rbRating.getProgress());
            event.setCategoryId(spnCats.getSelectedItemPosition());
            
            Gen.appendLog("EventViewFragment$PostEventTask::doInBackground> uId = " + event.getUserId());
            Gen.appendLog("EventViewFragment$PostEventTask::doInBackground> eventId = " + event.getEventId());
            Gen.appendLog("EventViewFragment$PostEventTask::doInBackground> comment = " + event.getComment());
            Gen.appendLog("EventViewFragment$PostEventTask::doInBackground> rating = " + event.getRating());
            Gen.appendLog("EventViewFragment$PostEventTask::doInBackground> imagePath = " + imagePath);
            Gen.appendLog("EventViewFragment$PostEventTask::doInBackground> cat = " + event.getCategoryId());
             
            return Communication.editEvent(
            	event.getUserId(),
            	event.getEventId(),
            	event.getComment(),
            	event.getRating(),
            	imagePath,
            	event.getCategoryId()
            );
        }

        protected void onPostExecute(JSONObject result) {  
        	pg.dismiss();
        	if (result.optString("error_msg", null) != null) {
        		Gen.appendError("EventViewFragment$PostEventTask::onPostExecute> uId = " + uId);
        		Gen.appendError("EventViewFragment$PostEventTask::onPostExecute> result = " + result.optString("error_msg"));
        		Toast.makeText(activity, "Event could not be edited. Please retry later", Toast.LENGTH_SHORT).show();
        	} else {
        		Toast.makeText(activity, "Event edited successfully", Toast.LENGTH_SHORT).show();
        		etCommentRO.setText(etComment.getText().toString());
        		event.setOriginalMediaPath(result.optString("path_original", null));
                event.setResizedMediaPath(result.optString("path_resized", null));
                event.setThumbMediaPath(result.optString("path_thumb", null));
        		eventList.set(position, event);
        		activity.sendEventList(eventList);
        		activity.updateEventOnStories(event);
        		goEditMode();
        	}
        }
        
        @Override
 		protected void onCancelled() {
        	Toast.makeText(getActivity(), "Operation cancelled", Toast.LENGTH_SHORT).show();
        	pg.dismiss();
 		}
    }


	/***************************************************************************************
	 *
	 *                                DeleteEventTask Class
	 * 
	 ***************************************************************************************/
	class DeleteEventTask extends AsyncTask<String, Integer, Boolean>
    {
		private ProgressDialog pg;
		 
       protected void onPreExecute() {
    	   pg = ProgressDialog.show(activity, "", "Deleting event...", true);
       } 

       protected Boolean doInBackground(String ...params) {
    	   event.setSelected(true);
       	   return Communication.deleteEvent(uId, event);
       }

       protected void onPostExecute(Boolean result) {
	       if (!result) {
	    	   Gen.appendError("EventViewFragment$DeleteEventTask::onPostExecute> uId = " + event.getUserId());
	    	   Gen.appendError("EventViewFragment$DeleteEventTask::onPostExecute> eventId = " + event.getEventId());
	    	   Toast.makeText(activity, "Event could not be deleted. Please retry later", Toast.LENGTH_SHORT).show();
	       } else {
	    	   Toast.makeText(activity, "Event deleted", Toast.LENGTH_SHORT).show();
	    	   eventList.remove(position);
	    	   activity.sendEventList(eventList);
	    	   activity.removeEventFromStories(event.getEventId());
	    	   activity.getFragmentManager().popBackStackImmediate();
	       }
	       event.setSelected(false);
	       pg.dismiss();
       }
       
       @Override
       protected void onCancelled() {
    	   Toast.makeText(getActivity(), "Operation cancelled", Toast.LENGTH_SHORT).show();
    	   pg.dismiss();
       }
   }
	
	
	/***************************************************************************************
	 *
	 *                                LinkEventTask Class
	 * 
	 ***************************************************************************************/
	private class LinkEventTask extends AsyncTask<Void, Integer, Boolean>
	{
		private ProgressDialog pg = null;
		
		protected void onPreExecute() {
			pg = ProgressDialog.show(getActivity(), "", "Linking event...", true);
		}

		protected Boolean doInBackground(Void ...params) {
			List<Story> stories = new ArrayList<Story>();
			for(Story s : storyList) {
				if (s.isSelected()) {
					stories.add(s);
					s.addEvent(event);
				}
			}
			event.setSelected(true);
			return Communication.linkEvent(uId, event, stories);
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				Gen.appendError("EventViewFragment$LinkEventsTask::onPostExecute> Error while linking stories");
				Toast.makeText(getActivity(), "Event could not be linked. Please retry later", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(), "Event linked", Toast.LENGTH_SHORT).show();
			}
			
			for(Story s : storyList)
				s.setSelected(false);
			event.setSelected(false);
			activity.sendEventList(eventList);
			activity.sendStoryList(storyList);
			pg.dismiss();
		}
  
		@Override
		protected void onCancelled() {
			Toast.makeText(getActivity(), "Operation cancelled", Toast.LENGTH_SHORT).show();
			pg.dismiss();
		}
	}

}

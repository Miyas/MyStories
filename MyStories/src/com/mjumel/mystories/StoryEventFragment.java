package com.mjumel.mystories;


import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.mjumel.mystories.adapters.StoryPagerAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;
import com.nostra13.universalimageloader.core.ImageLoader;

public class StoryEventFragment extends Fragment {
	
	StoryViewFragment activity;
	StoryPagerAdapter adapter;
	
	private Story story = null;
	private Event event = null;
	private int eventPos = -1;
	private String uId = null;
	
	private TextView comment;
	private ImageView image;
	private RatingBar rating;
	
	private Menu mMenu = null;
	private boolean editMode = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(false);
    	
    	activity = (StoryViewFragment)getActivity();
    	
    	getActivity().getActionBar().hide();
    	
    	Gen.appendLog("StoryEventFragment::onCreate> Starting");
    	story = (Story)getArguments().getParcelable("story");
    	event = (Event)getArguments().getParcelable("event");
    	eventPos = getArguments().getInt("pos");
    	uId = story.getUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
		View view = inflater.inflate(R.layout.fragment_view_story_item,container, false);
		comment = (TextView) view.findViewById(R.id.story_event_comment);
		image = (ImageView) view.findViewById(R.id.story_event_image);
		rating = (RatingBar) view.findViewById(R.id.story_event_rating);
		
		comment.setText(event.getComment());
		rating.setProgress(event.getRating());
		if (event.getResizedMediaPath() != null) { 
			ImageLoader.getInstance().displayImage(event.getResizedMediaPath(), image);
		}
		
		if (event.getEventId().compareTo("-1") == 0) {
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			comment.setLayoutParams(lp);
			image.setVisibility(ImageView.GONE);
			rating.setVisibility(RatingBar.GONE);
		}
		
		image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	setHasOptionsMenu(true);
            	if (activity.getActionBar().isShowing())
            		activity.getActionBar().hide();
            	else
            		activity.getActionBar().show();
            }
         });
		
		Gen.appendLog("StoryEventFragment::onCreateView> Ending");
		return view;
    }
    
    public static StoryEventFragment newInstance(Story story, Event event, int pos) {
    	Gen.appendLog("StoryEventFragment::newInstance> Starting (pos=" + pos + ")");
    	StoryEventFragment fragment = new StoryEventFragment();
    	Bundle args = new Bundle();
    	args.putParcelable("story", story);
    	args.putParcelable("event", event);
    	args.putInt("pos", pos);
    	fragment.setArguments(args);
    	return fragment;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_view_story, menu);
        activity.setTitle("Event #" + eventPos);
        mMenu = menu;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	        case R.id.view_story_edit:
	        case R.id.view_story_cancel:
	        	goEditMode();
	            return true;
	        case R.id.view_story_delete:
	        	deleteDialog();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
    }
 
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    	return;
    }
    
    private void goEditMode()
    {
    	if (mMenu != null)
    	{
    		mMenu.findItem(R.id.view_story_rate).setVisible(editMode);
    		mMenu.findItem(R.id.view_story_publish).setVisible(editMode);
    		mMenu.findItem(R.id.view_story_edit).setVisible(editMode);
    		mMenu.findItem(R.id.view_story_delete).setVisible(!editMode);
    		mMenu.findItem(R.id.view_story_cancel).setVisible(!editMode);
    		mMenu.findItem(R.id.view_story_save).setVisible(!editMode);
		}
		
		editMode = !editMode;
    }
    
    private void deleteDialog() {
    	Gen.appendLog("StoryEventFragment::deleteDialog> Starting");
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(activity);
        myAlertDialog.setTitle("Are you sure?");
        myAlertDialog.setMessage("This event will disappear from this story!");

        myAlertDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    	new RemoveLinkEventStoryTask().execute(new String[] {});
                    }
                });
        myAlertDialog.setNegativeButton("No", null);
        myAlertDialog.show();
    }
    
    
    /***************************************************************************************
	 *
	 *                                RemoveLinkEventStoryTask Class
	 * 
	 ***************************************************************************************/
	class RemoveLinkEventStoryTask extends AsyncTask<String, Integer, JSONObject>
	{
		private ProgressDialog pg;
		 
		protected void onPreExecute() {
			pg = ProgressDialog.show(activity, "", "Removing link...", true);
		} 

		protected JSONObject doInBackground(String ...params) {
			return Communication.removeLink(uId, event, story);
		}

		protected void onPostExecute(JSONObject result) {
			pg.dismiss();
			if (result.optString("error_msg", null) != null) {
				Gen.appendError("StoryEventFragment$RemoveLinkEventStoryTask::onPostExecute> uId = " + story.getUserId());
				Gen.appendError("StoryEventFragment$RemoveLinkEventStoryTask::onPostExecute> storyId = " + story.getStoryId());
				Gen.appendError("StoryEventFragment$RemoveLinkEventStoryTask::onPostExecute> eventId = " + event.getEventId());
				Gen.appendError("StoryEventFragment$RemoveLinkEventStoryTask::onPostExecute> eventId = " + result.optString("error_msg", null));
				Toast.makeText(activity, result.optString("error_msg", null), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(activity, "Event removed", Toast.LENGTH_SHORT).show();
				story.getEvents().remove(event);
				activity.updatePager(story);
				//activity.finish();
			}
		}
      
		@Override
		protected void onCancelled() {
			pg.dismiss();
		}
	}
}

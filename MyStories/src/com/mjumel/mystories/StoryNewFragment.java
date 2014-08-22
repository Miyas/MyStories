package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mjumel.mystories.adapters.EventListAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;

public class StoryNewFragment extends Fragment {
	
	private DrawerActivity activity;

	private ListView lv;
	private EditText te;
	private EventListAdapter adapter;
	private Menu mMenu;
	
	private String uId = null;
	private List<Story> storyList = null;
	private List<Event> eventList = null;
	private boolean checkAllSelected = false;
	//private boolean hideAllText = false;
	//private boolean hideAllStars = false;
	
	
    public StoryNewFragment()
    {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
    	activity = (DrawerActivity) getActivity();
    	
		eventList = activity.getEventList();
		storyList = activity.getStoryList();
		uId = activity.getUserId();
    	
		adapter = new EventListAdapter(activity, this, eventList);
		
		Gen.appendLog("NewStoryFragment::onCreate> uId=" + uId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("NewStoryFragment::onCreateView> Starting");
    	
    	View view = inflater.inflate(R.layout.fragment_new_story,container, false);
    	te = (EditText) view.findViewById(R.id.new_story_title);
    	lv = (ListView) view.findViewById(R.id.share_story_contacts);
		lv.setAdapter(adapter);
		
		Gen.appendLog("NewStoryFragment::onCreateView> Ending");
		return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_new_story, menu);
        menu.findItem(R.id.new_story_check_all).setVisible(false);
        menu.findItem(R.id.new_story_save).setVisible(false);
        mMenu = menu;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.new_story_check_all:
        	Gen.appendLog("NewStoryFragment::onOptionsItemSelected> Menu new_story_check_all");
        	for (Event event : eventList)
        		event.setSelected(!checkAllSelected);
        	checkAllSelected = !checkAllSelected;
        	//updateMenu();
        	adapter.notifyDataSetChanged();
            return true;
        case R.id.new_story_save:
        	Gen.appendLog("NewStoryFragment::onOptionsItemSelected> Menu new_story_save");
        	new SaveStoryTask().execute(eventList);
            return true;
        case R.id.new_story_filter:
        	Gen.appendLog("NewStoryFragment::onOptionsItemSelected> Menu new_story_filter");
        	Toast.makeText(activity, "TODO", Toast.LENGTH_SHORT).show();
        	// TODO
            return true;
        case R.id.new_story_hide_text:
        	Gen.appendLog("NewStoryFragment::onOptionsItemSelected> Menu new_story_hide_text");
        	Toast.makeText(activity, "TODO", Toast.LENGTH_SHORT).show();
        	// TODO
            return true;
        case R.id.new_story_hide_stars:
        	Gen.appendLog("NewStoryFragment::onOptionsItemSelected> Menu new_story_hide_stars");
        	Toast.makeText(activity, "TODO", Toast.LENGTH_SHORT).show();
        	// TODO
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	Gen.appendLog("NewStoryFragment::onResume> Starting");
    	lv.setAdapter(adapter);
    	Gen.appendLog("NewStoryFragment::onResume> Ending");
    }
    
    
    /***************************************************************************************
	 *
	 *                                Event-based functions
	 * 
	 ***************************************************************************************/
	
	public void updateMenu(boolean show) {
       	mMenu.findItem(R.id.new_story_save).setVisible(show);
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
	 *                                SaveStoryTask Class
	 * 
	 ***************************************************************************************/
	class SaveStoryTask extends AsyncTask<List<Event>, Integer, Integer> {
		
		private ProgressDialog pg;
		private Story story = null;

        protected void onPreExecute() {
        	pg = ProgressDialog.show(activity, "", "Posting event...", true);
       	 	hideKeyboard();
        } 

        protected Integer doInBackground(List<Event> ...params) {
        	List<Event> events = (List<Event>)params[0];
        	List<Event> selEvents = new ArrayList<Event>();
        	for (Event event : events) {
        		if (event.isSelected())
        			selEvents.add(event);
        	}
        	if (selEvents.size() <= 0)
        		return -2;
        	else if (te.getText().length() <= 0)
        		return -3;
        	else {
        		story = new Story(te.getText().toString(), uId, selEvents);
        		return Communication.addStory(story);
        	}
        }

        protected void onPostExecute(Integer result) {
        	switch (result) {
				case -3:
					Toast.makeText(activity, "Your story must have a title", Toast.LENGTH_SHORT).show();
					break;
				case -2:
					Toast.makeText(activity, "Your story must have at least one event", Toast.LENGTH_SHORT).show();
					break;
				case -1:
					Toast.makeText(activity, "Your story could not be saved. Please check your internet access and retry", Toast.LENGTH_SHORT).show();
					break;
				default:
					Toast.makeText(activity, "Story successfully saved", Toast.LENGTH_SHORT).show();
					if (storyList != null) {
	               		Gen.appendLog("StoryNewFragment$SaveStoryTask::onPostExecute> Adding new story and going back to list");
	               		storyList.add(0, story);
	               		activity.sendStoryList(storyList);
	               		activity.getFragmentManager().popBackStackImmediate();
	               	 }
					break;
			}
        	pg.dismiss();
        }
          
        @Override
  		protected void onCancelled() {
        	pg.dismiss();
  		}
     }
}

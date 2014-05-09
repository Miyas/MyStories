package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mjumel.mystories.adapters.NewStoryListAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;

public class StoryNewFragment extends Fragment {

	private ListView lv;
	private EditText te;
	private ProgressDialog pg;
	private NewStoryListAdapter adapter;
	
	private String uId = null;
	private List<Event> eventList = null;
	private boolean checkAllSelected = false;
	private boolean hideAllText = false;
	private boolean hideAllStars = false;
	
	private boolean firstRun = false;
	
    public StoryNewFragment()
    {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
    	eventList = new ArrayList<Event>();
		adapter = new NewStoryListAdapter(getActivity(), eventList);
		uId = (String)getExtra("uid");
		firstRun = true;
		
		Gen.appendLog("NewStoryFragment::onCreate> uId=" + uId);
		Gen.appendLog("NewStoryFragment::onCreate> origin=" + (String)getExtra("origin"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("NewStoryFragment::onCreateView> Starting");
    	
    	View view = inflater.inflate(R.layout.fragment_new_story,container, false);
    	te = (EditText) view.findViewById(R.id.new_story_title);
		lv = (ListView) view.findViewById(R.id.new_story_listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(viewEvent);
		
		if (uId != null && firstRun)
			new DownloadEventsTask().execute(uId);
		
		Gen.appendLog("NewStoryFragment::onCreateView> Ending");
		return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_new_story, menu);
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
        	adapter.notifyDataSetChanged();
            return true;
        case R.id.new_story_save:
        	Gen.appendLog("NewStoryFragment::onOptionsItemSelected> Menu new_story_save");
        	new SaveStoryTask(uId).execute(eventList);
            return true;
        case R.id.new_story_filter:
        	Gen.appendLog("NewStoryFragment::onOptionsItemSelected> Menu new_story_filter");
        	Toast.makeText(getActivity(), "TODO", Toast.LENGTH_SHORT).show();
        	// TODO
            return true;
        case R.id.new_story_hide_text:
        	Gen.appendLog("NewStoryFragment::onOptionsItemSelected> Menu new_story_hide_text");
        	for (Event event : eventList) {
        		if (hideAllText) {
        			event.hideText();
        		} else {
        			event.shownText();
        		}
        	}
        	hideAllText = !hideAllText;
        	adapter.notifyDataSetChanged();
            return true;
        case R.id.new_story_hide_stars:
        	Gen.appendLog("NewStoryFragment::onOptionsItemSelected> Menu new_story_hide_stars");
        	for (Event event : eventList) {
        		if (hideAllStars) {
        			event.hideText();
        		} else {
        			event.shownText();
        		}
        	}
        	hideAllStars = !hideAllStars;
        	adapter.notifyDataSetChanged();
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
    private OnItemClickListener viewEvent = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Event event = (Event)parent.getItemAtPosition(position);
            Gen.appendLog("NewStoryFragment::viewEvent> Select event#" + ((Event)parent.getItemAtPosition(position)).getEventId() + " / Current state : " + event.isSelected());
            event.setSelected(!event.isSelected());
            adapter.notifyDataSetChanged();
            Gen.appendLog("NewStoryFragment::viewEvent> Select event#" + ((Event)parent.getItemAtPosition(position)).getEventId() + " / New state : " + event.isSelected());
		}
	};
	
    
	/***************************************************************************************
	 *
	 *                                Misc functions
	 * 
	 ***************************************************************************************/
	private Object getExtra(String id)
    {
    	if (this.getActivity().getIntent().getExtras() != null)
			return this.getActivity().getIntent().getExtras().get(id);
    	else
    		return null;
    }    
    
    
	/***************************************************************************************
	 *
	 *                                DownloadEventsTask Class
	 * 
	 ***************************************************************************************/
	class DownloadEventsTask extends AsyncTask<String, Integer, List<Event>>
    {
        protected void onPreExecute() {
			pg = new ProgressDialog(getActivity());
			pg.setTitle("Loading events");
			pg.setMessage("Please wait...");
			pg.setCancelable(true);
			pg.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", 
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							cancel(true);				
						}});
			pg.show();
        } 

        protected List<Event> doInBackground(String ...params) {
			return Communication.getUserEvents((String)params[0]);
        } 

        protected void onPostExecute(List<Event> result) {     
			eventList.clear();
			if(result == null) {
              	Event event = new Event();
              	event.setComment("No event available");
              	eventList.add(event);
            } else {
        		eventList.addAll((List<Event>)result);
            }
            adapter.notifyDataSetChanged();
            Gen.appendLog("NewStoryFragment$DownloadEventsTask::onPostExecute> Nb of events downloaded : " + eventList.size());
        	firstRun = false;
        	pg.dismiss();
        }
          
        @Override
  		protected void onCancelled() {
        	firstRun = false;
        	pg.dismiss();
  		}
     }
	
	
	/***************************************************************************************
	 *
	 *                                SaveStoryTask Class
	 * 
	 ***************************************************************************************/
	class SaveStoryTask extends AsyncTask<List<Event>, Integer, Integer> {
		
		private String userId = null;
		
		public SaveStoryTask(String uId) {
			userId = uId;
		}

        protected void onPreExecute() {
   			pg = new ProgressDialog(getActivity());
			pg.setTitle("Saving Story");
			pg.setMessage("Please wait...");
			pg.setCancelable(true);
			pg.setButton(ProgressDialog.BUTTON_NEUTRAL, "Cancel", 
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							cancel(true);				
						}});
			pg.show();
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
        	else
        		return Communication.addStory(new Story(te.getText().toString(), userId, selEvents));
        }

        protected void onPostExecute(Integer result) {
        	pg.setButton(ProgressDialog.BUTTON_NEUTRAL, "OK", 
    			new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						pg.dismiss();
						
			}});
        	pg.getButton(ProgressDialog.BUTTON_NEUTRAL).invalidate();
			switch (result) {
				case -3:
					pg.setMessage("Your story must have a title");
					break;
				case -2:
					pg.setMessage("Your story must have at least one event");
					break;
				case -1:
					pg.setMessage("Your story could not be saved. Please check your internet access and retry");
					break;
				default:
					pg.setMessage("Story successfully saved");
					break;
			}
        	//pg.dismiss();
        }
          
        @Override
  		protected void onCancelled() {
        	pg.dismiss();
  		}
     }
}

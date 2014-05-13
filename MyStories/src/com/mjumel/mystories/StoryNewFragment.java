package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;



import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
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
	private Menu mMenu;
	
	private String uId = null;
	private ArrayList<Story> storyList = null;
	private List<Event> eventList = null;
	private List<Event> eventSelected = null;
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
    	eventSelected = new ArrayList<Event>();
		adapter = new NewStoryListAdapter(getActivity(), eventList);
		storyList = getArguments().getParcelableArrayList("stories"); 
		uId = (String)getExtra("uid");
		firstRun = true;
		
		Gen.appendLog("NewStoryFragment::onCreate> uId=" + uId);
		Gen.appendLog("NewStoryFragment::onCreate> origin=" + (String)getExtra("origin"));
		Gen.appendLog("NewStoryFragment::onCreate> storyList nb = " + storyList.size());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("NewStoryFragment::onCreateView> Starting");
    	
    	View view = inflater.inflate(R.layout.fragment_new_story,container, false);
    	te = (EditText) view.findViewById(R.id.new_story_title);
    	lv = (ListView) view.findViewById(R.id.new_story_listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(selectEvent);
		
		if (uId != null && firstRun)
			new DownloadEventsTask().execute(uId);
		
		Gen.appendLog("NewStoryFragment::onCreateView> Ending");
		return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_new_story, menu);
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
        	updateMenu();
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
        			event.showText();
        		}
        	}
        	hideAllText = !hideAllText;
        	adapter.notifyDataSetChanged();
            return true;
        case R.id.new_story_hide_stars:
        	Gen.appendLog("NewStoryFragment::onOptionsItemSelected> Menu new_story_hide_stars");
        	for (Event event : eventList) {
        		if (hideAllStars) {
        			event.hideStars();
        		} else {
        			event.showStars();
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
    private OnItemClickListener selectEvent = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Event event = (Event)parent.getItemAtPosition(position);
            Gen.appendLog("NewStoryFragment::viewEvent> Select event#" + ((Event)parent.getItemAtPosition(position)).getEventId() + " / Current state : " + event.isSelected());
            selectEvent(event);
            Gen.appendLog("NewStoryFragment::viewEvent> Select event#" + ((Event)parent.getItemAtPosition(position)).getEventId() + " / New state : " + event.isSelected());
		}
	};
	
	private void selectEvent(Event event) {
		if (event.isSelected()) {
        	event.setSelected(false);
        	eventSelected.remove(event);
        } else {
        	event.setSelected(true);
        	eventSelected.add(event);
        }
		adapter.notifyDataSetChanged();
		updateMenu();
	}
	
	private void updateMenu() {
		Gen.appendLog("NewStoryFragment::updateMenu> Starting");
		if (eventSelected.size() > 0) {
        	mMenu.findItem(R.id.new_story_save).setVisible(true);
        	Gen.appendLog("NewStoryFragment::updateMenu> Showing action Save");
		} else {
        	mMenu.findItem(R.id.new_story_save).setVisible(false);
        	Gen.appendLog("NewStoryFragment::updateMenu> Hiding action Save");
		}
		Gen.appendLog("NewStoryFragment::updateMenu> Ending");
	}
	
	//Fonction appelée au clic d'une des checkbox
	public void cbChecked(View v) {
		CheckBox cb = (CheckBox) v;
		int position = Integer.parseInt(cb.getTag().toString());
		selectEvent(eventList.get(position));
	}
	
    
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
		private Story story = null;
		
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
        	else {
        		story = new Story(te.getText().toString(), userId, selEvents);
        		return Communication.addStory(story);
        	}
        }

        protected void onPostExecute(Integer result) {
        	if (pg.isShowing()) 
        		pg.dismiss();
        	
        	switch (result) {
				case -3:
					Toast.makeText(getActivity(), "Your story must have a title", Toast.LENGTH_SHORT).show();
					break;
				case -2:
					Toast.makeText(getActivity(), "Your story must have at least one event", Toast.LENGTH_SHORT).show();
					break;
				case -1:
					Toast.makeText(getActivity(), "Your story could not be saved. Please check your internet access and retry", Toast.LENGTH_SHORT).show();
					break;
				default:
					Toast.makeText(getActivity(), "Story successfully saved", Toast.LENGTH_SHORT).show();
					if (storyList != null) {
	               		Gen.appendLog("StoryNewFragment$SaveStoryTask::onPostExecute> Adding new story and going back to list");
	               		storyList.add(0, story);
	               		((DrawerActivity)getActivity()).sendStoryList(storyList);
	               		getActivity().getSupportFragmentManager().popBackStackImmediate();
	               	 }
					break;
			}
        	//pg.dismiss();
        }
          
        @Override
  		protected void onCancelled() {
        	if (pg.isShowing()) 
        		pg.dismiss();
  		}
     }
}

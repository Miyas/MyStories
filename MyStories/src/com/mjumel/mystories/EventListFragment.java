package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mjumel.mystories.adapters.DialogLinkAdapter;
import com.mjumel.mystories.adapters.EventListAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;

public class EventListFragment extends Fragment {

	private DrawerActivity activity;
	
	private ListView lv;
	private ProgressDialog pg;
	private EventListAdapter adapter;
	
	private PullToRefreshLayout mPullToRefreshLayout;
	
	private String uId = null;
	private List<Event> eventList = null;
	private List<Story> storyList = null;
	
	private Menu mMenu;
	
	// LinkDialog variables
	private ListView dlv;
	private TextView dtv;
	private int storiesChecked = 0;
	
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
    	activity = (DrawerActivity) getActivity();
    	
		eventList = activity.getEventList();
		storyList = activity.getStoryList();
		uId = activity.getUserId();
		
		adapter = new EventListAdapter(activity, this, eventList);
		
		Gen.appendLog("EventListFragment::onCreate> uId=" + uId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("EventListFragment::onCreateView> Starting");
    	
    	View view = inflater.inflate(R.layout.fragment_my_events_pull,container, false);
    	
    	mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(activity)
                .allChildrenArePullable()
                .listener(refreshEvent)
                .setup(mPullToRefreshLayout);
		
		lv = (ListView) view.findViewById(R.id.my_events_listView_pull);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(viewEvent);
		
		Gen.appendLog("EventListFragment::onCreateView> Ending");
		return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_my_events, menu);
        menu.findItem(R.id.my_events_search).getActionView();
        mMenu = menu;
    }
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.my_events_add:
	        	Gen.appendLog("EventListFragment::onOptionsItemSelected> Display new event fragment");
	            ((DrawerActivity)activity).changeFragment(new EventNewFragment(), null);
	            return true;
	        case R.id.my_events_delete:
	        	new DeleteEventsTask().execute();
	        	return true;
	        case R.id.my_events_link:
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
    	activity.setTitle("My Events");
    	lv.setAdapter(adapter);
    	    	
    	eventList = activity.getEventList(); 
   		adapter.notifyDataSetChanged();
    	
    	Gen.appendLog("EventListFragment::onResume> Ending");
    }
    
    public void updateMenu(boolean delete)
    {
   		mMenu.findItem(R.id.my_events_delete).setVisible(delete);
   		mMenu.findItem(R.id.my_events_link).setVisible(delete);
   		mMenu.findItem(R.id.my_events_search).setVisible(!delete);
   		mMenu.findItem(R.id.my_events_add).setVisible(!delete);
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
    private OnItemClickListener viewEvent = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Gen.appendLog("EventListFragment::viewEvent> Display event#" + ((Event)parent.getItemAtPosition(position)).getEventId());
            Gen.appendLog("EventListFragment::viewEvent> Display event#" + eventList.get(position).getEventId());
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            ((DrawerActivity)activity).changeFragment(new EventViewFragment(), bundle);
		}
	};
	
	private OnRefreshListener refreshEvent = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			if (uId != null) {
				new DownloadEventsTask().execute(uId);
			}
		}
	};
	
    
	/***************************************************************************************
	 *
	 *                                Misc functions
	 * 
	 ***************************************************************************************/
	private void linkDialog() {
    	Gen.appendLog("EventViewFragment::linkDialog> Starting");
    	
    	if (storyList == null) {
    		Gen.appendError("EventViewFragment::linkDialog> storyList is empty");
    		Toast.makeText(activity, "You don't have any story", Toast.LENGTH_SHORT).show();
    		return;
    	}

    	Gen.appendLog("EventViewFragment::linkDialog> Here");
    	AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(activity);
        myAlertDialog.setTitle("Stories to link to");

        DialogLinkAdapter dAdapter = new DialogLinkAdapter(activity, this, storyList);
        
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dView = inflater.inflate(R.layout.dialog_link_events, null);
        dtv = (TextView) dView.findViewById(R.id.dialog_link_events_title);
        dlv = (ListView) dView.findViewById(R.id.dialog_link_events_listView);
        dlv.setAdapter(dAdapter);
        //dlv.setOnItemClickListener();
		
		myAlertDialog.setView(dView);

        myAlertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
                    	if (storiesChecked <= 0 && dtv.getText().length() <= 0) {
                    		Toast.makeText(activity, "You have to enter a title or choose at least on existing story", Toast.LENGTH_SHORT).show();
                    	} else if (storiesChecked > 0 && dtv.getText().length() > 0) {
                    		Toast.makeText(activity, "You have to choose between creating a new story or linking to existing one", Toast.LENGTH_SHORT).show();
                    	} else if (storiesChecked > 0)
                    		new LinkEventsTask().execute();
                    	else if (dtv.getText().length() > 0)
                    		new CreateStoryTask().execute();
                    }
                });
        myAlertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						for (Story story : storyList) {
							story.setSelected(false);
						}
					}
        		});
        myAlertDialog.show();
    }
	
	public void setNbStoriesChecked(int nb) {
		storiesChecked = nb;
	}
    
    
	/***************************************************************************************
	 *
	 *                                DownloadEventsTask Class
	 * 
	 ***************************************************************************************/
	private class DownloadEventsTask extends AsyncTask<String, Integer, List<Event>>
    {
          protected void onPreExecute() {     
          	  pg = ProgressDialog.show(activity, "", "Loading events...", true);
          } 

          protected List<Event> doInBackground(String ...params) {  
                return Communication.getUserEvents(params[0]);
          } 

          protected void onPostExecute(List<Event> result) {     
                if(result == null) {
                	result = new ArrayList<Event>();
                	Event event = new Event();
                	event.setComment("No event available");
	                result.add(event);
                }
                eventList.clear();
                eventList.addAll(result);
                activity.sendEventList(eventList);
            	adapter.notifyDataSetChanged();
            	mPullToRefreshLayout.setRefreshComplete();
            	pg.dismiss();
            	Gen.appendLog("EventListFragment::DownloadEventsTask::onPostExecute> Nb of events downloaded : " + eventList.size());
          }
          
          @Override
  		protected void onCancelled() {
        	  mPullToRefreshLayout.setRefreshComplete();
        	  pg.dismiss();
  		}
     }
	
	
	/***************************************************************************************
	 *
	 *                                DeleteEventsTask Class
	 * 
	 ***************************************************************************************/
	private class DeleteEventsTask extends AsyncTask<String, Integer, Boolean>
	{
		private ProgressDialog pg;
		 
		protected void onPreExecute() {
			pg = ProgressDialog.show(activity, "", "Deleting events...", true);
		} 

		protected Boolean doInBackground(String ...params) {
			return Communication.deleteEvents(uId, eventList);
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				Gen.appendError("EventListFragment$DeleteEventsTask::onPostExecute> Error while deleting stories");
				Toast.makeText(activity, "Events could not be deleted. Please retry later", Toast.LENGTH_SHORT).show();
			} else {
				List<Event> list = new ArrayList<Event>();
				for (Event e : eventList) {
					if (e.isSelected()) {
						list.add(e);
					}
				}
				for (Event e : list) {
					eventList.remove(e);
				}
				list = null;
				activity.sendEventList(eventList);
				adapter.notifyDataSetChanged();
				updateMenu(false);
				Toast.makeText(activity, "Events deleted", Toast.LENGTH_SHORT).show();
			}
			pg.dismiss();
		}
    
		@Override
		protected void onCancelled() {
			Toast.makeText(activity, "Operation cancelled", Toast.LENGTH_SHORT).show();
			pg.dismiss();
		}
	}
	
	
	/***************************************************************************************
	 *
	 *                                LinkEventsTask Class
	 * 
	 ***************************************************************************************/
	private class LinkEventsTask extends AsyncTask<String, Integer, Boolean>
	{
		private ProgressDialog pg = null;
		 
		protected void onPreExecute() {
			pg = ProgressDialog.show(activity, "", "Linking events...", true);
		} 

		protected Boolean doInBackground(String ...params) {
			return Communication.linkEvents(uId, eventList, storyList);
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				Gen.appendError("EventListFragment$LinkEventsTask::onPostExecute> Error while linking stories");
				Toast.makeText(activity, "Events could not be linked. Please retry later", Toast.LENGTH_SHORT).show();
			} else {
				activity.sendEventList(eventList);
				Toast.makeText(activity, "Events linked", Toast.LENGTH_SHORT).show();
			}
			for (Event event : eventList) {
				event.setSelected(false);
			}
			for (Story story : storyList) {
				story.setSelected(false);
			}
			adapter.notifyDataSetChanged();
			updateMenu(false);
			pg.dismiss();
		}
   
		@Override
		protected void onCancelled() {
			Toast.makeText(activity, "Operation cancelled", Toast.LENGTH_SHORT).show();
			pg.dismiss();
		}
	}
	
	
	/***************************************************************************************
	 *
	 *                                CreateStoryTask Class
	 * 
	 ***************************************************************************************/
	class CreateStoryTask extends AsyncTask<String, Integer, Integer> {
		
		private ProgressDialog pg;
		private Story story = null;
		
		protected void onPreExecute() {
			pg = ProgressDialog.show(activity, "", "Creating new story...", true);
			hideKeyboard();
		} 
		
		protected Integer doInBackground(String ...params) {
			List<Event> selEvents = new ArrayList<Event>();
			for (Event event : eventList) {
				if (event.isSelected()) {
					selEvents.add(event);
				}
			}
			story = new Story(dtv.getText().toString(), uId, selEvents);
			return Communication.addStory(story);
		}
		
		protected void onPostExecute(Integer result) {
			switch (result) {
				case -1:
					Toast.makeText(activity, "Your story could not be created. Please check your internet access and retry", Toast.LENGTH_SHORT).show();
					break;
				default:
					Toast.makeText(activity, "Story successfully created", Toast.LENGTH_SHORT).show();
					if (storyList != null) {
						Gen.appendLog("EventListFragment$CreateStoryTask::onPostExecute> Creating new story");
						storyList.add(0, story);
						activity.sendStoryList(storyList);
					}
					for (Event event : eventList) {
						event.setSelected(false);
					}
					adapter.notifyDataSetChanged();
					updateMenu(false);
					break;
			}
			pg.dismiss();
		}
			     
		@Override
		protected void onCancelled() {
			Toast.makeText(activity, "Operation cancelled", Toast.LENGTH_SHORT).show();
			story = null;
			pg.dismiss();
		}
	}
}

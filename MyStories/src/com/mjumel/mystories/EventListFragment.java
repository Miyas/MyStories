package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.mjumel.mystories.adapters.EventListAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;

public class EventListFragment extends Fragment {

	private ListView lv;
	private ProgressDialog pg;
	private EventListAdapter adapter;
	
	private PullToRefreshLayout mPullToRefreshLayout;
	
	private String uId = null;
	private List<Event> eventList = null;
	private List<Story> storyList = null;
	
	private boolean firstRun = false;
	private Menu mMenu;
	
    	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
		eventList = (List<Event>) getExtra("events");
		if (eventList == null)
			eventList = new ArrayList<Event>();
		
		adapter = new EventListAdapter(getActivity(), this, eventList);
		uId = (String)getExtra("uid");
		firstRun = ((String)getExtra("origin")).equals("splash");
		
		Gen.appendLog("EventListFragment::onCreate> uId=" + uId);
		Gen.appendLog("EventListFragment::onCreate> origin=" + (String)getExtra("origin"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("EventListFragment::onCreateView> Starting");
    	
    	View view = inflater.inflate(R.layout.fragment_my_events_pull,container, false);
    	
    	List<Event> events = ((DrawerActivity)getActivity()).getEventList(); 
    	if (events != null) {
    		eventList.clear();
    		eventList.addAll(events);
    		events = null;
    		adapter.notifyDataSetChanged();
    	}
    	
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(refreshEvent)
                .setup(mPullToRefreshLayout);
		
		lv = (ListView) view.findViewById(R.id.my_events_listView_pull);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(viewEvent);
		
		if (uId != null && eventList.size() <= 0 && firstRun)
			new DownloadEventsTask().execute(uId);
		
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
    
    @SuppressWarnings("unchecked")
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.my_events_add:
	        	Gen.appendLog("EventListFragment::onOptionsItemSelected> Display new event fragment");
	        	Bundle bundle = new Bundle();
	        	bundle.putParcelableArrayList("events", new ArrayList<Event>(eventList));
	            ((DrawerActivity)getActivity()).changeFragment(new EventNewFragment(), bundle);
	            return true;
	        case R.id.my_events_delete:
	        	new DeleteEventsTask().execute(eventList);
	        	return true;
	        case R.id.my_events_link:
	        	if (storyList == null) {
	        		storyList = new ArrayList<Story>();
	        		new DownloadStoriesTask().execute(uId);
	        	} else {
	        		linkDialog();
	        	}
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
    	getActivity().setTitle("My Events");
    	lv.setAdapter(adapter);
    	Gen.appendLog("EventListFragment::onResume> Ending");
    }
    
    public void updateMenu(boolean delete)
    {
   		mMenu.findItem(R.id.my_events_delete).setVisible(delete);
   		mMenu.findItem(R.id.my_events_link).setVisible(delete);
   		mMenu.findItem(R.id.my_events_search).setVisible(!delete);
   		mMenu.findItem(R.id.my_events_add).setVisible(!delete);
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
            bundle.putParcelableArrayList("events", new ArrayList<Event>(eventList));
            ((DrawerActivity)getActivity()).changeFragment(new EventViewFragment(), bundle);
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
	private Object getExtra(String id)
    {
    	if (this.getActivity().getIntent().getExtras() != null)
			return this.getActivity().getIntent().getExtras().get(id);
    	else
    		return null;
    }
	
	private void linkDialog() {
    	Gen.appendLog("EventViewFragment::linkDialog> Starting");
    	final ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();
    	
    	if (storyList == null) {
    		Gen.appendError("EventViewFragment::linkDialog> storyList is empty");
    		Toast.makeText(getActivity(), "You don't have any story", Toast.LENGTH_SHORT);
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
		            if (isChecked) {
		                mSelectedItems.add(pos);
		            } else if (mSelectedItems.contains(pos)) {
		                mSelectedItems.remove(Integer.valueOf(pos));
		            }
		        }
        });

        myAlertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @SuppressWarnings("unchecked")
					public void onClick(DialogInterface arg0, int arg1) {
                    	new LinkEventsTask(mSelectedItems).execute(eventList);
                    }
                });
        myAlertDialog.setNegativeButton("Cancel", null);
        myAlertDialog.show();
    }
    
    
	/***************************************************************************************
	 *
	 *                                DownloadEventsTask Class
	 * 
	 ***************************************************************************************/
	class DownloadEventsTask extends AsyncTask<String, Integer, List<Event>>
    {
          protected void onPreExecute()
          {     
        	  //super.onPreExecute();
          	  pg = ProgressDialog.show(getActivity(), "", "Loading events...", true);
          } 

          protected List<Event> doInBackground(String ...params)
          {  
                return Communication.getUserEvents(params[0]);
          } 

          protected void onPostExecute(List<Event> result)
          {     
                //super.onPostExecute(result);
                if(result == null)
                {
                	result = new ArrayList<Event>();
                	Event event = new Event();
                	event.setComment("No event available");
	                result.add(event);
                }
                eventList.clear();
                eventList.addAll(result);
            	adapter.notifyDataSetChanged();
            	mPullToRefreshLayout.setRefreshComplete();
            	firstRun = false;
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
	private class DeleteEventsTask extends AsyncTask<List<Event>, Integer, Boolean>
	{
		private ProgressDialog pg;
		 
		protected void onPreExecute() {
			pg = ProgressDialog.show(getActivity(), "", "Deleting events...", true);
		} 

		protected Boolean doInBackground(List<Event> ...params) {
			return Communication.deleteEvents(uId, params[0]);
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				Gen.appendError("EventListFragment$DeleteEventsTask::onPostExecute> Error while deleting stories");
				Toast.makeText(getActivity(), "Events could not be deleted. Please retry later", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(), "Events deleted", Toast.LENGTH_SHORT).show();
				adapter.notifyDataSetChanged();
			}
			pg.dismiss();
		}
    
		@Override
		protected void onCancelled() {
			Toast.makeText(getActivity(), "Openration cancelled", Toast.LENGTH_SHORT).show();
			pg.dismiss();
		}
	}
	
	
	/***************************************************************************************
	 *
	 *                                DownloadStoriesTask Class
	 * 
	 ***************************************************************************************/
	private class DownloadStoriesTask extends AsyncTask<String, Integer, List<Story>>
	{
        protected void onPreExecute() {
        	pg = ProgressDialog.show(getActivity(), "", "Loading stories...", true);
        } 

        protected List<Story> doInBackground(String ...params) {  
        	return Communication.getUserStories(params[0]);
        } 

		protected void onPostExecute(List<Story> result) {     
			if(result == null)
			{
				result = new ArrayList<Story>();
				Story story = new Story();
				story.setTitle("No story available");
				result.add(story);
			}
			storyList.clear();
			storyList.addAll(result);
			pg.dismiss();
			Gen.appendLog("EventListFragment::DownloadStoriesTask::onPostExecute> Nb of stories downloaded : " + storyList.size());
			linkDialog();
		}
         
		@Override
		protected void onCancelled() {
			mPullToRefreshLayout.setRefreshComplete();
			pg.dismiss();
		}
	}
	
	
	/***************************************************************************************
	 *
	 *                                LinkEventsTask Class
	 * 
	 ***************************************************************************************/
	private class LinkEventsTask extends AsyncTask<List<Event>, Integer, Boolean>
	{
		private ProgressDialog pg = null;
		private List<Story> stories = new ArrayList<Story>();
		
		LinkEventsTask(ArrayList<Integer> index) {
			for(int i : index)
				stories.add(storyList.get(i));
		}
		 
		protected void onPreExecute() {
			pg = ProgressDialog.show(getActivity(), "", "Linking events...", true);
		} 

		protected Boolean doInBackground(List<Event> ...params) {
			return Communication.linkEvents(uId, params[0], stories);
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				Gen.appendError("EventListFragment$LinkEventsTask::onPostExecute> Error while linking stories");
				Toast.makeText(getActivity(), "Events could not be linked. Please retry later", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(), "Events linked", Toast.LENGTH_SHORT).show();
			}
			stories = null;
			pg.dismiss();
		}
   
		@Override
		protected void onCancelled() {
			Toast.makeText(getActivity(), "Openration cancelled", Toast.LENGTH_SHORT).show();
			stories = null;
			pg.dismiss();
		}
	}
}

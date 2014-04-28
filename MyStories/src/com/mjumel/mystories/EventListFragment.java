package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.ProgressDialog;
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
import android.widget.ListView;

import com.mjumel.mystories.adapters.EventListAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class EventListFragment extends Fragment {

	private ListView lv;
	private ProgressDialog pg;
	private EventListAdapter adapter;
	
	private PullToRefreshLayout mPullToRefreshLayout;
	
	private String uId = null;
	private List<Event> eventList = null;
	
    public EventListFragment()
    {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("EventListFragment::onCreateView> Starting");
    	
    	View view = inflater.inflate(R.layout.fragment_my_events_pull,container, false);
    	
    	// Now find the PullToRefreshLayout to setup
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                // Mark All Children as pullable
                .allChildrenArePullable()
                // Set a OnRefreshListener
                .listener(refreshEvent)
                // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);
    	
		
		lv = (ListView) view.findViewById(R.id.my_events_listView_pull);
		
		uId = (String)getExtra("uid");
		Gen.appendLog("EventListFragment::onCreateView> uId=" + uId);
		if (uId != null && eventList == null)
			new DownloadEventsTask().execute(uId);
		
		lv.setOnItemClickListener(viewEvent);
		
		Gen.appendLog("EventListFragment::onCreateView> Ending");
		return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_list_events, menu);
        menu.findItem(R.id.list_search).getActionView();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
        case R.id.list_add_event:
        	Gen.appendLog("EventListFragment::onOptionsItemSelected> Display new event fragment");
            ((DrawerActivity)getActivity()).changeFragment(new NewEventFragment(), null);
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
    	lv.setAdapter(adapter);
    	Gen.appendLog("EventListFragment::onResume> Ending");
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
            Bundle bundle = new Bundle();
            bundle.putParcelable("event", (Event)parent.getItemAtPosition(position));
            ((DrawerActivity)getActivity()).changeFragment(new ViewEventFragment(), bundle);
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
    
    
	/***************************************************************************************
	 *
	 *                                DownloadEventsTask Class
	 * 
	 ***************************************************************************************/
	class DownloadEventsTask extends AsyncTask<String, Integer, List<Event>>
    {
          protected void onPreExecute()
          {     super.onPreExecute();
          		pg = ProgressDialog.show(getActivity(), "", "Loading events...", true);
          } 

          protected List<Event> doInBackground(String ...params)
          {  
                return Communication.getUserEvents(params[0]);
          } 

          protected void onPostExecute(List<Event> result)
          {     
                super.onPostExecute(result);
                if(result == null)
                {
                	result = new ArrayList<Event>();
                	Event event = new Event();
                	event.setComment("No event available");
	                result.add(event);
                }
                eventList = result;
                adapter = new EventListAdapter(getActivity(), result);
            	lv.setAdapter(adapter);                	
            	pg.dismiss();
          }
          
          @Override
  		protected void onCancelled() {
        	  pg.dismiss();
  		}
     }
}

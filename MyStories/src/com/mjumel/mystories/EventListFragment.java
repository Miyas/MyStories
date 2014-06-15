package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
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
	
	private boolean firstRun = false;
	
    	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
		eventList = (List<Event>) getExtra("events");
		if (eventList == null)
			eventList = new ArrayList<Event>();
		
		adapter = new EventListAdapter(getActivity(), eventList);
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
        menu.findItem(R.id.list_search).getActionView();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.list_add_event:
        	Gen.appendLog("EventListFragment::onOptionsItemSelected> Display new event fragment");
        	Bundle bundle = new Bundle();
        	bundle.putParcelableArrayList("events", new ArrayList<Event>(eventList));
            ((DrawerActivity)getActivity()).changeFragment(new EventNewFragment(), bundle);
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
}

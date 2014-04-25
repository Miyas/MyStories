package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.mjumel.mystories.adapters.EventListAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;

public class EventListFragment extends Fragment {

	private ListView lv;
	private Button btnAddEvent;
	private ProgressDialog pg;
	private EventListAdapter adapter;
	
	private List<Event> eventList = null;
	
    public EventListFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("EventListFragment::onCreateView> Starting");
    	
		View view = inflater.inflate(R.layout.fragment_my_events,container, false);
		lv = (ListView) view.findViewById(R.id.my_events_listView);
		btnAddEvent = (Button) view.findViewById(R.id.my_events_button);
		
		String uId = (String)getExtra("uid");
		Gen.appendLog("EventListFragment::onCreateView> uId=" + uId);
		if (uId != null && eventList == null)
			new DownloadEventsTask().execute(uId);
		
		btnAddEvent.setOnClickListener(addNewEvent);
		lv.setOnItemClickListener(viewEvent);
		
		Gen.appendLog("EventListFragment::onCreateView> Ending");
		return view;
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
	private OnClickListener addNewEvent = new OnClickListener() {
    	@Override
        public void onClick(View v) {
             Gen.appendLog("EventListFragment::addNewEvent> Display new event fragment");
             ((DrawerActivity)getActivity()).changeFragment(new NewEventFragment(), null);
        }
	};
    
    private OnItemClickListener viewEvent = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Gen.appendLog("EventListFragment::viewEvent> Display event#" + ((Event)parent.getItemAtPosition(position)).getEventId());
            Bundle bundle = new Bundle();
            bundle.putParcelable("event", (Event)parent.getItemAtPosition(position));
            ((DrawerActivity)getActivity()).changeFragment(new ViewEventFragment(), bundle);
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
     }
}

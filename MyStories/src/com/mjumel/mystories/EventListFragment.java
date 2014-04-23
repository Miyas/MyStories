package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mjumel.mystories.adapters.EventListAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;

public class EventListFragment extends Fragment {

	private ListView lv;
	private List<Event> eventList = null;
	private ProgressDialog pg;
	
    public EventListFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("EventListFragment::onCreateView> Starting");
    	
		View view = inflater.inflate(R.layout.fragment_my_events,container, false);
		lv = (ListView) view.findViewById(R.id.my_events_listView);
		
		String uId = (String)getExtra("uid");
		Gen.appendLog("EventListFragment::onCreateView> uId=" + uId);
		if (uId != null && eventList == null)
			new DownloadEventsTask().execute(uId);
		
		Gen.appendLog("EventListFragment::onCreateView> Ending");
		return view;
    }
    
    private Object getExtra(String id)
    {
    	if (this.getActivity().getIntent().getExtras() != null)
			return this.getActivity().getIntent().getExtras().get(id);
    	else
    		return null;
    }
    
    
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
                	event.SetComment("No event available");
	                result.add(event);
                }
                eventList = result;
                EventListAdapter adapter = new EventListAdapter(getActivity(), result);
            	lv.setAdapter(adapter);                	
            	pg.dismiss();
          }
     }
}

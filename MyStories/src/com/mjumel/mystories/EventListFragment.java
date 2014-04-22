package com.mjumel.mystories;

import java.util.List;

import android.app.Fragment;
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
	private List<Event> eventList;
	
    public EventListFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.writeLog("EventListFragment::onCreateView> Starting");
    	
		View view = inflater.inflate(R.layout.fragment_my_events,container, false);
		lv = (ListView) view.findViewById(R.id.my_events_listView);
		
		String uId = (String)getExtra("uid");
		if (uId != null)
			eventList = Communication.getUserEvents(uId);
		
		EventListAdapter adapter = new EventListAdapter(this.getActivity(), eventList);
    	lv.setAdapter(adapter);
		
		Gen.writeLog("EventListFragment::onCreateView> Ending");
		return view;
    }
    
    private Object getExtra(String id)
    {
    	if (this.getActivity().getIntent().getExtras() != null)
			return this.getActivity().getIntent().getExtras().get(id);
    	else
    		return null;
    }
}

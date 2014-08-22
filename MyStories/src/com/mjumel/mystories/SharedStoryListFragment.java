package com.mjumel.mystories;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
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

import com.mjumel.mystories.adapters.SharedStoryListAdapter;
import com.mjumel.mystories.tools.Gen;

public class SharedStoryListFragment extends Fragment {
	
	private DrawerActivity activity;
	private String title = MyStoriesApp.APPLICATION_NAME;

	private ListView lv;
	private SharedStoryListAdapter adapter;
	
	private String uId = null;
	private List<Story> storyList = null;
	
    public SharedStoryListFragment()
    {
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
    	activity = (DrawerActivity) getActivity();
    	
		storyList = activity.getSharedStoryList();
		uId = activity.getUserId();
    	
		adapter = new SharedStoryListAdapter(activity, storyList);
		
		if (getArguments() != null) {
			if (getArguments().containsKey("title"))
				title = getArguments().getString("title");
		}
		
		Gen.appendLog("SharedStoryListFragment::onCreate> uId=" + uId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("SharedStoryListFragment::onCreateView> Starting");
    	
    	View view = inflater.inflate(R.layout.fragment_my_stories,container, false);
		
		lv = (ListView) view.findViewById(R.id.my_stories_listView);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(viewStory);
		
		Gen.appendLog("SharedStoryListFragment::onCreateView> Ending");
		return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_shared_stories, menu);
        menu.findItem(R.id.my_stories_search).getActionView();
    }
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.my_stories_search:
        	Toast.makeText(getActivity(), "Search action", Toast.LENGTH_SHORT).show();
        	return true;
        case R.id.my_stories_filters:
        	Toast.makeText(getActivity(), "Show filters action", Toast.LENGTH_SHORT).show();
        	return true;
        case R.id.my_stories_switch:
        	Toast.makeText(getActivity(), "Switch display action", Toast.LENGTH_SHORT).show();
        	return true;
        case R.id.my_stories_hide_title:
        	Toast.makeText(getActivity(), "Hide title action", Toast.LENGTH_SHORT).show();
        	return true;
        case R.id.my_stories_hide_stars:
        	Toast.makeText(getActivity(), "Hide stars action", Toast.LENGTH_SHORT).show();
        	return true;
        default:
        	Toast.makeText(getActivity(), "TODO", Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	Gen.appendLog("SharedStoryListFragment::onResume> Starting");
    	lv.setAdapter(adapter);
    	activity.setTitle(title);
    	
    	storyList = activity.getSharedStoryList();
    	for (Story s : storyList)
    		s.setSelected(false);

   		adapter.notifyDataSetChanged();
   		
    	Gen.appendLog("SharedStoryListFragment::onResume> Ending");
    }
    
    
    /***************************************************************************************
	 *
	 *                                Event-based functions
	 * 
	 ***************************************************************************************/
    private OnItemClickListener viewStory = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Gen.appendLog("SharedStoryListFragment::viewStory> Display story#" + ((Story)parent.getItemAtPosition(position)).getStoryId());
            Intent intent = new Intent(getActivity().getApplicationContext(), StoryViewFragment.class);
            intent.putExtra("story", (Story)parent.getItemAtPosition(position));
			intent.putExtra("position", position);
			intent.putExtra("shared", true);
			startActivity(intent);
		}
	};
}

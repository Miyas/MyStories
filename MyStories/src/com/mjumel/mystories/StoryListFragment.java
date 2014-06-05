package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.ProgressDialog;
import android.content.Intent;
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

import com.mjumel.mystories.adapters.StoryListAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;

public class StoryListFragment extends Fragment {

	private ListView lv;
	private ProgressDialog pg;
	private StoryListAdapter adapter;
	private Menu mMenu;
	
	private PullToRefreshLayout mPullToRefreshLayout;
	
	private String uId = null;
	private List<Story> storyList = null;
	
	private boolean firstRun = false;
	
    public StoryListFragment()
    {
    }
    	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
    	storyList = (List<Story>) getExtra("stories");
    	if (storyList == null)
    		storyList = new ArrayList<Story>();
    	
		adapter = new StoryListAdapter(getActivity(), this, storyList);
		uId = (String)getExtra("uid");
		firstRun = ((String)getExtra("origin")).equals("splash");
		
		Gen.appendLog("StoryListFragment::onCreate> uId=" + uId);
		Gen.appendLog("StoryListFragment::onCreate> origin=" + (String)getExtra("origin"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("StoryListFragment::onCreateView> Starting");
    	
    	View view = inflater.inflate(R.layout.fragment_my_stories_pull,container, false);
    	
    	List<Story> stories = ((DrawerActivity)getActivity()).getStoryList(); 
    	if (stories != null) {
    		storyList.clear();
    		storyList.addAll(stories);
    		stories = null;
    		adapter.notifyDataSetChanged();
    	}
    	
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.my_stories_ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(refreshEvent)
                .setup(mPullToRefreshLayout);
		
		lv = (ListView) view.findViewById(R.id.my_stories_listView_pull);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(viewStory);
		
		if (uId != null && storyList.size() <= 0 && firstRun)
			new DownloadStoriesTask().execute(uId);
		
		Gen.appendLog("StoryListFragment::onCreateView> Ending");
		return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_my_stories, menu);
        menu.findItem(R.id.my_stories_search).getActionView();
        this.mMenu = menu;
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.my_stories_add:
        	Gen.appendLog("StoryListFragment::onOptionsItemSelected> Display new story fragment");
        	Bundle bundle = new Bundle();
        	bundle.putParcelableArrayList("stories", new ArrayList<Story>(storyList));
            ((DrawerActivity)getActivity()).changeFragment(new StoryNewFragment(), bundle);
            return true;
        case R.id.my_stories_cancel:
        	updateMenu(false);
        	return true;
        case R.id.my_stories_delete:
        	Toast.makeText(getActivity(), "Delete action", Toast.LENGTH_SHORT).show();
        	new DeleteStoryTask().execute(storyList);
        	return true;
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
    	Gen.appendLog("StoryListFragment::onResume> Starting");
    	lv.setAdapter(adapter);
    	getActivity().setTitle("My Stories");
    	Gen.appendLog("StoryListFragment::onResume> Ending");
    }
    
    public void updateMenu(boolean delete)
    {
   		mMenu.findItem(R.id.my_stories_delete).setVisible(delete);
   		mMenu.findItem(R.id.my_stories_cancel).setVisible(delete);
   		mMenu.findItem(R.id.my_stories_search).setVisible(!delete);
   		mMenu.findItem(R.id.my_stories_filters).setVisible(!delete);
   		mMenu.findItem(R.id.my_stories_add).setVisible(!delete);
   		mMenu.findItem(R.id.my_stories_switch).setVisible(!delete);
   		mMenu.findItem(R.id.my_stories_hide_title).setVisible(!delete);
   		mMenu.findItem(R.id.my_stories_hide_stars).setVisible(!delete);
    }
    
    
    /***************************************************************************************
	 *
	 *                                Event-based functions
	 * 
	 ***************************************************************************************/
    private OnItemClickListener viewStory = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Gen.appendLog("StoryListFragment::viewStory> Display story#" + ((Story)parent.getItemAtPosition(position)).getStoryId());
            Intent intent = new Intent(getActivity().getApplicationContext(), StoryViewFragment.class);
			intent.putExtra("story", (Story)parent.getItemAtPosition(position));;			
			startActivity(intent);
		}
	};
	
	private OnRefreshListener refreshEvent = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			if (uId != null) {
				new DownloadStoriesTask().execute(uId);
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
	private class DownloadStoriesTask extends AsyncTask<String, Integer, List<Story>>
    {
          protected void onPreExecute()
          {     //super.onPreExecute();
          		pg = ProgressDialog.show(getActivity(), "", "Loading stories...", true);
          } 

          protected List<Story> doInBackground(String ...params)
          {  
                return Communication.getUserStories(params[0]);
          } 

          protected void onPostExecute(List<Story> result)
          {     
                //super.onPostExecute(result);
                if(result == null)
                {
                	result = new ArrayList<Story>();
                	Story story = new Story();
                	story.setTitle("No story available");
	                result.add(story);
                }
                storyList.clear();
                storyList.addAll(result);
            	adapter.notifyDataSetChanged();
            	mPullToRefreshLayout.setRefreshComplete();
            	firstRun = false;
            	pg.dismiss();
            	Gen.appendLog("StoryListFragment::DownloadEventsTask::onPostExecute> Nb of stories downloaded : " + storyList.size());
          }
          
          @Override
  		protected void onCancelled() {
        	  mPullToRefreshLayout.setRefreshComplete();
        	  pg.dismiss();
  		}
    }
	
	
	/***************************************************************************************
	 *
	 *                                DeleteStoryTask Class
	 * 
	 ***************************************************************************************/
	private class DeleteStoryTask extends AsyncTask<List<Story>, Integer, Boolean>
	{
		private ProgressDialog pg;
		 
		protected void onPreExecute() {
			pg = ProgressDialog.show(getActivity(), "", "Deleting stories...", true);
		} 

		protected Boolean doInBackground(List<Story> ...params) {
			return Communication.deleteStories(params[0]);
		}

		protected void onPostExecute(Boolean result) {
			pg.dismiss();
			if (!result) {
				Gen.appendError("StoryEventFragment$DeleteStoryTask::onPostExecute> Error while deleting stories");
				Toast.makeText(getActivity(), "Stories could not be deleted. Please retry later", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(), "Stories deleted", Toast.LENGTH_SHORT).show();
				getActivity().finish();
			}
		}
     
		@Override
		protected void onCancelled() {
			pg.dismiss();
		}
	}
}

package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
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

import com.mjumel.mystories.adapters.StoryListAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;

public class StoryListFragment extends Fragment {
	
	private DrawerActivity activity;
	private String title = MyStoriesApp.APPLICATION_NAME;

	private ListView lv;
	private ProgressDialog pg;
	private StoryListAdapter adapter;
	private Menu mMenu;
	
	private PullToRefreshLayout mPullToRefreshLayout;
	
	private String uId = null;
	private List<Story> storyList = null;
	
    public StoryListFragment()
    {
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
    	activity = (DrawerActivity) getActivity();
    	
		storyList = activity.getStoryList();
		uId = activity.getUserId();
    	
		adapter = new StoryListAdapter(activity, this, storyList);
		
		if (getArguments() != null) {
			if (getArguments().containsKey("title"))
				title = getArguments().getString("title");
		}
		
		Gen.appendLog("StoryListFragment::onCreate> uId=" + uId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("StoryListFragment::onCreateView> Starting");
    	
    	View view = inflater.inflate(R.layout.fragment_my_stories_pull,container, false);
    	
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.my_stories_ptr_layout);
        ActionBarPullToRefresh.from(activity)
                .allChildrenArePullable()
                .listener(refreshEvent)
                .setup(mPullToRefreshLayout);
		
		lv = (ListView) view.findViewById(R.id.my_stories_listView_pull);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(viewStory);
		
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
    
    //@SuppressWarnings("unchecked")
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	Bundle bundle = new Bundle();
        switch (item.getItemId()) {
        case R.id.my_stories_add:
        	Gen.appendLog("StoryListFragment::onOptionsItemSelected> Display new story fragment");
        	bundle = new Bundle();
        	bundle.putBoolean("createStory", true);
        	activity.changeFragment(new EventListFragment(), bundle);
            return true;
        case R.id.my_stories_delete:
        	new DeleteStoryTask().execute();
        	return true;
        case R.id.my_stories_share:
        	Gen.appendLog("StoryListFragment::onOptionsItemSelected> Share story fragment");
        	bundle = new Bundle();
        	int pos = -1;
        	for(int i = 0; i < storyList.size(); i++)
        		if (storyList.get(i).isSelected())
        			pos = i;
        	if (pos >= 0) {
        		Gen.appendLog("StoryListFragment::onOptionsItemSelected> Sharing story #" + pos);
        		bundle.putInt("position", pos);
        		activity.changeFragment(new StoryShareFragment(), bundle);
        	} else {
        		Toast.makeText(activity, "You can't share more than one story at a time", Toast.LENGTH_SHORT).show();
        	}
        	return true;
        case R.id.my_stories_search:
        	Toast.makeText(activity, "Search action", Toast.LENGTH_SHORT).show();
        	return true;
        case R.id.my_stories_filters:
        	Toast.makeText(activity, "Show filters action", Toast.LENGTH_SHORT).show();
        	return true;
        case R.id.my_stories_switch:
        	Toast.makeText(activity, "Switch display action", Toast.LENGTH_SHORT).show();
        	return true;
        case R.id.my_stories_hide_title:
        	Toast.makeText(activity, "Hide title action", Toast.LENGTH_SHORT).show();
        	return true;
        case R.id.my_stories_hide_stars:
        	Toast.makeText(activity, "Hide stars action", Toast.LENGTH_SHORT).show();
        	return true;
        default:
        	Toast.makeText(activity, "TODO", Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	Gen.appendLog("StoryListFragment::onResume> Starting");
    	lv.setAdapter(adapter);
    	activity.setTitle(title);
    	
    	storyList = activity.getStoryList();
    	for (Story s : storyList)
    		s.setSelected(false);

    	adapter.resetSelected();
   		adapter.notifyDataSetChanged();
   		
    	Gen.appendLog("StoryListFragment::onResume> Ending");
    }
    
    public void updateMenu(int state)
    {
    	boolean delete = (state > 0?true:false);
   		mMenu.findItem(R.id.my_stories_delete).setVisible(delete);
   		mMenu.findItem(R.id.my_stories_share).setVisible(state==1);
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
            Intent intent = new Intent(activity.getApplicationContext(), StoryViewFragment.class);
            intent.putExtra("story", (Story)parent.getItemAtPosition(position));
			intent.putExtra("position", position);
			startActivityForResult(intent, 1);
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
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Gen.appendLog("StoryListFragment::onActivityResult> Starting");
    	Gen.appendLog("StoryListFragment::onActivityResult> requestCode : " + requestCode);
    	Gen.appendLog("StoryListFragment::onActivityResult> resultCode : " + resultCode);
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (resultCode == Activity.RESULT_OK) {
    		Story story = data.getParcelableExtra("story");
	    	String storyId = story.getStoryId();
	    	Story storyTmp = null;
	    	for (Story s : storyList) {
    			if (s.getStoryId().compareTo(storyId) == 0) {
    				Gen.appendLog("StoryListFragment::onActivityResult> Update story " + s.getTitle());
    				storyTmp = s;
    				break;
    			}
	    	}
    		if (storyTmp != null) {
    			int pos = storyList.indexOf(storyTmp);
    			storyList.remove(pos);
    			storyList.add(pos, story);
    		}
	    	activity.sendStoryList(storyList);
    	}
	}
	
    
    
	/***************************************************************************************
	 *
	 *                                DownloadStoriesTask Class
	 * 
	 ***************************************************************************************/
	private class DownloadStoriesTask extends AsyncTask<String, Integer, List<Story>>
    {
          protected void onPreExecute()
          {
          		pg = ProgressDialog.show(activity, "", "Loading stories...", true);
          } 

          protected List<Story> doInBackground(String ...params)
          {  
                return Communication.getUserStories(params[0]);
          } 

          protected void onPostExecute(List<Story> result)
          {     
                if(result == null)
                {
                	result = new ArrayList<Story>();
                	Story story = new Story();
                	story.setTitle("No story available");
	                result.add(story);
                }
                storyList.clear();
                storyList.addAll(result);
                activity.sendStoryList(storyList);
            	adapter.notifyDataSetChanged();
            	mPullToRefreshLayout.setRefreshComplete();
            	pg.dismiss();
            	Gen.appendLog("StoryListFragment::DownloadStoriesTask::onPostExecute> Nb of stories downloaded : " + storyList.size());
          }
          
          @Override
  		protected void onCancelled() {
        	  Toast.makeText(activity, "Operation cancelled", Toast.LENGTH_SHORT).show();
        	  mPullToRefreshLayout.setRefreshComplete();
        	  pg.dismiss();
  		}
    }
	
	
	/***************************************************************************************
	 *
	 *                                DeleteStoryTask Class
	 * 
	 ***************************************************************************************/
	private class DeleteStoryTask extends AsyncTask<Void, Integer, Boolean>
	{
		private ProgressDialog pg;
		 
		protected void onPreExecute() {
			pg = ProgressDialog.show(activity, "", "Deleting stories...", true);
		} 

		protected Boolean doInBackground(Void ...params) {
			return Communication.deleteStories(uId, storyList);
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				Gen.appendError("StoryEventFragment$DeleteStoryTask::onPostExecute> Error while deleting stories");
				Toast.makeText(activity, "Stories could not be deleted. Please retry later", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(activity, "Stories deleted", Toast.LENGTH_SHORT).show();
				List<Story> list = new ArrayList<Story>();
				for (Story s : storyList) {
					if (s.isSelected()) {
						list.add(s);
					}
				}
				for (Story e : list) {
					storyList.remove(e);
				}
				list = null;
				activity.sendStoryList(storyList);
				adapter.notifyDataSetChanged();
				updateMenu(0);
			}
			pg.dismiss();
		}
     
		@Override
		protected void onCancelled() {
			Toast.makeText(activity, "Operation cancelled", Toast.LENGTH_SHORT).show();
			pg.dismiss();
		}
	}
}

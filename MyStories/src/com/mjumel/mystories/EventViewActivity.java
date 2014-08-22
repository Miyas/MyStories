package com.mjumel.mystories;

import java.util.List;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.mjumel.mystories.adapters.EventPagerAdapter;
import com.mjumel.mystories.tools.Gen;

public class EventViewActivity extends FragmentActivity  {
	
	private EventPagerAdapter adapter;
    private ViewPager mViewPager;
    private final static String ACTIONBAR_TITLE = "myevents";
    
    private List<Story> storyList = null;
    private List<Event> eventList = null;
    private int position;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Gen.appendLog("EventViewActivity::onCreate> Starting");
        
        ActionBar actionBar = getActionBar();    
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        
        setTitle(ACTIONBAR_TITLE);
        
        if (getIntent().getExtras() != null) {
        	Bundle bundle = getIntent().getExtras(); 
			if (bundle.containsKey("eventList"))
				eventList = bundle.getParcelableArrayList("eventList");
			if (bundle.containsKey("storyList"))
				storyList = bundle.getParcelableArrayList("storyList");
			if (bundle.containsKey("position"))
				position = bundle.getInt("position");
		}
        
        setContentView(R.layout.fragment_view_story);
        adapter = new EventPagerAdapter(getSupportFragmentManager(), eventList);
        mViewPager = (ViewPager) findViewById(R.id.view_story_viewPager);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(position);
    }
	
	public void updatePager(List<Event> eventList) {
		this.eventList = eventList;
		adapter.setUpdateStatus(true);
		adapter.notifyDataSetChanged();
	}
	
	public List<Event> getEventList() {
		return eventList;
	}
	
	public List<Story> getStoryList() {
		return storyList;
	}
	
	public void sendEventList(List<Event> eventList) {
		this.eventList = eventList;
	}
	
	public void sendStoryList(List<Story> storyList) {
		this.storyList = storyList;
	}
	
	public void updateEventOnStories(Event event) {
		
	}
	
	public void removeEventFromStories(String eventId) {
		
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case android.R.id.home:
	            this.finish();
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
    public void setTitle(CharSequence title) {
        getActionBar().setTitle(Gen.bicolorSpan(ACTIONBAR_TITLE.toString(), 2));
    }
	
	@Override
    public void onDestroy() {
		Intent intent = new Intent();
		intent.putParcelableArrayListExtra("eventList", Gen.eventListToArrayList(eventList));
		setResult(RESULT_OK, intent);
		super.onDestroy();
	}
}

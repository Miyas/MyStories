package com.mjumel.mystories.adapters;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.mjumel.mystories.Event;
import com.mjumel.mystories.Story;
import com.mjumel.mystories.StoryEventFragment;

public class StoryPagerAdapter extends FragmentStatePagerAdapter {

	private Story story;
	private List<Event> events;
	private boolean updateStatus = false;
	
	public StoryPagerAdapter(FragmentManager fm, Story story) {
		super(fm);
		this.story = story;
		this.events = story.getEvents();
		if (this.events.size() <= 0)
			this.events.add(new Event());
	}

	@Override
	public Fragment getItem(int pos) {
		//Gen.appendLog("StoryPagerAdapter::getItem> Starting (pos=" + pos + ")");
		return StoryEventFragment.newInstance(story, events.get(pos), pos);
	}

	@Override
	public int getCount() {
		//Gen.appendLog("StoryPagerAdapter::getCount> Starting");
		return events.size();
	}
	
	@Override
    public int getItemPosition(Object object){
		if(updateStatus) { //this includes deleting or adding pages
			updateStatus = false;
			return PagerAdapter.POSITION_NONE;
		} else
			return PagerAdapter.POSITION_UNCHANGED; //this ensures high performance in other operations such as editing list items.
	}
	
	public void setUpdateStatus(boolean status) {
		updateStatus = true;
	}
}

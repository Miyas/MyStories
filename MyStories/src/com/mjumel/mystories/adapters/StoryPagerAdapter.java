package com.mjumel.mystories.adapters;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mjumel.mystories.Event;
import com.mjumel.mystories.StoryEventFragment;

public class StoryPagerAdapter extends FragmentPagerAdapter {

	List<Event> eventList;
	
	public StoryPagerAdapter(FragmentManager fm, List<Event> eventList) {
		super(fm);
		this.eventList = eventList;
	}

	@Override
	public Fragment getItem(int pos) {
		return StoryEventFragment.newInstance(eventList.get(pos));
	}

	@Override
	public int getCount() {
		return this.eventList.size();
	}

}

package com.mjumel.mystories.adapters;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mjumel.mystories.Event;
import com.mjumel.mystories.Story;
import com.mjumel.mystories.StoryEventFragment;
import com.mjumel.mystories.tools.Gen;

public class StoryPagerAdapter extends FragmentPagerAdapter {

	Story story;
	List<Event> events;
	
	public StoryPagerAdapter(FragmentManager fm, Story story) {
		super(fm);
		this.story = story;
		this.events = story.getEvents();
		if (this.events.size() <= 0)
			this.events.add(new Event());
	}

	@Override
	public Fragment getItem(int pos) {
		Gen.appendLog("StoryPagerAdapter::getItem> Starting (pos=" + pos + ")");
		return StoryEventFragment.newInstance(story, events.get(pos), pos);
	}

	@Override
	public int getCount() {
		Gen.appendLog("StoryPagerAdapter::getCount> Starting");
		return events.size();
	}

}

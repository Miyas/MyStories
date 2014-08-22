package com.mjumel.mystories.adapters;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.mjumel.mystories.Event;
import com.mjumel.mystories.EventViewFragment;

public class EventPagerAdapter extends FragmentStatePagerAdapter {

	private List<Event> eventList;
	private boolean updateStatus = false;
	
	public EventPagerAdapter(FragmentManager fm, List<Event> eventList) {
		super(fm);
		this.eventList = eventList;
	}

	@Override
	public Fragment getItem(int pos) {
		return EventViewFragment.newInstance(eventList, pos);
	}

	@Override
	public int getCount() {
		return eventList.size();
	}
	
	@Override
    public int getItemPosition(Object object){
		if (updateStatus) {
			updateStatus = false;
			return PagerAdapter.POSITION_NONE;
		} else
			return PagerAdapter.POSITION_UNCHANGED;
	}
	
	public void setUpdateStatus(boolean status) {
		updateStatus = true;
	}
}

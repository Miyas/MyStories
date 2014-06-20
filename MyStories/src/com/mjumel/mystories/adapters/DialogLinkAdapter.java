package com.mjumel.mystories.adapters;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.mjumel.mystories.EventListFragment;
import com.mjumel.mystories.R;
import com.mjumel.mystories.Story;

public class DialogLinkAdapter extends ArrayAdapter<Story> {
	private LayoutInflater inflater;
	private List<Story> storyList;
	private EventListFragment fragment;
	
	private int checkedCount = 0;
	
	static class ViewHolder {
		CheckBox cb;
	}

	public DialogLinkAdapter(Activity context, EventListFragment fragment, List<Story> stories) {
		super(context, R.layout.dialog_link_events_item, stories);
		this.inflater = LayoutInflater.from(context);
		this.fragment = fragment;
		this.storyList = stories;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder holder;
		
		//Gen.appendLog("EventListAdapter::getView> Starting event#"+position);
		
		if ( rowView == null ) {
			rowView = inflater.inflate(R.layout.dialog_link_events_item, null);
			holder = new ViewHolder();
            holder.cb = (CheckBox) rowView.findViewById(R.id.dialog_link_events_cb);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
		
		Story story = this.getItem(position);
		//Gen.appendLog("EventListAdapter::getView> event#" + position + " / eventid#" + event.getEventId());
				
		holder.cb.setTag(position);
		holder.cb.setText(story.getTitle());
		holder.cb.setChecked(story.isSelected());
		holder.cb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Story s = storyList.get((Integer)v.getTag()); 
            	s.setSelected(!s.isSelected());
            	fragment.setNbStoriesChecked(setCount(s));
            	notifyDataSetChanged();
            }
        });
		
		return rowView;
	}
	
	// Set selected count
    private int setCount(Story s) {
        if (s.isSelected()) {
            checkedCount++;
        } else {
            if (checkedCount != 0) {
                checkedCount--;
            }
        }
        return checkedCount;
    }
}

package com.mjumel.mystories.adapters;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mjumel.mystories.R;
import com.mjumel.mystories.Story;

public class StoryListAdapter extends ArrayAdapter<Story> {
	private LayoutInflater inflater;
	
	static class ViewHolder {
		TextView title;
		TextView desc;
		TextView viewers;
		RatingBar rating;
	}

	public StoryListAdapter(Activity context, List<Story> stories) {
		super(context, R.layout.fragment_my_stories_item, stories);
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder holder;
		
		//Gen.appendLog("StoryListAdapter::getView> Starting event#"+position);
		
		if ( rowView == null ) {
			rowView = inflater.inflate(R.layout.fragment_my_stories_item, null);
			holder = new ViewHolder();
            holder.title = (TextView) rowView.findViewById(R.id.my_stories_item_title);
            holder.desc = (TextView) rowView.findViewById(R.id.my_stories_item_desc);
            holder.viewers = (TextView) rowView.findViewById(R.id.my_stories_item_viewers);
            holder.rating = (RatingBar) rowView.findViewById(R.id.my_stories_item_ratingBar);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
		
		Story story = this.getItem(position);
		
		holder.title.setText(story.getTitle());
		holder.desc.setText(story.getEventsCount() + " events, created on xx/xx/xxxx");
		holder.viewers.setText(String.valueOf(Math.round(Math.random()*10)) + " views");
		
		/*if (story.getRating() < 0)
			holder.rating.setVisibility(RatingBar.INVISIBLE);
		else
		{*/
			holder.rating.setVisibility(RatingBar.VISIBLE);
			holder.rating.setProgress(story.getRating());
		//}

        //Gen.appendLog("EventListAdapter::getView> Ending event#"+position);
		return rowView;
	}
}

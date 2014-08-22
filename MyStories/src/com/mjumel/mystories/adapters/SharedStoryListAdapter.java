package com.mjumel.mystories.adapters;

import java.util.List;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mjumel.mystories.R;
import com.mjumel.mystories.Story;

public class SharedStoryListAdapter extends ArrayAdapter<Story> {
	private LayoutInflater inflater;
	private Activity context;
	private Resources res;

	static class ViewHolder {
		TextView title;
		TextView desc;
		TextView viewers;
		RatingBar rating;
		TextView ratingText;
	}

	public SharedStoryListAdapter(Activity context, List<Story> storyList) {
		super(context, R.layout.fragment_my_stories_item, storyList);
		this.inflater = LayoutInflater.from(context);
		this.context = context;
		this.res = this.context.getResources();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder holder;
		
		if ( rowView == null ) {
			rowView = inflater.inflate(R.layout.fragment_my_stories_item, null);
			holder = new ViewHolder();
            holder.title = (TextView) rowView.findViewById(R.id.my_stories_item_title);
            holder.desc = (TextView) rowView.findViewById(R.id.my_stories_item_desc);
            holder.viewers = (TextView) rowView.findViewById(R.id.my_stories_item_viewers);
            holder.rating = (RatingBar) rowView.findViewById(R.id.my_stories_item_ratingBar);
            holder.ratingText = (TextView) rowView.findViewById(R.id.my_stories_item_rating_text);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
		
		Story story = this.getItem(position);
		
		holder.title.setText(story.getTitle());
		
		String nbEvents = res.getQuantityString(R.plurals.story_number_of_events, story.getEventsCount(), story.getEventsCount());
		holder.desc.setText(nbEvents + "xx/xx/xxxx");
		
		holder.viewers.setText(String.valueOf(Math.round(Math.random()*10)) + " views");
		holder.ratingText.setTag(position);
		setRatingText(holder.ratingText, story, true);

		return rowView;
	}
	
	private void setRatingText(TextView tv, Story story, boolean firstDisplay) {
		boolean selected = story.isSelected();
		selected=firstDisplay?!selected:selected;
		
		if (selected) {
			if (story.getRating() < 0) {
				tv.setText(String.valueOf("No Review"));
				tv.setBackgroundColor(context.getResources().getColor(R.color.background_dark));
			}
			else {
				tv.setText(String.valueOf(story.getRating()));
				setRatingColor(tv, story);
			}
        } else {
        	tv.setText("");
        	tv.setBackgroundResource(R.drawable.ms_icon_accept);
        }
	}
	
	private void setRatingColor(TextView tv, Story story) {
		if (story.getRating() < 0)
			tv.setBackgroundColor(context.getResources().getColor(R.color.rating_0));
		else if (story.getRating() < 1)
			tv.setBackgroundColor(context.getResources().getColor(R.color.rating_0));
		else if (story.getRating() < 2)
			tv.setBackgroundColor(context.getResources().getColor(R.color.rating_1));
		else if (story.getRating() < 3)
			tv.setBackgroundColor(context.getResources().getColor(R.color.rating_2));
		else if (story.getRating() < 4)
			tv.setBackgroundColor(context.getResources().getColor(R.color.rating_3));
		else if (story.getRating() < 5)
			tv.setBackgroundColor(context.getResources().getColor(R.color.rating_4));
		else
			tv.setBackgroundColor(context.getResources().getColor(R.color.rating_5));
	}
}

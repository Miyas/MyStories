package com.mjumel.mystories.adapters;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mjumel.mystories.Event;
import com.mjumel.mystories.R;
import com.mjumel.mystories.tools.Gen;
import com.nostra13.universalimageloader.core.ImageLoader;

public class NewStoryListAdapter extends ArrayAdapter<Event> {
	private LayoutInflater inflater;
	private Activity context;
	
	static class ViewHolder {
		TextView comment;
		RatingBar rating;
		ImageView image;
	}

	public NewStoryListAdapter(Activity context, List<Event> events) {
		super(context, R.layout.fragment_new_story_item, events);
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder holder;
		
		//Gen.appendLog("EventListAdapter::getView> Starting event#"+position);
		
		if ( rowView == null ) {
			rowView = inflater.inflate(R.layout.fragment_new_story_item, null);
			holder = new ViewHolder();
            holder.comment = (TextView) rowView.findViewById(R.id.my_stories_item_title);
            holder.rating = (RatingBar) rowView.findViewById(R.id.my_stories_item_ratingBar);
            holder.image = (ImageView) rowView.findViewById(R.id.event_item_imageView);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
		
		Event event = this.getItem(position);
		
		if (event.isSelected())
			rowView.setBackgroundColor(context.getResources().getColor(R.color.background_dark));
		else if (position%2 == 0)
			rowView.setBackgroundColor(context.getResources().getColor(R.color.item_background_2));
		else
			rowView.setBackgroundColor(context.getResources().getColor(R.color.item_background_1));
		
		holder.comment.setText(event.getComment());
		holder.comment.setTextColor(Color.BLACK);
		
		if (event.getRating() < 0)
			holder.rating.setVisibility(RatingBar.GONE);
		else
		{
			if (event.getRating() == 0)
				holder.rating.setNumStars(1);
			else
				holder.rating.setNumStars(event.getRating());
			holder.rating.setVisibility(RatingBar.VISIBLE);
			holder.rating.setProgress(event.getRating());
		}
		
		if (event.getThumbMediaPath() != null)
		{
			Gen.appendLog("EventListAdapter::getView> Image loading for event#" + position + " (" + event.getThumbMediaPath() + ")");
			holder.image.setVisibility(ImageView.VISIBLE);
			
			ImageLoader.getInstance().displayImage(event.getThumbMediaPath(), holder.image);
		}
		else
		{
			holder.image.setImageBitmap(null);
			holder.image.setVisibility(ImageView.GONE);
		}

        //Gen.appendLog("EventListAdapter::getView> Ending event#"+position);
		return rowView;
	}
}

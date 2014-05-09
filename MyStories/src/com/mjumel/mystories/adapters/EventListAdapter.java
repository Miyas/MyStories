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

public class EventListAdapter extends ArrayAdapter<Event> {
	private LayoutInflater inflater;
	private Activity context;
	
	static class ViewHolder {
		TextView comment;
		RatingBar rating;
		ImageView image;
	}

	public EventListAdapter(Activity context, List<Event> events) {
		super(context, R.layout.fragment_my_events_item, events);
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder holder;
		
		//Gen.appendLog("EventListAdapter::getView> Starting event#"+position);
		
		if ( rowView == null ) {
			rowView = inflater.inflate(R.layout.fragment_my_events_item, null);
			holder = new ViewHolder();
            holder.comment = (TextView) rowView.findViewById(R.id.my_stories_item_title);
            holder.rating = (RatingBar) rowView.findViewById(R.id.my_stories_item_ratingBar);
            holder.image = (ImageView) rowView.findViewById(R.id.event_item_imageView);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
		
		if (position%2 == 0)
			rowView.setBackgroundColor(context.getResources().getColor(R.color.item_background_1));
		else
			rowView.setBackgroundColor(context.getResources().getColor(R.color.item_background_2));
		
		Event event = this.getItem(position);
		//Gen.appendLog("EventListAdapter::getView> event#" + position + " / eventid#" + event.getEventId());
		
		holder.comment.setText(event.getComment());
		holder.comment.setTextColor(Color.BLACK);
		
		if (event.getRating() < 0)
			holder.rating.setVisibility(RatingBar.INVISIBLE);
		else {
			holder.rating.setVisibility(RatingBar.VISIBLE);
			holder.rating.setProgress(event.getRating());
		}
		
		if (event.getThumbMediaPath() != null) {
			Gen.appendLog("EventListAdapter::getView> Image loading for event#" + position + " (" + event.getThumbMediaPath() + ")");
			holder.image.setVisibility(ImageView.VISIBLE);
			ImageLoader.getInstance().displayImage(event.getThumbMediaPath(), holder.image);
		} else {
			holder.image.setImageBitmap(null);
			holder.image.setVisibility(ImageView.INVISIBLE);
		}
			
		// Change layout display in function of the position of the view in the list
		/*LayoutParams lpRow = new LayoutParams(LayoutParams.MATCH_PARENT, 80);
		LayoutParams lpImage = new LayoutParams(80,LayoutParams.MATCH_PARENT);
		LayoutParams lpText = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		LayoutParams lpRating = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		if (position%2 == 0) {
			lpImage.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
			lpText.addRule(RelativeLayout.ALIGN_PARENT_TOP, -1);
			lpText.addRule(RelativeLayout.LEFT_OF, R.id.event_item_imageView);
			lpRating.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
			lpRating.addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1);
		} else {
			lpImage.addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1);
			lpText.addRule(RelativeLayout.ALIGN_PARENT_TOP, -1);
			lpText.addRule(RelativeLayout.RIGHT_OF, R.id.event_item_imageView);
			lpRating.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
			lpRating.addRule(RelativeLayout.RIGHT_OF, R.id.event_item_imageView);
		}
		//rowView.setLayoutParams(lpRow);
		holder.image.setLayoutParams(lpImage);
		holder.comment.setLayoutParams(lpText);
		holder.rating.setLayoutParams(lpRating);*/
		
        //Gen.appendLog("EventListAdapter::getView> Ending event#"+position);
		return rowView;
	}
}

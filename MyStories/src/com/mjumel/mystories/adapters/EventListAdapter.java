package com.mjumel.mystories.adapters;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.net.Uri;
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
import com.mjumel.mystories.tools.ImageLoader;
import com.mjumel.mystories.tools.UserPicture;

public class EventListAdapter extends ArrayAdapter<Event> {
	private LayoutInflater inflater;
	private Activity context;
	private ImageLoader imageLoader; 
	
	static class ViewHolder {
		TextView comment;
		RatingBar rating;
		ImageView image;
	}

	public EventListAdapter(Activity context, List<Event> events) {
		super(context, R.layout.fragment_my_events_item, events);
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		
		// Create ImageLoader object to download and show image in list
        // Call ImageLoader constructor to initialize FileCache
        imageLoader = new ImageLoader(this.context.getApplicationContext());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder holder;
		
		Gen.appendLog("EventListAdapter::getView> Starting event#"+position);
		
		if ( rowView == null ) {
			rowView = inflater.inflate(R.layout.fragment_my_events_item, null);
			holder = new ViewHolder();
            holder.comment = (TextView) rowView.findViewById(R.id.event_item_textView);
            holder.rating = (RatingBar) rowView.findViewById(R.id.event_item_ratingBar);
            holder.image = (ImageView) rowView.findViewById(R.id.event_item_imageView);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
		
		Event event = this.getItem(position);
		Gen.appendLog("EventListAdapter::getView> event#" + position + " / eventid#" + event.getEventId());
		
		holder.comment.setText(event.getComment());
		
		if (event.getRating() < 0)
			holder.rating.setVisibility(RatingBar.INVISIBLE);
		else
		{
			holder.rating.setVisibility(RatingBar.VISIBLE);
			holder.rating.setProgress(event.getRating());
		}
		
		holder.image.setImageBitmap(null);
		if (event.getThumbMediaPath() != null)
		{
			Gen.appendLog("EventListAdapter::getView> Image loading for event#" + position + " (" + event.getThumbMediaPath() + ")");
			imageLoader.DisplayImage(event.getThumbMediaPath(), holder.image);
			holder.image.setVisibility(ImageView.VISIBLE);
	    	/*UserPicture userPicture = new UserPicture(Uri.parse(event.getThumbMediaPath()), context.getContentResolver());
	        try {
	        	holder.image.setImageBitmap(userPicture.getBitmap());
			} catch (IOException e) {
				e.printStackTrace();
				Gen.appendLog("EventListAdapter::getView> IOException Error ("+event.getThumbMediaPath()+")");
			}
	        userPicture = null;*/
		}
		else
			holder.image.setVisibility(ImageView.INVISIBLE);

        Gen.appendLog("EventListAdapter::getView> Ending event#"+position);
		return rowView;
	}
}

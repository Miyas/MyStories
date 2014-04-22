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
import com.mjumel.mystories.tools.UserPicture;

public class EventListAdapter extends ArrayAdapter<Event> {
	private LayoutInflater inflater;
	private Activity context;
	
	static class ViewHolder {
		TextView comment;
		RatingBar rating;
		ImageView image;
	}

	public EventListAdapter(Activity context, List<Event> nodes) {
		super(context, R.layout.event_list_item, nodes);
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder holder = new ViewHolder();
		
		Gen.appendLog("EventListAdapter::getView> Starting");
		
		if ( rowView == null ) {
			rowView = inflater.inflate(R.layout.event_list_item, null);
            holder.comment = (TextView) rowView.findViewById(R.id.event_item_textView);
            holder.rating = (RatingBar) rowView.findViewById(R.id.event_item_ratingBar);
            holder.image = (ImageView) rowView.findViewById(R.id.event_item_imageView);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
		
		rowView.setTag(holder);
		
		Event event = this.getItem(position);
		holder.comment.setText(event.GetComment());
		
		if (event.GetRating() < 0)
			holder.rating.setVisibility(RatingBar.INVISIBLE);
		else
			holder.rating.setProgress(event.GetRating());
		
		if (event.GetThumbMediaPath() != null)
		{
	    	UserPicture userPicture = new UserPicture(Uri.parse(event.GetThumbMediaPath()), context.getContentResolver());
	        try {
	        	holder.image.setImageBitmap(userPicture.getBitmap());
			} catch (IOException e) {
				e.printStackTrace();
				Gen.appendLog("EventListAdapter::getView> IOException Error");
			}
	        userPicture = null;
		}

        Gen.appendLog("EventListAdapter::getView> Ending");
		return rowView;
	}
}

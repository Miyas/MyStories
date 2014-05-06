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
import com.mjumel.mystories.tools.ImageLoader;

public class NewStoryListAdapter extends ArrayAdapter<Event> {
	private LayoutInflater inflater;
	private Activity context;
	private ImageLoader imageLoader; 
	
	static class ViewHolder {
		TextView comment;
		RatingBar rating;
		ImageView image;
	}

	public NewStoryListAdapter(Activity context, List<Event> events) {
		super(context, R.layout.fragment_new_story_item, events);
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
		
		//Gen.appendLog("EventListAdapter::getView> Starting event#"+position);
		
		if ( rowView == null ) {
			rowView = inflater.inflate(R.layout.fragment_new_story_item, null);
			holder = new ViewHolder();
            holder.comment = (TextView) rowView.findViewById(R.id.event_item_textView);
            holder.rating = (RatingBar) rowView.findViewById(R.id.event_item_ratingBar);
            holder.image = (ImageView) rowView.findViewById(R.id.event_item_imageView);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
		
		Event event = this.getItem(position);
		//Gen.appendLog("EventListAdapter::getView> event#" + position + " / eventid#" + event.getEventId());
		
		/*if (position%2 == 0)
			if (rowView.isSelected())
				rowView.setBackgroundColor(context.getResources().getColor(R.color.background_dark));
			else
				rowView.setBackgroundColor(context.getResources().getColor(R.color.background_light));
		else
			if (rowView.isSelected())
				rowView.setBackgroundColor(Color.BLACK);
			else
				rowView.setBackgroundColor(Color.WHITE);*/
		
		if (event.isSelected())
			rowView.setBackgroundColor(context.getResources().getColor(R.color.background_dark));
		else
			rowView.setBackgroundColor(context.getResources().getColor(R.color.background_light));
		
		holder.comment.setText(event.getComment());
		holder.comment.setTextColor(Color.BLACK);
		
		if (event.getRating() < 0)
			holder.rating.setVisibility(RatingBar.INVISIBLE);
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
			imageLoader.DisplayImage(event.getThumbMediaPath(), holder.image);
			
			// Change layout display in function of the position of the view in the list
			/*LayoutParams lpImage = new LayoutParams(80,80);
			LayoutParams lpText = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			if (position%2 == 0) {
				lpImage.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
				lpText.addRule(RelativeLayout.LEFT_OF, R.id.event_item_imageView);
			} else {
				lpImage.addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1);
				lpText.addRule(RelativeLayout.RIGHT_OF, R.id.event_item_imageView);
			}
			holder.image.setLayoutParams(lpImage);
			holder.comment.setLayoutParams(lpText);
			//holder.rating.setLayoutParams(lpText);
			 */
			
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
		{
			holder.image.setImageBitmap(null);
			holder.image.setVisibility(ImageView.INVISIBLE);
		}

        //Gen.appendLog("EventListAdapter::getView> Ending event#"+position);
		return rowView;
	}
}
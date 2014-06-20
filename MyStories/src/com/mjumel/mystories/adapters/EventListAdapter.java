package com.mjumel.mystories.adapters;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mjumel.mystories.Event;
import com.mjumel.mystories.EventListFragment;
import com.mjumel.mystories.R;
import com.mjumel.mystories.StoryNewFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

public class EventListAdapter extends ArrayAdapter<Event> {
	private LayoutInflater inflater;
	private List<Event> events;
	private EventListFragment fragmentEvent;
	private StoryNewFragment fragmentStory;
	
	private Animation animation1;
	private Animation animation2;
	private ImageView tvFlip;
	private int checkedCount = 0;
	
	static class ViewHolder {
		TextView comment;
		RatingBar rating;
		ImageView image;
	}

	public EventListAdapter(Activity context, EventListFragment fragment, List<Event> events) {
		super(context, R.layout.fragment_my_events_item, events);
		this.inflater = LayoutInflater.from(context);
		this.events = events;
		this.fragmentEvent = fragment;
		
		animation1 = AnimationUtils.loadAnimation(context, R.anim.to_middle);
        animation2 = AnimationUtils.loadAnimation(context, R.anim.from_middle);
	}
	
	public EventListAdapter(Activity context, StoryNewFragment fragment, List<Event> events) {
		super(context, R.layout.fragment_my_events_item, events);
		this.inflater = LayoutInflater.from(context);
		this.events = events;
		this.fragmentStory = fragment;
		
		animation1 = AnimationUtils.loadAnimation(context, R.anim.to_middle);
        animation2 = AnimationUtils.loadAnimation(context, R.anim.from_middle);
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
		
		holder.image.setTag(position);
		holder.image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	tvFlip = (ImageView) v;
            	tvFlip.clearAnimation();
            	tvFlip.setAnimation(animation1);
            	tvFlip.startAnimation(animation1);
            	setAnimListeners(events.get((Integer)v.getTag()));
            }
         });
		
		setImage(holder.image, event, true);
			
		return rowView;
	}
	
	private void setImage(ImageView tv, Event event, boolean firstDisplay) {
		boolean selected = event.isSelected();
		selected=firstDisplay?!selected:selected;
		
		if (selected) {
			if (event.getThumbMediaPath() != null) {
				ImageLoader.getInstance().displayImage(event.getThumbMediaPath(), tv);
			} else {
				tv.setImageBitmap(null);
				tv.setImageResource(R.drawable.ms_event_text);
				//holder.image.setBackgroundColor(context.getResources().getColor(R.color.purple));
			}
        } else {
        	tv.setImageResource(R.drawable.ms_icon_accept);
        }
	}
	
	private void setAnimListeners(final Event event) {
        AnimationListener animListener;
        animListener = new AnimationListener() {
 
            @Override
            public void onAnimationStart(Animation animation) {
                if (animation == animation1) {
                	setImage(tvFlip, event, false);
                    tvFlip.clearAnimation();
                    tvFlip.setAnimation(animation2);
                    tvFlip.startAnimation(animation2);
                } else {
                	event.setSelected(!event.isSelected());
                    setCount();
                    setActionBar();
                }
            }
 
            // Set selected count
            private void setCount() {
                if (event.isSelected()) {
                    checkedCount++;
                } else {
                    if (checkedCount != 0) {
                        checkedCount--;
                    }
                }
            }
 
            // Show/Hide action buttons
            private void setActionBar() {
            	if (checkedCount > 0) {
            		if (fragmentEvent != null)
            			fragmentEvent.updateMenu(true);
            		else
            			fragmentStory.updateMenu(true);
            	} else {
            		if (fragmentEvent != null)
            			fragmentEvent.updateMenu(false);
            		else
            			fragmentStory.updateMenu(false);
            	}
                notifyDataSetChanged();
            }
 
            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub
            }
 
            @Override
            public void onAnimationEnd(Animation arg0) {
                // TODO Auto-generated method stub
            }
        };
 
        animation1.setAnimationListener(animListener);
        animation2.setAnimationListener(animListener);
    }
}

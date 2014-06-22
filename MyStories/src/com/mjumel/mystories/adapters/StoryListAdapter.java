package com.mjumel.mystories.adapters;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mjumel.mystories.R;
import com.mjumel.mystories.Story;
import com.mjumel.mystories.StoryListFragment;

public class StoryListAdapter extends ArrayAdapter<Story> {
	private LayoutInflater inflater;
	private Activity context;
	private StoryListFragment fragment;
	private List<Story> stories;
	
	Animation animation1;
    Animation animation2;
    TextView tvFlip;
    int checkedCount = 0;
    ActionBar mMode;
    boolean isActionModeShowing;
	
	static class ViewHolder {
		TextView title;
		TextView desc;
		TextView viewers;
		RatingBar rating;
		TextView ratingText;
	}

	public StoryListAdapter(Activity context, StoryListFragment fragment, List<Story> stories) {
		super(context, R.layout.fragment_my_stories_item, stories);
		this.inflater = LayoutInflater.from(context);
		this.context = context;
		this.stories = stories;
		this.fragment = fragment;
		
		animation1 = AnimationUtils.loadAnimation(context, R.anim.to_middle);
        animation2 = AnimationUtils.loadAnimation(context, R.anim.from_middle);
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
		//holder.title.setBackgroundColor(context.getResources().getColor(R.color.abc_search_url_text_pressed));
		holder.desc.setText(story.getEventsCount() + " events, created on xx/xx/xxxx");
		//holder.desc.setBackgroundColor(context.getResources().getColor(R.color.green));
		holder.viewers.setText(String.valueOf(Math.round(Math.random()*10)) + " views");
		//holder.viewers.setBackgroundColor(context.getResources().getColor(R.color.purple));
		
		holder.ratingText.setTag(position);
		holder.ratingText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	tvFlip = (TextView) v;
            	tvFlip.clearAnimation();
            	tvFlip.setAnimation(animation1);
            	tvFlip.startAnimation(animation1);
            	setAnimListeners(stories.get((Integer)v.getTag()));
            }
         });
		
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
	
	private void setAnimListeners(final Story story) {
        AnimationListener animListener;
        animListener = new AnimationListener() {
 
            @Override
            public void onAnimationStart(Animation animation) {
                if (animation == animation1) {
                	setRatingText(tvFlip, story, false);
                    tvFlip.clearAnimation();
                    tvFlip.setAnimation(animation2);
                    tvFlip.startAnimation(animation2);
                } else {
                	story.setSelected(!story.isSelected());
                    setCount();
                    setActionBar();
                }
            }
 
            // Set selected count
            private void setCount() {
                if (story.isSelected()) {
                    checkedCount++;
                } else {
                    if (checkedCount != 0) {
                        checkedCount--;
                    }
                }
            }
 
            // Show/Hide action buttons
            private void setActionBar() {
            	fragment.updateMenu(checkedCount);
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

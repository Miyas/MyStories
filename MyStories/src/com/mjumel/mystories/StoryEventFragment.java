package com.mjumel.mystories;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mjumel.mystories.tools.Gen;
import com.nostra13.universalimageloader.core.ImageLoader;

public class StoryEventFragment extends Fragment {
	
	private Event event = null;
	
	private TextView comment;
	private ImageView image;
	private RatingBar rating;
	

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("StoryEventFragment::onCreateView> Starting");
    	event = (Event)getArguments().getParcelable("event");
		
		View view = inflater.inflate(R.layout.fragment_view_story_event,container, false);
		comment = (TextView) view.findViewById(R.id.story_event_comment);
		image = (ImageView) view.findViewById(R.id.story_event_image);
		rating = (RatingBar) view.findViewById(R.id.story_event_rating);
		
		comment.setText(event.getComment());
		rating.setProgress(event.getRating());
		if (event.getResizedMediaPath() != null) { 
			ImageLoader.getInstance().displayImage(event.getResizedMediaPath(), image);
		}
		
		Gen.appendLog("StoryEventFragment::onCreateView> Ending");
		return view;
    }
    
    public static StoryEventFragment newInstance(Event event) {
    	StoryEventFragment fragment = new StoryEventFragment();
    	Bundle args = new Bundle();
    	args.putParcelable("event", event);
    	fragment.setArguments(args);
    	return fragment;
    }
}

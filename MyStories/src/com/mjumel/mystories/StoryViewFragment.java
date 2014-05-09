package com.mjumel.mystories;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.mjumel.mystories.adapters.StoryPagerAdapter;

public class StoryViewFragment extends FragmentActivity  {
	private StoryPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    
    private Story story;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        story = (Story)getIntent().getExtras().getParcelable("story");
        
        setContentView(R.layout.fragment_view_story);
        // Set up the adapter.
        mSectionsPagerAdapter = new StoryPagerAdapter(getSupportFragmentManager(), story.getEvents());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.view_story_viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }
}

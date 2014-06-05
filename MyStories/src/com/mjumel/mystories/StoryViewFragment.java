package com.mjumel.mystories;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.mjumel.mystories.adapters.StoryPagerAdapter;
import com.mjumel.mystories.tools.Gen;

public class StoryViewFragment extends FragmentActivity  {
	private StoryPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    
    private Story story;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Gen.appendLog("StoryViewFragment::onCreate> Starting");
        story = (Story)getIntent().getExtras().getParcelable("story");
        
        setContentView(R.layout.fragment_view_story);
        // Set up the adapter.
        mSectionsPagerAdapter = new StoryPagerAdapter(getSupportFragmentManager(), story);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.view_story_viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        
        //final ActionBar bar = getActionBar();
        //bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
    }
}

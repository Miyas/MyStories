package com.mjumel.mystories;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.mjumel.mystories.adapters.StoryPagerAdapter;
import com.mjumel.mystories.tools.Gen;

public class StoryViewFragment extends FragmentActivity  {
	
	private StoryPagerAdapter adapter;
    private ViewPager mViewPager;
    
    private Story story;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Gen.appendLog("StoryViewFragment::onCreate> Starting");
        story = (Story)getIntent().getExtras().getParcelable("story");
        
        setContentView(R.layout.fragment_view_story);
        // Set up the adapter.
        adapter = new StoryPagerAdapter(getSupportFragmentManager(), story);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.view_story_viewPager);
        mViewPager.setAdapter(adapter);
        
        //final ActionBar bar = getActionBar();
        //bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
    }
	
	public void updatePager(Story story) {
		this.story = story;
		adapter.setUpdateStatus(true);
		adapter.notifyDataSetChanged();
	}
	
	@Override
    public void onDestroy() {
		Intent intent = new Intent();
		intent.putExtra("story", story);
		setResult(RESULT_OK, intent);
		super.onDestroy();
	}
}

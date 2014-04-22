package com.mjumel.mystories;

import java.util.Locale;

import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;
import com.mjumel.mystories.tools.ImageWorker;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends ActionBarActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		Gen.writeLog("Home::onCreate> Running application");
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed()
	{
		if (getIntent().getBooleanExtra("EXIT", false)) {
			 moveTaskToBack(true); // exist app
        }
	}


	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			//if (position == 0 && getMediaUri() != null)
				return PlaceholderFragment.newInstance(position + 1, getIntent().getExtras());
			//else
				//return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section_events).toUpperCase(l);
			case 1:
				return getString(R.string.title_section_stories).toUpperCase(l);
			case 2:
				return getString(R.string.title_section_friends).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		private static final String ARG_MEDIA_URI = "media_uri";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			Gen.writeLog("Home::PlaceholderFragment> Starting newInstance1");
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			Gen.writeLog("Home::PlaceholderFragment> Ending newInstance1");
			return fragment;
		}
		
		public static PlaceholderFragment newInstance(int sectionNumber, Uri mediaUri) {
			Gen.writeLog("Home::PlaceholderFragment> Starting newInstance2");
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			args.putParcelable(ARG_MEDIA_URI, mediaUri);
			fragment.setArguments(args);
			Gen.writeLog("Home::PlaceholderFragment> Ending newInstance2");
			return fragment;
		}
		
		public static PlaceholderFragment newInstance(int sectionNumber, Bundle extras) {
			Gen.writeLog("Home::PlaceholderFragment> Starting newInstance3");
			PlaceholderFragment fragment = new PlaceholderFragment();
			//Bundle args = new Bundle();
			extras.putInt(ARG_SECTION_NUMBER, sectionNumber);
			//args.putParcelable(ARG_MEDIA_URI, mediaUri);
			fragment.setArguments(extras);
			Gen.writeLog("Home::PlaceholderFragment> Ending newInstance3");
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			Gen.writeLog("Home::onCreateView> Starting");
			View rootView = inflater.inflate(R.layout.fragment_home, container, false);
			final TextView title = (TextView) rootView.findViewById(R.id.section_label);
			final EditText comment = (EditText) rootView.findViewById(R.id.comment);
			final Button button     = (Button) rootView.findViewById(R.id.button1);
			final RatingBar rating  = (RatingBar) rootView.findViewById(R.id.rating);
			
			title.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
			
			Gen.writeLog("Home::onCreateView> Preparing ImageView (step 1)");
			Uri mediaUriTmp = (Uri) getArguments().get("mediaUri");
			
			final String uid = getArguments().getString("uid");
			final Uri mediaUri = mediaUriTmp==null?null:Uri.parse(ImageWorker.getRealPathFromURI(getActivity(), mediaUriTmp));
			
			Gen.writeLog("Home::onCreateView> mediaUri = " + mediaUri);
			Gen.writeLog("Home::onCreateView> uid = " + uid);
			
			if (uid != null)
				title.setText("Welcome " + uid + "!");
			
			if (mediaUri != null)
			{
				ImageView imageView = (ImageView)rootView.findViewById(R.id.media);
				imageView.setImageBitmap(
					    ImageWorker.decodeSampledBitmapFromFile(
					    		mediaUri.getPath(), 
					    		imageView.getWidth(), 
					    		imageView.getHeight()
					    )
				);
			}
			
			button.setOnClickListener(new OnClickListener() {           
	            @Override
	            public void onClick(View v) {
	                //ProgressDialog.show(getActivity(), "", "Posting event...", true);
	                Toast.makeText(getActivity(), "Posting event.....", Toast.LENGTH_SHORT).show();
	                new Thread(new Runnable() {
	                        public void run() {
	                             getActivity().runOnUiThread(new Runnable() {
	                                    public void run() {
	                                    	//ProgressDialog.show(getActivity(), "", "Posting started.....", true);
	                                    	Toast.makeText(getActivity(), "Posting started.....", Toast.LENGTH_SHORT).show();
	                                    }
	                                });
	                             Gen.writeLog("Home::onCreateView::onClick> title = " + title.getText().toString());
	                             Gen.writeLog("Home::onCreateView::onClick> comment = " + comment.getText().toString());
	                             Gen.writeLog("Home::onCreateView::onClick> rating = " + rating.getProgress());
	                             Gen.writeLog("Home::onCreateView::onClick> mediaUri = " + (mediaUri == null?null:mediaUri.getPath()));
	                             Communication.postEvent(title.getText().toString(), comment.getText().toString(), rating.getProgress(), (mediaUri == null?null:mediaUri.getPath()), -1);
	                             Toast.makeText(getActivity(), "Posting done.....", Toast.LENGTH_SHORT).show();
	                        }
	                      }).start();
	                //ProgressDialog.show(getActivity(), "", "Posting done...", true);
	                //Toast.makeText(getActivity(), "Posting done.....", Toast.LENGTH_SHORT).show();
	            }
	         });
			
			Gen.writeLog("Home::onCreateView> Ending");
			return rootView;
		}
	}
}

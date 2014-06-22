package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mjumel.mystories.adapters.StoryListAdapter;
import com.mjumel.mystories.adapters.StoryShareAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;
import com.nostra13.universalimageloader.core.ImageLoader;

public class StoryShareFragment extends Fragment implements LoaderCallbacks<Cursor> {
	
	private DrawerActivity activity;

	private View view;
	private TextView tvTitle;
	private ImageView imgView1;
	private ImageView imgView2;
	private ImageView imgView3;
	private EditText etMsg;
	private ListView lv;
	private ProgressDialog pg;
	
	private Menu mMenu;
	
	private String uId = null;
	private Story story = null;
	private List<Story> storyList = null;
	private int position = -1;
	
	@SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    Contacts.DISPLAY_NAME_PRIMARY :
                    Contacts.DISPLAY_NAME
    };
	@SuppressLint("InlinedApi")
	private static final String[] PROJECTION =
    {
        Contacts._ID,
        Contacts.LOOKUP_KEY,
        Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.HONEYCOMB ?
                Contacts.DISPLAY_NAME_PRIMARY :
                Contacts.DISPLAY_NAME

    };
	private final static int[] TO_IDS = {
        R.id.share_story_item_name
	};
	private static final int CONTACT_ID_INDEX = 0;
	private static final int LOOKUP_KEY_INDEX = 1;
	 // Defines the text expression
    @SuppressLint("InlinedApi")
    private static final String SELECTION =
            Contacts.HAS_PHONE_NUMBER + " = ?";
    private static final String SORT_ORDER = Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.HONEYCOMB ?
            Contacts.DISPLAY_NAME_PRIMARY :
            Contacts.DISPLAY_NAME;
    // Defines a variable for the search string
    private String mSearchString = "";
    // Defines the array to hold values that replace the ?
    private String[] mSelectionArgs = { mSearchString };
    
	//private SimpleCursorAdapter mCursorAdapter;
	private StoryShareAdapter mCursorAdapter;
	
	
    public StoryShareFragment()
    {
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
    	activity = (DrawerActivity) getActivity();
		storyList = activity.getStoryList();
		uId = activity.getUserId();
    	
    	position = getArguments().getInt("position");
    	story = storyList.get(position);
    	story.setSelected(false);
    	activity.sendStoryList(storyList);
    	
		//adapter = new StoryListAdapter(getActivity(), this, storyList);
    	mCursorAdapter = new StoryShareAdapter(
    			activity,
    			this,
    			getContacts(),
                FROM_COLUMNS, 
                TO_IDS,
                0);
    	
    	//getLoaderManager().initLoader(0, null, (LoaderCallbacks<Cursor>) this);

		
		Gen.appendLog("StoryShareFragment::onCreate> uId=" + uId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("StoryShareFragment::onCreateView> Starting");
    	
    	view = inflater.inflate(R.layout.fragment_share_story,container, false);
    	tvTitle = (TextView) view.findViewById(R.id.share_story_title);
    	imgView1 = (ImageView) view.findViewById(R.id.share_story_iv1);
    	imgView2 = (ImageView) view.findViewById(R.id.share_story_iv2);
    	imgView3 = (ImageView) view.findViewById(R.id.share_story_iv3);
    	etMsg = (EditText) view.findViewById(R.id.share_story_text);
		lv = (ListView) view.findViewById(R.id.share_story_contacts);
		
		tvTitle.setText(story.getTitle());
		etMsg.setText("Dear friend, check it out!");
		lv.setAdapter(mCursorAdapter);
		//lv.setOnItemClickListener(viewStory);
		
		int cpt = 1;
		for (Event e : story.getEvents()) {
			if (e.getThumbMediaPath() != null) {
				switch (cpt) {
					case 1:
						ImageLoader.getInstance().displayImage(e.getThumbMediaPath(), imgView1);
						break;
					case 2:
						ImageLoader.getInstance().displayImage(e.getThumbMediaPath(), imgView2);
						break;
					case 3:
						ImageLoader.getInstance().displayImage(e.getThumbMediaPath(), imgView3);
						break;
				}
				cpt++;
			}
		}
		
		if (cpt >= 1)
			imgView1.setVisibility(ImageView.GONE);
		if (cpt >= 2)
			imgView2.setVisibility(ImageView.GONE);
		if (cpt >= 3)
			imgView3.setVisibility(ImageView.GONE);
		
		Gen.appendLog("StoryShareFragment::onCreateView> Ending");
		return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_share_story, menu);
        this.mMenu = menu;
    }
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
	        case R.id.share_story_share:
	        	new ShareStoryTask().execute();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	Gen.appendLog("StoryShareFragment::onResume> Starting");
    	lv.setAdapter(mCursorAdapter);
    	getActivity().setTitle("Share Story");
    	
    	storyList = activity.getStoryList();
   		mCursorAdapter.notifyDataSetChanged();
   		
    	Gen.appendLog("StoryShareFragment::onResume> Ending");
    }
    
    public void updateMenu(boolean delete)
    {
   		mMenu.findItem(R.id.share_story_share).setVisible(delete);
    }
    
    
    /***************************************************************************************
	 *
	 *                                Event-based functions
	 * 
	 ***************************************************************************************/
    private OnItemClickListener viewStory = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Gen.appendLog("StoryShareFragment::viewStory> Display story#" + ((Story)parent.getItemAtPosition(position)).getStoryId());
            /*Intent intent = new Intent(getActivity().getApplicationContext(), StoryViewFragment.class);
            intent.putExtra("story", (Story)parent.getItemAtPosition(position));
			intent.putExtra("position", position);
			startActivity(intent);*/
		}
	};
	
    
    
	/***************************************************************************************
	 *
	 *                                DownloadStoriesTask Class
	 * 
	 ***************************************************************************************/
	private class ShareStoryTask extends AsyncTask<String, Integer, Void>
    {
          protected Void doInBackground(String ...params)
          {
              Bundle data = new Bundle();
              data.putString("my_message", "A friend wants to share a story with you");
              data.putString("my_action", "Let's see it now!");
              Communication.sendNotif(uId, "!!!!!!!!!!!!", "A friend wants to share a story with you", story);
              //activity.sendToGcm(data);
              return null;
          }
    }
	

	public Cursor getContacts() {
		Gen.appendLog("StoryShareFragment::getContacts> Starting");
		//mSelectionArgs[0] = "%" + mSearchString + "%";
		mSelectionArgs[0] = "1";
        // Starts the query
        return activity.getContentResolver().query(
            Contacts.CONTENT_URI,
            PROJECTION,
            SELECTION,
            mSelectionArgs,
            SORT_ORDER
        );
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
		Gen.appendLog("StoryShareFragment::onCreateLoader> Starting");
		//mSelectionArgs[0] = "%" + mSearchString + "%";
		mSelectionArgs[0] = "1";
		Gen.appendLog("StoryShareFragment::onCreateLoader> mSelectionArgs = " + mSelectionArgs[0]);
        // Starts the query
        return new CursorLoader(
            getActivity(),
            Contacts.CONTENT_URI,
            PROJECTION,
            SELECTION,
            mSelectionArgs,
            SORT_ORDER
        );
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Gen.appendLog("StoryShareFragment::onLoadFinished> Cursor loaded : " + cursor.getCount() + " rows read");
        //mCursorAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Gen.appendLog("StoryShareFragment::onLoaderReset> Starting");
        mCursorAdapter.swapCursor(null);
	}
}

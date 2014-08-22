package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mjumel.mystories.adapters.NavDrawerItem;
import com.mjumel.mystories.adapters.NavDrawerListAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Contacts;
import com.mjumel.mystories.tools.Gen;
import com.mjumel.mystories.tools.Prefs;
import com.mjumel.mystories.tools.SQLQuery;

public class DrawerActivity extends Activity {
	
	private Context context;
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
 
    // used to store app title
    private CharSequence mTitle;
 
    // slide menu items
    private String[] navMenuTitles, navMenuItems;
    private TypedArray navMenuIcons;
 
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    
    private int drawerPosition;
    private boolean isFirstCall = false;
    private boolean isNotif = false;
    private List<Event> eventList;
    private List<Story> storyList;
    private List<Story> sharedStoryList;
    private List<Contact> contactList;
    private String uid;
    
    
    private String regid;
    
    private SQLQuery sqlQuery;
    

 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Gen.appendLog("DrawerActivity::onCreate> Starting");
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_drawer);
        context = getApplicationContext();
 
        mTitle = getTitle();
 
        // load slide menu items
        navMenuItems = getResources().getStringArray(R.array.nav_drawer_items);
        
        // load slide menu titles
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_titles);
 
        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
 
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
 
        navDrawerItems = new ArrayList<NavDrawerItem>();
 
        // adding nav drawer items to array
        navDrawerItems.add(new NavDrawerItem(navMenuItems[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuItems[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuItems[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuItems[3], navMenuIcons.getResourceId(3, -1), true, "2"));
        navDrawerItems.add(new NavDrawerItem(navMenuItems[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuItems[5], navMenuIcons.getResourceId(5, -1)));
 
        // Recycle the typed array
        navMenuIcons.recycle();
 
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
 
        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);
 
        mDrawerToggle = new ActionBarDrawerToggle(
        		this, 
        		mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
            	super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }
 
            public void onDrawerOpened(View view) {
            	super.onDrawerOpened(view);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        ActionBar actionBar = getActionBar();    
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        setTitle("storiesme");
        
        eventList = new ArrayList<Event>();
    	storyList = new ArrayList<Story>();
    	sharedStoryList = new ArrayList<Story>();
    	
    	sqlQuery = new SQLQuery(context);
    	sqlQuery.open();
        
        // Getting events and stories lists
        if (getIntent().getExtras() != null) {
        	Gen.appendLog("DrawerActivity::onCreate> Getting extras");
        	uid = getIntent().getStringExtra("uid");
        	//if (getIntent().getStringExtra("origin").compareToIgnoreCase("splash") == 0) {
        	//	eventList = sqlQuery.getEvents(uid, null, null);
        	//	storyList = sqlQuery.getStories(uid, null, null);
        	//} else {
        		eventList = getIntent().getParcelableArrayListExtra("events");
        		storyList = getIntent().getParcelableArrayListExtra("stories");
        	//}
        	sharedStoryList = getIntent().getParcelableArrayListExtra("shared_stories");
        	contactList = getIntent().getParcelableArrayListExtra("contacts");
        	
        	isNotif = getIntent().hasExtra("story_id");
        }
        
    	if (contactList == null)
    		contactList = Contacts.getContacts(context);
        
    	Gen.appendLog("DrawerActivity::onCreate> Nb of events : " + eventList.size());
    	Gen.appendLog("DrawerActivity::onCreate> Nb of stories : " + storyList.size());
    	Gen.appendLog("DrawerActivity::onCreate> Nb of contacts : " + contactList.size());
    	Gen.appendLog("DrawerActivity::onCreate> User ID : " + uid);

        if (savedInstanceState == null) {
        	isFirstCall = true;
        	if (isNotif)
        		displayView(2);
        	else
        		displayView(0);
        }
 
        regid = Prefs.getRegistrationId(context);
        if (regid == null)
        	registerInBackground();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Gen.appendLog("DrawerActivity::onActivityResult> Starting");
        super.onActivityResult(requestCode, resultCode, data);
        Gen.appendLog("DrawerActivity::onActivityResult> Ending");
    }
 
    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }
 
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_new_event, menu);
        return true;
    }*/

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
        case R.id.action_settings:
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
 
    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
    	Gen.appendLog("DrawerActivity::onPrepareOptionsMenu> Starting");
        return super.onPrepareOptionsMenu(menu);
    }
 
    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
    	Gen.appendLog("DrawerActivity::displayView> Starting with position#" + position);

        Fragment fragment = null;
        Bundle bundle = new Bundle();
        bundle.putString("origin", "DrawerActivity");
        bundle.putString("title", navMenuTitles[position]);
        drawerPosition = position;
        switch (position) {
        case 0:
        	if (getIntent().getExtras() != null) {
        		bundle.putAll(getIntent().getExtras()); 
        		if (bundle.get(Intent.EXTRA_STREAM) != null)
        		{
        			fragment = new EventNewFragment();
        			changeFragment(fragment, bundle);
        			break;
        		}
        	}
            fragment = new EventListFragment();
            changeFragment(fragment, bundle);
            break;
        case 1:
            fragment = new StoryListFragment();
            changeFragment(fragment, bundle);
            break;
        case 2:
            fragment = new SharedStoryListFragment();
            changeFragment(fragment, bundle);
            break;
        case 5:
        	discoDialog();
        	break;
        default:
        	fragment = new GenFragment();
        	changeFragment(fragment, bundle);
            break;
        }
    }
    
    public void changeFragment(Fragment fragment, Bundle bundle)
    {
    	Gen.appendLog("DrawerActivity::changeFragment> Starting");
    	Gen.appendLog("DrawerActivity::changeFragment> drawerPosition = " + drawerPosition);
    	if (fragment != null) {
    		if (bundle != null) fragment.setArguments(bundle);
    		FragmentTransaction transaction = getFragmentManager().beginTransaction();
    		transaction.setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_in_left);
    		transaction.replace(R.id.frame_container, fragment, navMenuTitles[drawerPosition]);
    		if (isFirstCall) {
    			isFirstCall = false;
    			Gen.appendLog("DrawerActivity::changeFragment> First call, no back stack");
    		} else {
    			transaction.addToBackStack(navMenuTitles[drawerPosition]);
    			Gen.appendLog("DrawerActivity::changeFragment> Other call, back stack");
    		}
    		
    		transaction.commit();
 
            // Update selected item and title, then close the drawer
            mDrawerList.setItemChecked(drawerPosition, true);
            mDrawerList.setSelection(drawerPosition);
            setTitle(navMenuTitles[drawerPosition]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // Error in creating fragment
        	Gen.appendError("DrawerActivity::changeFragment> Error in creating fragment");
        }
    }
    
    /***************************************************************************************
	 *
	 *                                Communication between fragments
	 * 
	 ***************************************************************************************/
	
    public void sendEventList(List<Event> eventList) {
    	this.eventList = eventList;
    }
    
    public List<Event> getEventList() {
    	return eventList;
    }
    
    public void sendStoryList(List<Story> storyList) {
    	this.storyList = storyList;
    }
    
    public List<Story> getStoryList() {
    	return storyList;
    }
    
    public void sendSharedStoryList(List<Story> sharedStoryList) {
    	this.sharedStoryList = storyList;
    }
    
    public List<Story> getSharedStoryList() {
    	return sharedStoryList;
    }
    
    public String getUserId() {
    	return uid;
    }
    
    public void sendContactList(List<Contact> contactList) {
    	this.contactList = contactList;
    }
    
    public List<Contact> getContactList() {
    	return contactList;
    }
    
    public void removeEventFromStories(String eventId) {
    	for (Story s : storyList) {
    		for (Event e : s.getEvents()) {
    			if (e.getEventId().compareTo(eventId) == 0) {
    				s.getEvents().remove(e);
    				break;
    			}
    		}
    	}
    }
    
    public void updateEventOnStories(Event event) {
    	String eventId = event.getEventId();
    	Event eventTmp = null;
    	for (Story s : storyList) {
    		for (Event e : s.getEvents()) {
    			if (e.getEventId().compareTo(eventId) == 0) {
    				Gen.appendLog("DrawerActivity::updateEventOnStories> Update on story " + s.getTitle());
    				eventTmp = e;
    				break;
    			}
    		}
    		if (eventTmp != null) {
    			int pos = s.getEvents().indexOf(eventTmp);
    			s.getEvents().remove(pos);
    			s.getEvents().add(pos, event);
    			eventTmp = null;
    		}
    	}
    }
    
    
    
 
    @Override
    public void setTitle(CharSequence title) {
    	Gen.appendLog("DrawerActivity::setTitle> Setting title " + title);
        mTitle = title;
        getActionBar().setTitle(Gen.bicolorSpan(mTitle.toString(), 2));
    }
    
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
    */
 
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    private void discoDialog() {
    	Gen.appendLog("DrawerActivity::discoDialog> Starting");
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("Disconnect");
        myAlertDialog.setMessage("Do you really want to disconnect ?");

        myAlertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Prefs.removeAllExceptFirstRun(getApplicationContext());
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
    		    		intent.putExtra("origin", "DrawerActivity");
    					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    					startActivity(intent);
    					finish();
                    }
                });

        myAlertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    	return;
                    }
                });
        myAlertDialog.show();
    }
    
    
    
    
    @Override
    protected void onStart() {
    	Gen.appendLog("DrawerActivity::onStart> Starting");
        super.onStart();
        // The activity is about to become visible.
        Gen.appendLog("DrawerActivity::onStart> Ending");
    }
    @Override
    protected void onResume() {
    	Gen.appendLog("DrawerActivity::onResume> Starting");
        super.onResume();
        // The activity has become visible (it is now "resumed").
        Gen.appendLog("DrawerActivity::onResume> Ending");
    }
    @Override
    protected void onPause() {
    	Gen.appendLog("DrawerActivity::onPause> Starting");
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
        Gen.appendLog("DrawerActivity::onPause> Ending");
    }
    @Override
    protected void onStop() {
    	Gen.appendLog("DrawerActivity::onStop> Starting");
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        Gen.appendLog("DrawerActivity::onStop> Ending");
    }
    @Override
    protected void onRestart() {
    	Gen.appendLog("DrawerActivity::onRestart> Starting");
        super.onRestart();
        // The activity has become visible (it is now "resumed").
        Gen.appendLog("DrawerActivity::onRestart> Ending");
    }
    @Override
    protected void onDestroy() {
    	Gen.appendLog("DrawerActivity::onDestroy> Starting");
    	sqlQuery.close();
        super.onDestroy();
        // The activity is about to be destroyed.
        Gen.appendLog("DrawerActivity::onDestroy> Ending");
    }
    @Override
    protected void onSaveInstanceState(Bundle bundle) {
    	Gen.appendLog("DrawerActivity::onSaveInstanceState> Starting");
    }
    @Override
    protected void onRestoreInstanceState (Bundle bundle) {
    	Gen.appendLog("DrawerActivity::onRestoreInstanceState> Starting");
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return Communication.sendRegId(uid, regid);
            }

            @Override
            protected void onPostExecute(Boolean msg) {
                Gen.appendLog("DrawerActivity::registerInBackground> " + msg);
            }
        }.execute(null, null, null);
    }
}

package com.mjumel.mystories;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
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
import com.mjumel.mystories.tools.Gen;
import com.mjumel.mystories.tools.Prefs;

public class DrawerActivity extends Activity {
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
 
    // used to store app title
    private CharSequence mTitle;
 
    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
 
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    
    private int drawerPosition;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Gen.appendLog("DrawerActivity::onCreate> Starting");
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_drawer);
 
        mTitle = getTitle();
 
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
 
        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
 
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
 
        navDrawerItems = new ArrayList<NavDrawerItem>();
 
        // adding nav drawer items to array
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), true, "2"));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
 
        // Recycle the typed array
        navMenuIcons.recycle();
 
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
 
        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);
 
        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
 
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }
 
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
 
        if (savedInstanceState == null) {
        	// TODO
        	// Don't forget to change this value to 0 when debug is done
            displayView(1);
        }
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
        drawerPosition = position;
        switch (position) {
        case 0:
        	if (getIntent().getExtras() != null) {
        		bundle = getIntent().getExtras(); 
        		if (bundle.get(Intent.EXTRA_STREAM) != null)
        		{
        			fragment = new NewEventFragment();
        			break;
        		}
        	}
            fragment = new EventListFragment();
            changeFragment(fragment, bundle);
            break;
        case 1:
            fragment = new NewStoryFragment();
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
    		transaction.replace(R.id.frame_container, fragment);
    		transaction.addToBackStack(null);
    		transaction.commit();
 
            // Update selected item and title, then close the drawer
            mDrawerList.setItemChecked(drawerPosition, true);
            mDrawerList.setSelection(drawerPosition);
            setTitle(navMenuTitles[drawerPosition]);
            getActionBar().setIcon(navMenuIcons.getResourceId(drawerPosition, -1));
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // Error in creating fragment
        	Gen.appendLog("DrawerActivity::changeFragment> Error in creating fragment", "E");
        }
    }
 
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
 
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
 
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	Gen.appendLog("DrawerActivity::onConfigurationChanged> Starting");
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
        Gen.appendLog("DrawerActivity::onConfigurationChanged> Ending");
    }
    
    private static final String MS_PREFS_LOGIN = "MyStories_login";
	private static final String MS_PREFS_PWD = "MyStories_pwd";
	private static final String MS_PREFS_UID = "MyStories_uid";
    private void discoDialog() {
    	Gen.appendLog("DrawerActivity::discoDialog> Starting");
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("Disconnect");
        myAlertDialog.setMessage("Do you really want to disconnect ?");

        myAlertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Prefs.remove(getApplicationContext(), MS_PREFS_LOGIN);
                        Prefs.remove(getApplicationContext(), MS_PREFS_PWD);
                        Prefs.remove(getApplicationContext(), MS_PREFS_UID);
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
}

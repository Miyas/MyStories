package com.mjumel.mystories;

import java.util.HashSet;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mjumel.mystories.adapters.StoryShareAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;
import com.nostra13.universalimageloader.core.ImageLoader;

public class StoryShareFragment extends Fragment {
	
	private DrawerActivity activity;
	private String title = MyStoriesApp.APPLICATION_NAME;

	private View view;
	private TextView tvTitle;
	private ImageView imgView1;
	private ImageView imgView2;
	private ImageView imgView3;
	private EditText etMsg;
	private ListView lv;
	
	private Menu mMenu;
	
	private String uId = null;
	private Story story = null;
	private List<Story> storyList = null;
	private List<Contact> contactList = null;
	//private List<Contact> contactRegList = null;
	private int position = -1;
	
	private int contactsChecked = 0;
	private StoryShareAdapter adapter;
	
	
	
    public StoryShareFragment()
    {
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
    	activity = (DrawerActivity) getActivity();
		storyList = activity.getStoryList();
		contactList = activity.getContactList();
		uId = activity.getUserId();
		Gen.appendLog("StoryShareFragment::onCreate> uId=" + uId);

		if (getArguments() != null) {
			if (getArguments().containsKey("position")) {
				position = getArguments().getInt("position");
				Gen.appendLog("StoryShareFragment::onCreate> position = " + position);
		    	story = storyList.get(position);
		    	story.setSelected(false);
		    	activity.sendStoryList(storyList);
		    	Gen.appendLog("StoryShareFragment::onCreate> storyId = " + story.getStoryId());
				adapter = new StoryShareAdapter(getActivity(), this, contactList);
			}
			else {
				Toast.makeText(activity, "Error while loading story", Toast.LENGTH_SHORT).show();
				activity.getFragmentManager().popBackStackImmediate();
			}				

			if (getArguments().containsKey("title"))
				title = getArguments().getString("title");
		}
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
		lv.setAdapter(adapter);
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
	        	activity.getFragmentManager().popBackStackImmediate();
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
    	lv.setAdapter(adapter);
    	getActivity().setTitle(title);
    	
    	storyList = activity.getStoryList();
    	adapter.notifyDataSetChanged();
   		
    	Gen.appendLog("StoryShareFragment::onResume> Ending");
    }
    
    public void updateMenu(boolean delete)
    {
   		mMenu.findItem(R.id.share_story_share).setVisible(delete);
    }
    
    public void setNbContactsChecked(int nb) {
    	contactsChecked = nb;
    	updateMenu(contactsChecked>0);
	}
    
    
	/***************************************************************************************
	 *
	 *                                DownloadStoriesTask Class
	 * 
	 ***************************************************************************************/
	private class ShareStoryTask extends AsyncTask<String, Integer, Boolean>
    {
		protected Boolean doInBackground(String ...params)
		{
			HashSet<String> recip = new HashSet<String>();
			HashSet<String> mails = new HashSet<String>();
			for (Contact c : contactList) {
				if (c.isSelected()) {
					if (c.getRegId() == null) {
						if (c.getFirstMail() != null) {
							mails.add(c.getFirstMail());
							Intent i = new Intent(Intent.ACTION_SEND);
							i.setType("message/rfc822");
							i.putExtra(Intent.EXTRA_EMAIL  , new String[]{c.getFirstMail()});
							i.putExtra(Intent.EXTRA_SUBJECT, "StoriesLe - Someone wants to share a story with you!");
							i.putExtra(Intent.EXTRA_TEXT   , "Please come and download StoriesLe application in order to see the story");
							try {
								startActivity(Intent.createChooser(i, "Send mail..."));
							} catch (android.content.ActivityNotFoundException ex) {
								Toast.makeText(activity,  "There are no email clients installed.", Toast.LENGTH_SHORT).show();
							}
						}
					} else
						recip.add(c.getMyStoriesId());
					c.setSelected(false);
				}
			}
			return Communication.shareStory(uId, recip, story);
		}
          
          protected void onPostExecute(Boolean result) {
        	  if (result)
        		  Toast.makeText(activity, "Notification sent", Toast.LENGTH_SHORT).show();
        	  else
        		  Toast.makeText(activity, "Notification could not be sent, please retry later", Toast.LENGTH_SHORT).show();
        	  activity.sendContactList(contactList);
          }
    }
}

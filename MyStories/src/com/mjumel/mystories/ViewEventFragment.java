package com.mjumel.mystories;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;

import com.mjumel.mystories.adapters.EventListAdapter;
import com.mjumel.mystories.adapters.NothingSelectedSpinnerAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;
import com.mjumel.mystories.tools.ImageLoader;

public class ViewEventFragment extends Fragment {

	private View view;
	private Spinner spnCats;
	private Button btnRemember;
	private ImageView ivImage;
	private EditText etComment;
	private RatingBar rbRating, rbRatingMini;
	
	private Event event;
	private String mediaPath;
	private String uId;
	
	private static int SELECT_PICTURE = 1;
	private ImageLoader imageLoader;
	
	private Menu mMenu = null;
	private boolean editMode = false;
	private boolean fullScreenMode = false;
	
    public ViewEventFragment()
    {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	
    	event = (Event)getArguments().getParcelable("event");
    	mediaPath = event.getResizedMediaPath();

		Gen.appendLog("ViewEventFragment::onCreate> mediaPath = " + mediaPath);
		Gen.appendLog("ViewEventFragment::onCreate> uid = " + uId);
		
		imageLoader = new ImageLoader(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("ViewEventFragment::onCreateView> Starting");

		view = inflater.inflate(R.layout.fragment_view_event,container, false);
		spnCats = (Spinner) view.findViewById(R.id.view_event_cats);
		ivImage = (ImageView) view.findViewById(R.id.view_event_imageView);
		etComment = (EditText) view.findViewById(R.id.view_event_comment);
		btnRemember = (Button) view.findViewById(R.id.view_event_save);
		rbRating  = (RatingBar) view.findViewById(R.id.view_event_rating);
		rbRatingMini  = (RatingBar) view.findViewById(R.id.view_event_mini_rating);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.nav_spinner_cats, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnCats.setPrompt("Select your category");
		spnCats.setAdapter(
		  new NothingSelectedSpinnerAdapter(
		        adapter,
		        R.layout.cats_spinner_row_nothing_selected,
		        // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
		        this.getActivity()));
		
		if (mediaPath != null) { 
			imageLoader.DisplayImage(mediaPath, ivImage);
		}
		etComment.setText(event.getComment());
		rbRating.setProgress(event.getRating());
		rbRatingMini.setProgress(event.getRating());
		spnCats.setSelection(event.getCategoryId());
		
		ivImage.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (editMode) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
                } else 
                	goFullScreen();
            }
        });
		
		btnRemember.setOnClickListener(new OnClickListener() {           
            @Override
            public void onClick(View v) {
            	final ProgressDialog pg = ProgressDialog.show(getActivity(), "", "Posting event...", true);
                new Thread(new Runnable() {
                        public void run() {
                             Gen.appendLog("ViewEventFragment::onCreateView::onClick> uId = " + uId);
                             Gen.appendLog("ViewEventFragment::onCreateView::onClick> comment = " + etComment.getText().toString());
                             Gen.appendLog("ViewEventFragment::onCreateView::onClick> rating = " + rbRating.getProgress());
                             Gen.appendLog("ViewEventFragment::onCreateView::onClick> mediaPath = " + mediaPath);
                             Gen.appendLog("ViewEventFragment::onCreateView::onClick> cat = " + spnCats.getSelectedItemPosition());
                             Communication.postEvent(
                            		 uId, 
                            		 etComment.getText().toString(), 
                            		 rbRating.getProgress(), 
                            		 mediaPath,
                            		 spnCats.getSelectedItemPosition()
                             );
                             getActivity().runOnUiThread(new Runnable() {
                                 public void run() {
                                	 pg.dismiss();
                                 }
                             });
                        }
                      }).start();                
            }
         });
		
		Gen.appendLog("ViewEventFragment::onCreateView> Ending");
		return view;
    }
    
    @SuppressWarnings("static-access")
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK && requestCode == SELECT_PICTURE) {
        	mediaPath = data.getData().toString();
        	imageLoader.DisplayImage(mediaPath, ivImage);
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	Gen.appendLog("ViewEventFragment::onCreateOptionsMenu> Starting");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_view_event, menu);
        mMenu = menu;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.view_event_edit:
        	if (mMenu != null)
        	{
        		mMenu.findItem(R.id.view_event_link).setVisible(false);
        		mMenu.findItem(R.id.view_event_edit).setVisible(false);
        		mMenu.findItem(R.id.view_event_menu_overflow).setVisible(false);
        		mMenu.findItem(R.id.edit_event_cancel).setVisible(true);
        		mMenu.findItem(R.id.edit_event_save).setVisible(true);
        		
        		rbRating.setVisibility(RatingBar.VISIBLE);
        		rbRatingMini.setVisibility(RatingBar.GONE);
        		spnCats.setClickable(true);
        		etComment.setClickable(true);
        		ivImage.setClickable(true);
        		
        		editMode = true;
        	}
            return true;
        case R.id.edit_event_cancel:
        case R.id.edit_event_save:
        	if (mMenu != null)
        	{
        		mMenu.findItem(R.id.view_event_link).setVisible(true);
        		mMenu.findItem(R.id.view_event_edit).setVisible(true);
        		mMenu.findItem(R.id.view_event_menu_overflow).setVisible(true);
        		mMenu.findItem(R.id.edit_event_cancel).setVisible(false);
        		mMenu.findItem(R.id.edit_event_save).setVisible(false);
        		
        		rbRating.setVisibility(RatingBar.GONE);
        		rbRatingMini.setVisibility(RatingBar.VISIBLE);
        		spnCats.setClickable(false);
        		etComment.setClickable(false);
        		ivImage.setClickable(false);
        		
        		editMode = false;
        	}
            return true;
        case R.id.view_event_full_screen:
        	goFullScreen();
        	return true;
        case R.id.view_event_back:
        	goFullScreen();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    	Gen.appendLog("ViewEventFragment::onPrepareOptionsMenu> Starting");
    }
    
    private void goFullScreen()
    {
    	if (!fullScreenMode) {
    		if (mMenu != null) {
	    		mMenu.findItem(R.id.view_event_link).setVisible(false);
	    		mMenu.findItem(R.id.view_event_edit).setVisible(false);
	    		mMenu.findItem(R.id.view_event_menu_overflow).setVisible(false);
	    		mMenu.findItem(R.id.view_event_back).setVisible(true);
    		}
    		
    		view.setPadding(0, 0, 0, 0);
    		spnCats.setVisibility(RatingBar.GONE);
    		etComment.setVisibility(RatingBar.GONE);
    		btnRemember.setVisibility(RatingBar.GONE);
    		
    		fullScreenMode = true;
    	} else {
    		if (mMenu != null) {
	    		mMenu.findItem(R.id.view_event_link).setVisible(true);
	    		mMenu.findItem(R.id.view_event_edit).setVisible(true);
	    		mMenu.findItem(R.id.view_event_menu_overflow).setVisible(true);
	    		mMenu.findItem(R.id.view_event_back).setVisible(false);
    		}

    		int padV = R.dimen.activity_vertical_margin;
    		int padH = R.dimen.activity_horizontal_margin;
    		//view.setPadding(padH, padV, padH, padV);
    		spnCats.setVisibility(RatingBar.VISIBLE);
    		etComment.setVisibility(RatingBar.VISIBLE);
    		btnRemember.setVisibility(RatingBar.VISIBLE);
    	    		
    		fullScreenMode = false;
    	}
    	rbRatingMini.setVisibility(RatingBar.VISIBLE);
    	rbRating.setVisibility(RatingBar.GONE);
    	//ivImage.setClickable(false);
    }
}

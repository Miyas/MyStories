package com.mjumel.mystories;

import java.io.IOException;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;

import com.mjumel.mystories.adapters.NothingSelectedSpinnerAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;
import com.mjumel.mystories.tools.ImageLoader;
import com.mjumel.mystories.tools.UserPicture;

public class ViewEventFragment extends Fragment {

	private Spinner spnCats;
	private Button btnRemember;
	private ImageView ivImage;
	private EditText etComment;
	private RatingBar rbRating;
	
	private Event event;
	private Uri mediaUri;
	private String uId;
	
	private static int SELECT_PICTURE = 1;
	private UserPicture userPicture;
	private ImageLoader imageLoader; 
	
    public ViewEventFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("ViewEventFragment::onCreateView> Starting");

    	event = (Event)getArguments().getParcelable("event");
    	mediaUri = Uri.parse(event.getResizedMediaPath());

		Gen.appendLog("ViewEventFragment::onCreateView> mediaUri = " + mediaUri);
		Gen.appendLog("ViewEventFragment::onCreateView> uid = " + uId);
		
		imageLoader = new ImageLoader(getActivity().getApplicationContext());
		//userPicture = new UserPicture(mediaUri, getActivity().getContentResolver());

		View view = inflater.inflate(R.layout.fragment_new_event,container, false);
		spnCats = (Spinner) view.findViewById(R.id.event_cats);
		ivImage = (ImageView) view.findViewById(R.id.event_imageView);
		etComment = (EditText) view.findViewById(R.id.event_comment);
		btnRemember = (Button) view.findViewById(R.id.event_save);
		rbRating  = (RatingBar) view.findViewById(R.id.event_rating);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.nav_spinner_cats, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnCats.setPrompt("Select your category");
		spnCats.setAdapter(
		  new NothingSelectedSpinnerAdapter(
		        adapter,
		        R.layout.cats_spinner_row_nothing_selected,
		        // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
		        this.getActivity()));
		
		if (mediaUri != null) { 
			//setImage(mediaUri);
			imageLoader.DisplayImage(mediaUri.toString(), ivImage);
		}
		etComment.setText(event.getComment());
		rbRating.setProgress(event.getRating());
		spnCats.setSelection(event.getCategoryId());
		
		ivImage.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                // in onCreate or any event where your want the user to
                // select a file
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
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
                             Gen.appendLog("ViewEventFragment::onCreateView::onClick> mediaUri = " + (mediaUri == null?null:userPicture.getPath()));
                             Gen.appendLog("ViewEventFragment::onCreateView::onClick> cat = " + spnCats.getSelectedItemPosition());
                             Communication.postEvent(
                            		 uId, 
                            		 etComment.getText().toString(), 
                            		 rbRating.getProgress(), 
                            		 (mediaUri == null?null:userPicture.getPath()),
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
            setImage(data.getData());
        }
    }
    
    private void setImage(Uri uri)
    {
    	mediaUri = uri;
    	userPicture.setUri(mediaUri);
        try {
			ivImage.setImageBitmap(userPicture.getBitmap());
		} catch (IOException e) {
			Gen.appendLog("ViewEventFragment::setImage> IOException error", "E");
			Gen.appendLog("ViewEventFragment::setImage> " + e.getLocalizedMessage(), "E");
		}
    }
}

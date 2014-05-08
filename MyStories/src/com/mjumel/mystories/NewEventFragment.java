package com.mjumel.mystories;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.mjumel.mystories.adapters.NothingSelectedSpinnerAdapter;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;
import com.mjumel.mystories.tools.ImageWorker;
import com.mjumel.mystories.tools.UserPicture;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

public class NewEventFragment extends Fragment {

	private Spinner spnCats;
	private Button btnRemember;
	private ImageView ivImage;
	private EditText etComment;
	private RatingBar rbRating;
	
	private Uri mediaUri;
	private Uri mImageCaptureUri;
	private String uId;
	private String mediaLocalPath;
	
	private Intent pictureActionIntent = null;
	protected static final int CAMERA_REQUEST = 0;
	protected static final int GALLERY_PICTURE = 1;
	
	private static final int SELECT_PICTURE = 2;
	private UserPicture userPicture;
	
    public NewEventFragment()
    {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Gen.appendLog("NewEventFragment::onCreate> Starting");
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    	Gen.appendLog("NewEventFragment::onCreate> Ending");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("NewEventFragment::onCreateView> Starting");

    	mediaUri = (Uri)getExtra(Intent.EXTRA_STREAM);
		uId = (String)getExtra("uid");
		getActivity().getIntent().putExtra(Intent.EXTRA_STREAM, (String)null);

		Gen.appendLog("NewEventFragment::onCreateView> mediaUri = " + mediaUri);
		Gen.appendLog("NewEventFragment::onCreateView> uid = " + uId);
		
		userPicture = new UserPicture(mediaUri, getActivity().getContentResolver());

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
		
		if (mediaUri != null)
			setImage(mediaUri);
		
		ivImage.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                startDialog();
            }
        });
		
		btnRemember.setOnClickListener(new OnClickListener() {           
            @Override
            public void onClick(View v) {
            	final ProgressDialog pg = ProgressDialog.show(getActivity(), "", "Posting event...", true);
                new Thread(new Runnable() {
                        public void run() {
                             Gen.appendLog("NewEventFragment::onCreateView::onClick> uId = " + uId);
                             Gen.appendLog("NewEventFragment::onCreateView::onClick> comment = " + etComment.getText().toString());
                             Gen.appendLog("NewEventFragment::onCreateView::onClick> rating = " + rbRating.getProgress());
                             Gen.appendLog("NewEventFragment::onCreateView::onClick> mediaPath = " + mediaUri==null?null:ImageWorker.getPath(getActivity(), mediaUri));
                             Gen.appendLog("NewEventFragment::onCreateView::onClick> cat = " + spnCats.getSelectedItemPosition());
                            
                             Communication.postEvent(
                            		 uId, 
                            		 etComment.getText().toString(), 
                            		 rbRating.getProgress(), 
                            		 mediaUri==null?null:ImageWorker.getPath(getActivity(), mediaUri),
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
		
		Gen.appendLog("NewEventFragment::onCreateView> Ending");
		return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_new_event, menu);
    }
    
    @SuppressWarnings("static-access")
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Gen.appendLog("NewEventFragment::onActivityResult> Starting");
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (resultCode != getActivity().RESULT_OK) {
    		Gen.appendLog("NewEventFragment::onActivityResult> RESULT_KO (Cancelled?)");
    		return;
    	}
    	
    	mediaUri = null;
    	mediaLocalPath = null;
    	switch(requestCode) {
	    	case(SELECT_PICTURE):
	    		Gen.appendLog("NewEventFragment::onActivityResult> Case SELECT_PICTURE");
	    		mediaUri = data.getData();
	    		break;
	    	case(GALLERY_PICTURE):
	    		Gen.appendLog("NewEventFragment::onActivityResult> Case GALLERY_PICTURE");
                if (data != null) {
                	Gen.appendLog("NewEventFragment::onActivityResult> Url = " + data.getData());
                	//mediaLocalPath = ImageWorker.getPath(getActivity(), data.getData());
                	mediaUri = data.getData();
                }
	    		break;
	    	case(CAMERA_REQUEST):
	    		Gen.appendLog("NewEventFragment::onActivityResult> Case CAMERA_REQUEST");

	    		Uri mImageCaptureUri_samsung = null;
                // Final Code As Below
                //try {
                	if (data != null) {
                		Gen.appendLog("NewEventFragment::onActivityResult> inside Classical Phones");
                    	mediaUri = data.getData();
                    	break;
                    }
                	
                	Gen.appendLog("NewEventFragment::onActivityResult> inside Samsung Phones");
                    String[] projection = {
                            MediaStore.Images.Thumbnails._ID, // The columns we want
                            MediaStore.Images.Thumbnails.IMAGE_ID,
                            MediaStore.Images.Thumbnails.KIND,
                            MediaStore.Images.Thumbnails.DATA };
                    String selection = MediaStore.Images.Thumbnails.KIND + "=" + // Select
                                                                                    // only
                                                                                    // mini's
                            MediaStore.Images.Thumbnails.MINI_KIND;

                    String sort = MediaStore.Images.Thumbnails._ID + " DESC";

                    // At the moment, this is a bit of a hack, as I'm returning ALL
                    // images, and just taking the latest one. There is a better way
                    // to
                    // narrow this down I think with a WHERE clause which is
                    // currently
                    // the selection variable
                    Cursor myCursor = getActivity().getContentResolver().query(
                            MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                            projection, selection, null, sort);

                    long imageId = 0l;
                    long thumbnailImageId = 0l;
                    String thumbnailPath = "";

                    try {
                        myCursor.moveToFirst();
                        imageId = myCursor
                                .getLong(myCursor
                                        .getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
                        thumbnailImageId = myCursor
                                .getLong(myCursor
                                        .getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID));
                        thumbnailPath = myCursor
                                .getString(myCursor
                                        .getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
                    } finally {
                        // myCursor.close();
                    }

                    // Create new Cursor to obtain the file Path for the large image

                    String[] largeFileProjection = {
                            MediaStore.Images.ImageColumns._ID,
                            MediaStore.Images.ImageColumns.DATA };

                    String largeFileSort = MediaStore.Images.ImageColumns._ID
                            + " DESC";
                    myCursor = getActivity().getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            largeFileProjection, null, null, largeFileSort);
                    String largeImagePath = "";

                    try {
                        myCursor.moveToFirst();

                        // This will actually give you the file path location of the
                        // image.
                        largeImagePath = myCursor
                                .getString(myCursor
                                        .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
                        mImageCaptureUri_samsung = Uri.fromFile(new File(largeImagePath));
                        //mImageCaptureUri = null;
                    } finally {
                        // myCursor.close();
                    }

                    // These are the two URI's you'll be interested in. They give
                    // you a handle to the actual images
                    Uri uriLargeImage = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            String.valueOf(imageId));
                    Uri uriThumbnailImage = Uri.withAppendedPath(
                            MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                            String.valueOf(thumbnailImageId));

                    if (mImageCaptureUri_samsung != null)
                    	mediaUri = mImageCaptureUri_samsung;
                /*} catch (Exception e) {
                    mImageCaptureUri_samsung = null;
                    Gen.appendLog("inside catch Samsung Phones exception " + e.toString(), "E");
                }*/
                break;
    	}
    	if (mediaUri != null)
    		setImage(mediaUri);
    	else {
    		Toast.makeText(getActivity(), "Problem getting back the image", Toast.LENGTH_SHORT).show();
    		Gen.appendError("NewEventFragment::onActivityResult> Problem getting back the image");
    	}
    }
    
    private void startDialog() {
    	Gen.appendLog("NewEventFragment::startDialog> Starting");
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(getActivity());
        myAlertDialog.setTitle("Get Pictures Option");
        myAlertDialog.setMessage("Where do you want to get your picture from?");

        myAlertDialog.setPositiveButton(R.string.alert1_gallery,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        pictureActionIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
                        pictureActionIntent.setType("image/*");
                        pictureActionIntent.putExtra("return-data", true);
                        startActivityForResult(pictureActionIntent, GALLERY_PICTURE);
                    }
                });

        myAlertDialog.setNegativeButton(R.string.alert1_camera,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //pictureActionIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        //startActivityForResult(pictureActionIntent, CAMERA_REQUEST);
                        
                        // Other Method
                        String storageState = Environment.getExternalStorageState();

                        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                            pictureActionIntent = new Intent(
                                    MediaStore.ACTION_IMAGE_CAPTURE);

                            String filename = System.currentTimeMillis() + ".jpg";
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, filename);
                            mImageCaptureUri = getActivity().getContentResolver().insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    values);

                            pictureActionIntent.putExtra(
                                    android.provider.MediaStore.EXTRA_OUTPUT,
                                    mImageCaptureUri);

                            try {
                                startActivityForResult(pictureActionIntent, CAMERA_REQUEST);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getActivity(), "SD Card is required", Toast.LENGTH_SHORT).show();
                            Gen.appendLog("NewEventFragment::startDialog> SD Card is required", "E");
                        }

                    }
                });
        myAlertDialog.show();
    }
    
    private void setImage(Uri uri)
    {
    	Gen.appendLog("NewEventFragment::setImage> Starting with uri=" + uri.toString());
    	mediaUri = uri;
    	/*MyStoriesApp.getPicasso()
	    	.load(mediaUri)
	    	.fit().centerCrop()
	    	.error(R.drawable.ic_action_cancel)
	    	.placeholder(R.drawable.ic_action_refresh)
	    	//.resize(80, 80)
	    	.into(ivImage);*/
    	
    	ImageLoader.getInstance().displayImage(mediaUri.toString(), ivImage);
    	Gen.appendLog("NewEventFragment::setImage>Loading uri=" + ImageLoader.getInstance().getLoadingUriForView(ivImage));
    	
	    	
    	
    	/*userPicture.setUri(mediaUri);
        try {
			ivImage.setImageBitmap(userPicture.getBitmap());
		} catch (IOException e) {
			Gen.appendLog("NewEventFragment::setImage> IOException error", "E");
			Gen.appendLog("NewEventFragment::setImage> " + e.getLocalizedMessage(), "E");
		}*/
    }
    
    private Object getExtra(String id)
    {
    	if (this.getActivity().getIntent().getExtras() != null)
			return this.getActivity().getIntent().getExtras().get(id);
    	else
    		return null;
    }
    
    @Override
	public void onStart() {
    	Gen.appendLog("NewEventFragment::onStart> Starting");
        super.onStart();
        // The activity is about to become visible.
        Gen.appendLog("NewEventFragment::onStart> Ending");
    }
    @Override
    public void onResume() {
    	Gen.appendLog("NewEventFragment::onResume> Starting");
        super.onResume();
        // The activity has become visible (it is now "resumed").
        Gen.appendLog("NewEventFragment::onResume> Ending");
    }
    @Override
    public void onPause() {
    	Gen.appendLog("NewEventFragment::onPause> Starting");
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
        Gen.appendLog("NewEventFragment::onPause> Ending");
    }
    @Override
    public void onStop() {
    	Gen.appendLog("NewEventFragment::onStop> Starting");
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        Gen.appendLog("NewEventFragment::onStop> Ending");
    }
    @Override
    public void onDestroy() {
    	Gen.appendLog("NewEventFragment::onDestroy> Starting");
        super.onDestroy();
        // The activity is about to be destroyed.
        Gen.appendLog("NewEventFragment::onDestroy> Ending");
    }
    @Override
    public void onSaveInstanceState(Bundle bundle) {
    	Gen.appendLog("NewEventFragment::onSaveInstanceState> Starting");
    }
}

package com.mjumel.mystories;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.mjumel.mystories.tools.Gen;

public class Story implements Parcelable {
	
	private String storyId = null;
	private String title = null;
	private int rating = -1;
	private String uId = null;
	private List<Event> events;
	
	// Display options
	private boolean isSelected = false;
	private boolean showStars = true;
	private boolean showText = true;
	private boolean showMedia = true;
	private boolean showCategory = true;
	
	public Story() {
		Gen.appendLog("Story::Story> Creating new story with constructor#1");
    }
	
	@SuppressWarnings("unchecked")
	public Story(Parcel in) {
		Gen.appendLog("Story::Story> Creating new story with constructor#2");
		this.storyId = in.readString();
		this.title = in.readString();
		this.rating = in.readInt();
		this.uId = in.readString();
		this.events = in.readArrayList(Event.class.getClassLoader());
	}
	
	public Story(String storyId, String title, int rating, String uId, List<Event> events) 
    {
		Gen.appendLog("Story::Story> Creating new story with constructor#3");
		this.storyId = storyId;
		this.title = title;
		this.rating = rating;
		this.uId = uId;
		this.events = events;
		print();
    }
	
	public Story(String title, String uId, List<Event> events) 
    {
		Gen.appendLog("Story::Story> Creating new story with constructor#4");
		this.title = title;
		this.uId = uId;
		this.events = events;
		print();
    }
	
	public void print()
	{
		Gen.appendLog("Story::print> storyId = " + storyId);
		Gen.appendLog("Story::print> title = " + title);
		Gen.appendLog("Story::print> rating = " + rating);
		Gen.appendLog("Story::print> uId = " + uId);
		Gen.appendLog("Story::print> events = " + null);
	}
	
	public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    
    public int getRating() { return rating; }
    public void setRating(int value) { rating = value; }
    
    public String getUserId() { return uId; }
    public void setUserId(String value) { uId = value; }
    
    public String getStoryId() { return storyId; }
    public void setStoryId(String value) { storyId = value; }
    
    public List<Event> getEvents() { return events; }
    public void setEvents(List<Event> value) { events = value; }
    
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean value) { isSelected = value; }
    
    public boolean shownStars() { return showStars; }
    public void showStars() { showStars = true; }
    public void hideStars() { showStars = false; }
    
    public boolean shownText() { return showText; }
    public void showText() { showText = true; }
    public void hideText() { showText = false; }
    
    public boolean shownMedia() { return showMedia; }
    public void showMedia() { showMedia = true; }
    public void hideMedia() { showMedia = false; }
    
    public boolean shownCategory() { return showCategory; }
    public void showCategory() { showCategory = true; }
    public void hideCategory() { showCategory = false; }
    
    

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(storyId);
		dest.writeString(title);
		dest.writeInt(rating);
		dest.writeString(uId);
		dest.writeList(events);
	}
	
	public static final Parcelable.Creator<Story> CREATOR = new Parcelable.Creator<Story>()
	{
	    @Override
	    public Story createFromParcel(Parcel source) {
	        return new Story(source);
	    }

	    @Override
	    public Story[] newArray(int size) {
	    	return new Story[size];
	    }
	};
}

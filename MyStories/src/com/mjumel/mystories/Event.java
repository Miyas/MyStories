package com.mjumel.mystories;

import android.os.Parcel;
import android.os.Parcelable;

import com.mjumel.mystories.tools.Gen;

public class Event implements Parcelable {
	
	private String eventId = null;
	private String comment = null;
	private int rating = -1;
	private int category = -1;
	private String[] mediaPath = new String[3];
	private String uId = null;
	private String storyId = null;
	
	// Display variables
	private boolean isSelected = false;
	private boolean showStars = true;
	private boolean showText = true;
	private boolean showMedia = true;
	private boolean showCategory = true;
	
	private final String baseUrl = "http://anizoo.info/mystories/"; 
	
	public Event()
    {
    }
	
	public Event(Parcel in) {
		this.comment = in.readString();
		this.mediaPath = (String[])in.readArray(String.class.getClassLoader());
		this.eventId = in.readString();
		this.rating = in.readInt();
		this.category = in.readInt();
		this.uId = in.readString();
		this.storyId = in.readString();
	}
	
	public Event(String comment, int rating, int category, 
			String[] mediaPath, String uId, String storyId, String eventId) 
    {
		Gen.appendLog("Event::Event> Creating new event");
		this.comment = comment;
		this.rating = rating;
		this.category = category;
		this.mediaPath = mediaPath;
		this.uId = uId;
		this.storyId = storyId;
		this.eventId = eventId;
		print();
    }
	
	public void print()
	{
		Gen.appendLog("Event::printEvent> comment = " + comment);
		Gen.appendLog("Event::printEvent> rating = " + rating);
		Gen.appendLog("Event::printEvent> category = " + category);
		Gen.appendLog("Event::printEvent> mediaPath = " + mediaPath);
		Gen.appendLog("Event::printEvent> uId = " + uId);
		Gen.appendLog("Event::printEvent> storyId = " + storyId);
		Gen.appendLog("Event::printEvent> eventId = " + eventId);
	}
	
	public String getComment() { return comment; }
    public void setComment(String value) { comment = value; }
    
    public String getThumbMediaPath() { return mediaPath[0]==null?null:baseUrl+mediaPath[0]; }
    public void setThumbMediaPath(String value) { mediaPath[0] = value; }
    
    public String getResizedMediaPath() { return mediaPath[1]==null?null:baseUrl+mediaPath[1]; }
    public void setResizedMediaPath(String value) { mediaPath[1] = value; }
    
    public String getOriginalMediaPath() { return mediaPath[2]==null?null:baseUrl+mediaPath[2]; }
    public void setOriginalMediaPath(String value) { mediaPath[2] = value; }
    
    public int getRating() { return rating; }
    public void setRating(int value) { rating = value; }
    
    public int getCategoryId() { return category; }
    public void setCategoryId(int value) { category = value; }
    
    public String getUserId() { return uId; }
    public void setUserId(String value) { uId = value; }
    
    public String getStoryId() { return storyId; }
    public void setStoryId(String value) { storyId = value; }
    
    public String getEventId() { return eventId; }
    public void setEventId(String value) { eventId = value; }
    
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
		dest.writeString(comment);
		dest.writeArray(mediaPath);
		dest.writeString(eventId);
		dest.writeInt(rating);
		dest.writeInt(category);
		dest.writeString(uId);
		dest.writeString(storyId);
	}
	
	public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>()
	{
	    @Override
	    public Event createFromParcel(Parcel source) {
	        return new Event(source);
	    }

	    @Override
	    public Event[] newArray(int size) {
	    	return new Event[size];
	    }
	};
}

package com.mjumel.mystories;

import android.os.Parcel;
import android.os.Parcelable;

import com.mjumel.mystories.tools.Gen;

public class Event implements Parcelable {
	
	private String eventId = null;
	private String comment = null;
	private int rating = -1;
	private int category = -1;
	private String thumbMediaPath = null;
	private String resizedMediaPath = null;
	private String originalMediaPath = null;
	private String uId = null;
	private String storyId = null;
	
	// Display variables
	private boolean isSelected = false;
	private boolean showStars = true;
	private boolean showText = true;
	private boolean showMedia = true;
	private boolean showCategory = true;
	
	private final String baseUrl = "http://anizoo.info/mystories/"; 
	
	public Event() {
		//Gen.appendLog("Event::Event> Creating empty event");
		this.eventId = "-1";
    }
	
	public Event(Parcel in) {
		this.comment = in.readString();
		this.thumbMediaPath = in.readString();
		this.resizedMediaPath = in.readString();
		this.originalMediaPath = in.readString();
		this.eventId = in.readString();
		this.rating = in.readInt();
		this.category = in.readInt();
		this.uId = in.readString();
		this.storyId = in.readString();
		this.isSelected = in.readInt()==0?false:true;
		this.showStars = in.readInt()==0?false:true;
		this.showText = in.readInt()==0?false:true;
		this.showMedia = in.readInt()==0?false:true;
		this.showCategory = in.readInt()==0?false:true;
	}
	
	public Event(String comment, int rating, int category, 
			String[] mediaPath, String uId, String storyId, String eventId) 
    {
		//Gen.appendLog("Event::Event> Creating new event");
		this.comment = comment;
		this.rating = rating;
		this.category = category;
		if (mediaPath != null) {
			this.thumbMediaPath = mediaPath[0];
			this.resizedMediaPath = mediaPath[1];
			this.originalMediaPath = mediaPath[2];
		}
		this.uId = uId;
		this.storyId = storyId;
		this.eventId = eventId;
		//print();
    }
	
	public Event(String comment, int rating, int category, 
			String thumbMediaPath, String resizedMediaPath, String originalMediaPath,
			String uId, String storyId, String eventId) 
    {
		//Gen.appendLog("Event::Event> Creating new event");
		this.comment = comment;
		this.rating = rating;
		this.category = category;
		this.thumbMediaPath = thumbMediaPath;
		this.resizedMediaPath = resizedMediaPath;
		this.originalMediaPath = originalMediaPath;
		this.uId = uId;
		this.storyId = storyId;
		this.eventId = eventId;
		//print();
    }
	
	public void print()
	{
		Gen.appendLog("Event::printEvent> eventId = " + eventId);
		Gen.appendLog("Event::printEvent> storyId = " + storyId);
		Gen.appendLog("Event::printEvent> uId = " + uId);
		Gen.appendLog("Event::printEvent> comment = " + comment);
		Gen.appendLog("Event::printEvent> rating = " + rating);
		Gen.appendLog("Event::printEvent> category = " + category);
		Gen.appendLog("Event::printEvent> ThumbMediaPath = " + getThumbMediaPath());
		Gen.appendLog("Event::printEvent> ResizedMediaPath = " + getResizedMediaPath());
		Gen.appendLog("Event::printEvent> OriginalMediaPath = " + getOriginalMediaPath());
	}
	
	public String getComment() { return comment; }
    public void setComment(String value) { comment = value; }
    
    public String getThumbMediaPath() { return thumbMediaPath == null ? null : baseUrl+thumbMediaPath; }
    public void setThumbMediaPath(String value) { thumbMediaPath = value; }
    
    public String getResizedMediaPath() { return resizedMediaPath == null ? null : baseUrl+resizedMediaPath; }
    public void setResizedMediaPath(String value) { resizedMediaPath = value; }
    
    public String getOriginalMediaPath() { return originalMediaPath == null ? null : baseUrl+originalMediaPath; }
    public void setOriginalMediaPath(String value) { originalMediaPath = value; }
    
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
		dest.writeString(thumbMediaPath);
		dest.writeString(resizedMediaPath);
		dest.writeString(originalMediaPath);
		dest.writeString(eventId);
		dest.writeInt(rating);
		dest.writeInt(category);
		dest.writeString(uId);
		dest.writeString(storyId);
		dest.writeInt(isSelected?1:0);
		dest.writeInt(showStars?1:0);
		dest.writeInt(showText?1:0);
		dest.writeInt(showMedia?1:0);
		dest.writeInt(showCategory?1:0);
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

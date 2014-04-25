package com.mjumel.mystories;

import android.os.Parcel;
import android.os.Parcelable;

import com.mjumel.mystories.tools.Gen;

public class Event implements Parcelable {
	
	private int eventId = -1;
	private String comment = null;
	private int rating = -1;
	private int category = -1;
	private String[] mediaPath = new String[3];
	private int uId = -1;
	private int storyId = -1;
	
	private final String baseUrl = "http://anizoo.info/mystories/"; 
	
	public Event()
    {
    }
	
	public Event(Parcel in) {
		this.comment = in.readString();
		this.mediaPath = (String[])in.readArray(String.class.getClassLoader());
		this.eventId = in.readInt();
		this.rating = in.readInt();
		this.category = in.readInt();
		this.uId = in.readInt();
		this.storyId = in.readInt();
	}
	
	public Event(String comment, int rating, int category, 
			String[] mediaPath, int uId, int storyId, int eventId) 
    {
		Gen.appendLog("Event::Event> Creating new event");
		this.comment = comment;
		this.rating = rating;
		this.category = category;
		this.mediaPath = mediaPath;
		this.uId = uId;
		this.storyId = storyId;
		this.eventId = eventId;
		printEvent();
    }
	
	public void printEvent()
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
    
    public int getUserId() { return uId; }
    public void setUserId(int value) { uId = value; }
    
    public int getStoryId() { return storyId; }
    public void setStoryId(int value) { storyId = value; }
    
    public int getEventId() { return eventId; }
    public void setEventId(int value) { eventId = value; }

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(comment);
		dest.writeArray(mediaPath);
		dest.writeInt(eventId);
		dest.writeInt(rating);
		dest.writeInt(category);
		dest.writeInt(uId);
		dest.writeInt(storyId);
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

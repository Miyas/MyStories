package com.mjumel.mystories;

public class Event {
	
	private int eventId = -1;
	private String comment = null;
	private int rating = -1;
	private int category = -1;
	private String[] mediaPath = new String[3];
	private int uId = -1;
	private int storyId = -1;
	
	public Event() 
    {
    }
	
	public Event(String comment, int rating, int category, 
			String[] mediaPath, int uId, int storyId, int eventId) 
    {
		this.comment = comment;
		this.rating = rating;
		this.category = category;
		this.mediaPath = mediaPath;
		this.uId = uId;
		this.storyId = storyId;
		this.eventId = eventId;
    }
	
	public String GetComment() { return comment; }
    public void SetComment(String value) { comment = value; }
    
    public String GetThumbMediaPath() { return mediaPath[0]; }
    public void SetThumbMediaPath(String value) { mediaPath[0] = value; }
    
    public String GetResizedMediaPath() { return mediaPath[1]; }
    public void SetResizedMediaPath(String value) { mediaPath[1] = value; }
    
    public String GetOriginalMediaPath() { return mediaPath[2]; }
    public void SetOriginalMediaPath(String value) { mediaPath[2] = value; }
    
    public int GetRating() { return rating; }
    public void SetRating(int value) { rating = value; }
    
    public int GetCategoryId() { return category; }
    public void SetCategoryId(int value) { category = value; }
    
    public int GetUserId() { return uId; }
    public void SetUserId(int value) { uId = value; }
    
    public int GetStoryId() { return storyId; }
    public void SetStoryId(int value) { storyId = value; }
    
    public int GetEventId() { return eventId; }
    public void SetEventId(int value) { eventId = value; }
}

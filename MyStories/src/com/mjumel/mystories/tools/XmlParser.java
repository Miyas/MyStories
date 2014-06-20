package com.mjumel.mystories.tools;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.net.Uri;
import android.util.Xml;

import com.mjumel.mystories.Event;
import com.mjumel.mystories.Story;

public class XmlParser {
    
    public List<Event> parseEvents(Reader in) throws XmlPullParserException, IOException {
    	//Gen.appendLog("XmlParser::parseEvents> Starting");
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in);
            //parser.nextTag();
            return parseEvents(parser);
        } finally {
            in.close();
        }
    }

    private List<Event> parseEvents(XmlPullParser parser) throws XmlPullParserException,IOException
	{
		ArrayList<Event> events = new ArrayList<Event>();
        int eventType = parser.getEventType();
        Event currentEvent = null;
        String storyId = null;
        String userId = null;

        //Gen.appendLog("XmlParser::parseEvents> Private Starting");
        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            //Gen.appendLog("XmlParser::parseEvents> Reading new node = " + eventType);
            switch (eventType){
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    //Gen.appendLog("XmlParser::parseEvents> START_TAG = " + name);
                    if (name.equals("uid"))
                    	userId = parser.getAttributeValue(null, "id");
                    else if (name.equals("story"))
                    	storyId = parser.getAttributeValue(null, "id");
                    else if (name.equals("event")) {
                    	//Gen.appendLog("XmlParser::parseEvents> Adding new event 1");
                    	currentEvent = readEvent(parser);
                    	currentEvent.setUserId(userId);
                    	currentEvent.setStoryId(storyId);
                    	events.add(currentEvent);
                    }
                    break;
                /*case XmlPullParser.END_TAG:
                    name = parser.getName();
                    //Gen.appendLog("XmlParser::parseEvents> END_TAG = " + name);
                    if (name.equalsIgnoreCase("event") && currentEvent != null) {
                    	Gen.appendLog("XmlParser::parseEvents> Adding new event 2");
                    	currentEvent.setUserId(userId);
                    	currentEvent.setStoryId(storyId);
                    	currentEvent.print();
                    	events.add(currentEvent);
                    } */
            }
            eventType = parser.next();
        }
        return events;
	}
    
    public List<Story> parseStories(Reader in) throws XmlPullParserException, IOException {
    	//Gen.appendLog("XmlParser::parseStories> Starting");
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in);
            return parseStories(parser);
        } finally {
            in.close();
        }
    }
    private List<Story> parseStories(XmlPullParser parser) throws XmlPullParserException,IOException
	{
    	int eventType = parser.getEventType();
    	ArrayList<Story> stories = new ArrayList<Story>();
    	ArrayList<Event> events = new ArrayList<Event>();
    	Story currentStory = null;
        Event currentEvent = null;
        String storyId = null;
        String userId = null;

        //Gen.appendLog("XmlParser::parseStories> Private Starting");
        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            //Gen.appendLog("XmlParser::parseStories> Reading new node = " + eventType);
            switch (eventType){
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    //Gen.appendLog("XmlParser::parseStories> START_TAG = " + name);
                    if (name.equals("uid"))
                    	userId = parser.getAttributeValue(null, "id");
                    else if (name.equals("story")) {
                    	currentStory = new Story();
                    	events = new ArrayList<Event>();
                    	storyId = parser.getAttributeValue(null, "id");
                    	currentStory.setStoryId(storyId);
                    } else if (name.equals("title") && currentStory != null)
                    	currentStory.setTitle(Uri.decode(parser.nextText()));
                    else if (name.equals("event")) {
                    	//Gen.appendLog("XmlParser::parseStories> Adding new event 1");
                    	currentEvent = readEvent(parser);
                    	currentEvent.setUserId(userId);
                		currentEvent.setStoryId(storyId);
                		events.add(currentEvent);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    //Gen.appendLog("XmlParser::parseStories> END_TAG = " + name);
                    /*if (name.equalsIgnoreCase("event") && currentEvent != null) {
                    	Gen.appendLog("XmlParser::parseStories> Adding new event 2");
                    	currentEvent.setUserId(userId);
                    	currentEvent.setStoryId(storyId);
                    	events.add(currentEvent);
                    }
                    else*/ if (name.equalsIgnoreCase("story") && currentStory != null) {
                    	//Gen.appendLog("XmlParser::parseStories> Adding new story");
                    	currentStory.setUserId(userId);
                    	currentStory.setEvents(events);
                    	stories.add(currentStory);
                    }
            }
            eventType = parser.next();
        }
        return stories;
	}
    
    private Event readEvent(XmlPullParser parser) throws IOException, XmlPullParserException {
    	Event event = null;
    	String name = null;
    	int eventType = parser.getEventType();
    	
    	//Gen.appendLog("XmlParser::readEvent> Starting");
    	while (eventType != XmlPullParser.END_TAG) {
    		name = parser.getName();
    		//Gen.appendLog("XmlParser::readEvent> Name = " + name);
    		//Gen.appendLog("XmlParser::readEvent> EventType = " + eventType);
    		//Gen.appendLog("XmlParser::readEvent> Text = " + parser.getText());
    		if (parser.getEventType() != XmlPullParser.START_TAG) {
    			eventType = parser.next();
    			continue;
            } else if (name.equals("event")){
    			//Gen.appendLog("XmlParser::readEvent> Here 1");
    			event = new Event();
    			event.setEventId(parser.getAttributeValue(null, "id"));
    			//Gen.appendLog("XmlParser::readEvent> Event#" + event.getEventId());
            } else if (event != null){
            	//Gen.appendLog("XmlParser::readEvent> Here 2");
                if (name.equals("comment")){
                	event.setComment(Uri.decode(parser.nextText()));
                } else if (name.equals("rating")){
                	event.setRating(readInt(parser));
                } else if (name.equals("category")){
                	event.setCategoryId(readIntIdAtt(parser));
                	parser.nextText();
                } else if (name.equals("mediapath")){
                	event = readMediaPath(parser, event);
                }
            }
    		eventType = parser.next();
    	}
    	
    	//Gen.appendLog("XmlParser::readEvent> Ending");
    	return event;
    }
    
    private Event readMediaPath(XmlPullParser parser, Event event) throws IOException, XmlPullParserException {
    	String name = null;
    	int eventType = parser.getEventType();
    	
    	//Gen.appendLog("XmlParser::readMediaPath> Starting");
    	while (eventType != XmlPullParser.END_TAG) {
    		name = parser.getName();
    		//Gen.appendLog("XmlParser::readMediaPath> Name = " + name);
    		//Gen.appendLog("XmlParser::readMediaPath> EventType = " + eventType);
    		//Gen.appendLog("XmlParser::readMediaPath> Text = " + parser.getText());
    		if (parser.getEventType() != XmlPullParser.START_TAG) {
    			eventType = parser.next();
    			continue;
            } else if (event != null){
                if (name.equals("thumb")){
                	event.setThumbMediaPath(parser.nextText());
                } else if (name.equals("resized")){
                	event.setResizedMediaPath(parser.nextText());
                } else if (name.equals("original")){
                	event.setOriginalMediaPath(parser.nextText());
                }
            }
    		eventType = parser.next();
    	}
    	
    	//Gen.appendLog("XmlParser::readMediaPath> Ending");
    	return event;
    }
    
	private int readIntIdAtt(XmlPullParser parser) throws IOException, XmlPullParserException {
		//Gen.appendLog("XmlParser::readIdAtt> Starting");
		try {
			return Integer.valueOf(parser.getAttributeValue(null, "id"));
	    } catch (NumberFormatException e) {
	    	Gen.appendLog("XmlParser::readIdAtt> NumberFormatException : " + parser.getAttributeValue(null, "id"), "E");
	    	return -1;
	    }
	}

	private int readInt(XmlPullParser parser) throws IOException, XmlPullParserException {
		//Gen.appendLog("XmlParser::readInt> Starting");
	    try {
	    	return Integer.valueOf(parser.nextText());
	    } catch (NumberFormatException e) {
	    	Gen.appendLog("XmlParser::readInt> NumberFormatException : " + parser.nextText(), "E");
	    	return -1;
	    }
	}
}

package com.mjumel.mystories.tools;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.mjumel.mystories.Event;

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
		ArrayList<Event> events = null;
        int eventType = parser.getEventType();
        Event currentEvent = null;
        String storyId = null;
        String userId = null;

        //Gen.appendLog("XmlParser::parseEvents> Private Starting");
        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            //Gen.appendLog("XmlParser::parseEvents> Reading new node = " + eventType);
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                	events = new ArrayList<Event>();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    //Gen.appendLog("XmlParser::parseEvents> START_TAG = " + name);
                    if (name.equals("uid"))
                    	userId = parser.getAttributeValue(null, "id");
                    else if (name.equals("story"))
                    	storyId = parser.getAttributeValue(null, "id");
                    else if (name.equals("event")){
                    	//Gen.appendLog("XmlParser::parseEvents> Creating new event");
                    	currentEvent = new Event();
                    	currentEvent.setEventId(parser.getAttributeValue(null, "id"));
                    } else if (currentEvent != null){
                        if (name.equals("comment")){
                        	currentEvent.setComment(parser.nextText());
                        } else if (name.equals("rating")){
                        	currentEvent.setRating(readInt(parser));
                        } else if (name.equals("category")){
                        	currentEvent.setCategoryId(readIntIdAtt(parser));
                        } else if (name.equals("thumb")){
                        	currentEvent.setThumbMediaPath(parser.nextText());
                        } else if (name.equals("resized")){
                        	currentEvent.setResizedMediaPath(parser.nextText());
                        } else if (name.equals("original")){
                        	currentEvent.setOriginalMediaPath(parser.nextText());
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    //Gen.appendLog("XmlParser::parseEvents> END_TAG = " + name);
                    if (name.equalsIgnoreCase("event") && currentEvent != null) {
                    	//Gen.appendLog("XmlParser::parseEvents> Adding new event");
                    	currentEvent.setUserId(userId);
                    	currentEvent.setStoryId(storyId);
                    	events.add(currentEvent);
                    } 
            }
            eventType = parser.next();
        }
        return events;
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

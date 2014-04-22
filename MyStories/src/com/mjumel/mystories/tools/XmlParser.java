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
    
    private static final String ns = null;
   
    public List<Event> parseEvents(Reader in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in);
            parser.nextTag();
            return readRoot(parser, "feed", "event");
        } finally {
            in.close();
        }
    }

    private List<Event> readRoot(XmlPullParser parser, String rootTag, String tag) throws XmlPullParserException, IOException {
        List<Event> events = new ArrayList<Event>();
        Event eventTmp = new Event();
        int storyId = -1;
        
        parser.require(XmlPullParser.START_TAG, ns, rootTag);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(tag)) {
            	eventTmp = readEvent(parser, tag);
            	eventTmp.SetStoryId(storyId);
            	events.add(eventTmp);
            } else if (name.equals("story")) {
            	storyId = readIdAtt(parser, "story");
            } else {
                skip(parser);
            }
        }
        return events;
    }
    
	private Event readEvent(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, tag);
		int eventId = -1;
		String comment = null;
		int rating = -1;
		int category = -1;
		String[] mediaPath = null;
		int uId = -1;
		int storyId = -1;
		
		eventId = readIdAtt(parser, tag);
		
		while (parser.next() != XmlPullParser.END_TAG) {
		    if (parser.getEventType() != XmlPullParser.START_TAG) {
		        continue;
		    }
		    String name = parser.getName();
		    if (name.equals("comment")) {
		    	comment = readText(parser, "comment");
			} else if (name.equals("rating")) {
				rating = readInt(parser, "rating");
			} else if (name.equals("category")) {
				category = readIdAtt(parser, "category");
			} else if (name.equals("mediapath")) {
				mediaPath = readMedias(parser);
			} else {
			    skip(parser);
			}
		 }
		 return new Event(comment, rating, category, mediaPath, uId, storyId, eventId);
	}
	
	private String[] readMedias(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "mediapath");
		String[] medias = new String[2];
		
		while (parser.next() != XmlPullParser.END_TAG) {
		    if (parser.getEventType() != XmlPullParser.START_TAG) {
		        continue;
		    }
		    String name = parser.getName();
		    if (name.equals("thumb")) {
		    	medias[0] = readText(parser, "thumb");
			} else if (name.equals("resized")) {
				medias[1] = readText(parser, "resized");
			} else if (name.equals("original")) {
				medias[2] = readText(parser, "original");
			} else {
			    skip(parser);
			}
		 }
		 return medias;
	}
	
	private int readIdAtt(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
		String idAtt = null;
		try {
	    	parser.require(XmlPullParser.START_TAG, ns, tag);
		    idAtt = parser.getAttributeValue(null, "id"); 
		    parser.nextTag();
		    parser.require(XmlPullParser.END_TAG, ns, tag);
	    	return Integer.valueOf(idAtt);
	    } catch (NumberFormatException e) {
	    	Gen.writeLog("XmlParser::readIdAtt> NumberFormatException : " + idAtt);
	    	return -1;
	    }
	}

	private String readText(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
		String result = "";
		parser.require(XmlPullParser.START_TAG, ns, tag);
		if (parser.next() == XmlPullParser.TEXT) {
		    result = parser.getText();
		    parser.nextTag();
		}
		parser.require(XmlPullParser.END_TAG, ns, tag);
		return result;
	}
	
	private int readInt(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
		int result = -1;
		parser.require(XmlPullParser.START_TAG, ns, tag);
		if (parser.next() == XmlPullParser.TEXT) {
		    try {
		    	result = Integer.valueOf(parser.getText());
		    } catch (NumberFormatException e) {
		    	Gen.writeLog("XmlParser::readInt> NumberFormatException : " + parser.getText());
		    }
		    parser.nextTag();
		}
		parser.require(XmlPullParser.END_TAG, ns, tag);
		return result;
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	}
}

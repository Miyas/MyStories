package com.mjumel.mystories.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.mjumel.mystories.Contact;

public class Contacts {
	
	static class ContactReg {
		String msId;
		String regId;
		
		ContactReg(String msId, String regId) {
			this.msId = msId;
			this.regId = regId;
		}
	}
	
	public static List<Contact> getContacts(Context context)
	{
		List<Contact> c = new ArrayList<Contact>();
		
		//
	    //  Find contacts with a phone number.
	    //
	    ContentResolver cr = context.getContentResolver();
	    Cursor contacts = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
	        "HAS_PHONE_NUMBER = 1", null, "DISPLAY_NAME ASC");
	    while (contacts.moveToNext()) {
	
	        String contactId = contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts._ID));
	        Contact contact = new Contact(contactId);
	        contact.setName(contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
	        
	        //
	        //  Get all phone numbers.
	        //
	        Cursor phones = cr.query(Phone.CONTENT_URI, null,
	            Phone.CONTACT_ID + " = " + contactId, null, null);
	        while (phones.moveToNext()) {
	            String number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
	            //int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
	            //String type = context.getString(Phone.getTypeLabelResource(phones.getInt(phones.getColumnIndex(Phone.TYPE))));
	            contact.addPhoneNumber(number);
	        }
	        phones.close();
	        //
	        //  Get all email addresses.
	        //
	        Cursor emails = cr.query(Email.CONTENT_URI, null,
	            Email.CONTACT_ID + " = " + contactId, null, null);
	        while (emails.moveToNext()) {
	            String email = emails.getString(emails.getColumnIndex(Email.DATA));
	            //int type = emails.getInt(emails.getColumnIndex(Email.TYPE));
	            //String type = context.getString(Email.getTypeLabelResource(emails.getInt(emails.getColumnIndex(Email.TYPE))));
	            contact.addMail(email);
	        }
	        emails.close();
	        
	        //contact.print();
	        c.add(contact);
		}
	    contacts.close();
	    
	    return c;
	}
	
	public static List<Contact> updateRegIds(List<Contact> contacts, HashMap<String, String> regs)
	{
		for (Contact c : contacts) {
			if (regs.get(c.getId()) != null) {
				c.setRegId(regs.get(c.getId()));
			}
		}
		return sort(contacts);
	}
	
	
	
	public static List<Contact> updateRegIds(List<Contact> contacts, JSONArray regs)
	{
		HashMap<String, ContactReg> map = new HashMap<String, ContactReg>();
		String ctcpid, ctcmid, regid;
		
		for (int cpt = 0; cpt < regs.length() ; cpt++) {
			try {
				JSONObject obj = regs.getJSONObject(cpt);
				ctcpid = obj.getString("pid");
				ctcmid = obj.getString("mid");
				regid = obj.getString("rid");
				map.put(ctcpid, new ContactReg(ctcmid, regid));
			} catch (JSONException e) {
				Gen.appendError("Contacts::updateRegIds> ", e);
				Gen.appendError("Contacts::updateRegIds> Json = " + regs.toString());
			}
		}
		
		for (Contact c : contacts) {
			ContactReg reg = map.get(c.getId());
			if (reg != null) {
				c.setRegId(reg.regId);
				c.setMyStoriesId(reg.msId);
			}
		}
		return sort(contacts);
	}
	
	private static List<Contact> sort(List<Contact> contacts)
	{
		Contact cCache, cTmp;
		
		contacts.add(0, new Contact("My Stories friends", true, true));
		contacts.add(1, new Contact("Other friends", true, false));
		for (int i = 0; i < contacts.size(); i++) {
			cCache = contacts.get(i);
			for (int j = i+1; j < contacts.size(); j++) {
				cTmp = contacts.get(j);
				if (compare(cTmp, cCache) < 0) {
					contacts.set(i, cTmp);
					contacts.set(j, cCache);
					cCache = contacts.get(i);
				}
			}
		}
		
		return contacts;
	}
	
	private static int compare(Contact c1, Contact c2)
	{
		String s1 = (c1.getRegId()!=null?0:1) + (c1.isSection()?0:1) + c1.getName();
		String s2 = (c2.getRegId()!=null?0:1) + (c2.isSection()?0:1) + c2.getName();
		if (s1.compareToIgnoreCase(s2) < 0)
			return -1;
		else if (s1.compareToIgnoreCase(s2) == 0)
			return 0;
		else
			return 1;
	}
	
	
}

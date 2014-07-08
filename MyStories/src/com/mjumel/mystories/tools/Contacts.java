package com.mjumel.mystories.tools;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.mjumel.mystories.Contact;

public class Contacts {
	
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
}

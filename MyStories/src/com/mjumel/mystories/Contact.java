package com.mjumel.mystories;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.mjumel.mystories.tools.Gen;

public class Contact implements Parcelable {
	
	private String id = null;
	private String msId = null;
	private String regId = null;
	private String name = null;
	private String firstName = null;
	private String lastName = null;
	private HashSet<String> phones = new HashSet<String>();
	private HashSet<String> mails = new HashSet<String>();
	private boolean isSection = false;
	private boolean sendNotifThroughMail = true; //if false, sms will be used
	
	// Display options
	private boolean isSelected = false;
	
	class Phone {
		String number;
		int type;
		String sType;
	}
	
	class Mail {
		String mail;
		int type;
		String sType;
	}

	public Contact() {
		//Gen.appendLog("Contact::Contact> Creating empty Contact");
		//this.phones = new ArrayList<Phone>();
		//this.mails = new ArrayList<Mail>();
    }
	
	@SuppressWarnings("unchecked")
	public Contact(Parcel in) {
		//Gen.appendLog("Contact::Contact> Creating new Contact with constructor#2");
		this.id = in.readString();
		this.msId = in.readString();
		this.regId = in.readString();
		this.name = in.readString();
		this.phones = (HashSet<String>) in.readSerializable();
		this.mails = (HashSet<String>) in.readSerializable();
		this.isSection = in.readInt()==1?true:false;
		this.sendNotifThroughMail = in.readInt()==1?true:false;
	}
	
	public Contact(String id, String name, List<Phone> phones, List<Mail> mails) 
    {
		//Gen.appendLog("Contact::Contact> Creating new Contact with constructor#3");
		this.id = id;
		this.name = name;
		//this.phones = new ArrayList<Phone>();
		//this.phones.addAll(phones);
		//this.mails = new ArrayList<Mail>();
		//this.mails.addAll(mails);
		//print();
    }
	
	public Contact(String id) 
    {
		//Gen.appendLog("Contact::Contact> Creating new Contact with constructor#4");
		this.id = id;
		//print();
    }
	
	public Contact(String name, boolean isSection, boolean hasRegId) 
    {
		//Gen.appendLog("Contact::Contact> Creating new Contact with constructor#4");
		this.name = name;
		this.isSection = isSection;
		this.regId = (hasRegId?"":null);
		//print();
    }
	
	public void print()
	{
		Gen.appendLog("Contact::print> id = " + id);
		Gen.appendLog("Contact::print> regId = " + regId);
		Gen.appendLog("Contact::print> name = " + name);
		for (String p : phones)
			Gen.appendLog("Contact::print> phone# " + p);
		for (String m : mails)
			Gen.appendLog("Contact::print> mail# " + m);
	}
	
	public String getId() { return id; }
	
	public String getMyStoriesId() { return msId; }
    public void setMyStoriesId(String value) { msId = value; }
	
	public String getRegId() { return regId; }
    public void setRegId(String value) { regId = value; }
    
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    
    public HashSet<String> getPhoneNumbers() { 
    	return phones;
    }
    public String getFirstPhoneNumber() {
    	Iterator<String> it = phones.iterator();
    	if (it.hasNext())
    		return it.next();
    	else
    		return null;
    }
    public void addPhoneNumber(String number) {
    	if (number != null)
    		phones.add(number); 
    }
    
    public HashSet<String> getMails() { 
    	return mails;
    }
    public String getFirstMail() {
    	Iterator<String> it = mails.iterator();
    	if (it.hasNext())
    		return it.next();
    	else
    		return null;
    }
    public void addMail(String mail) {
    	if (mail != null)
    		mails.add(mail);
    }
    
    public String getFirstName() {return firstName; }
    public void setFirstName(String value) {firstName = value; }
    
    public String getLastName() {return lastName; }
    public void setLastName(String value) {lastName = value; }
    
    public boolean isSelected() {return isSelected; }
    public void setSelected(boolean value) {isSelected = value; }
    
    public boolean isSection() {return isSection; }
    public void setSection(boolean value) {isSection = value; }
    
    public boolean getSendNotifThroughMail() {return sendNotifThroughMail; }
    public void setSendNotifThroughMail(boolean value) {sendNotifThroughMail = value; }
    
    @Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(msId);
		dest.writeString(regId);
		dest.writeString(name);
		dest.writeSerializable(phones);
		dest.writeSerializable(mails);
		dest.writeInt(this.isSection?1:0);
		dest.writeInt(this.sendNotifThroughMail?1:0);
	}
	
	public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>()
	{
	    @Override
	    public Contact createFromParcel(Parcel source) {
	        return new Contact(source);
	    }

	    @Override
	    public Contact[] newArray(int size) {
	    	return new Contact[size];
	    }
	};
}

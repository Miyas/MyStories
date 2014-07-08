package com.mjumel.mystories;

import java.util.HashSet;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.mjumel.mystories.tools.Gen;

public class Contact implements Parcelable {
	
	private String id = null;
	private String regId = null;
	private String name = null;
	private HashSet<String> phones = new HashSet<String>();
	private HashSet<String> mails = new HashSet<String>();
	
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
		this.regId = in.readString();
		this.name = in.readString();
		this.phones = (HashSet<String>) in.readSerializable();
		this.mails = (HashSet<String>) in.readSerializable();
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
		//this.phones = new ArrayList<Phone>();
		//this.mails = new ArrayList<Mail>();
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
	
	public String getRegId() { return regId; }
    public void setRegId(String value) { regId = value; }
    
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    
    public HashSet<String> getPhoneNumbers() { 
    	return phones;
    }
    public void addPhoneNumber(String number) {
    	if (number != null)
    		phones.add(number); 
    }
    
    public HashSet<String> getMails() { 
    	return mails;
    }
    public void addMail(String mail) {
    	if (mail != null)
    		mails.add(mail);
    }
    
    public boolean isSelected() {return isSelected; }
    public void setSelected(boolean value) {isSelected = value; }
    
    @Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(regId);
		dest.writeString(name);
		dest.writeSerializable(phones);
		dest.writeSerializable(mails);
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

package com.mjumel.mystories.adapters;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.mjumel.mystories.Contact;
import com.mjumel.mystories.R;
import com.mjumel.mystories.StoryShareFragment;

public class StoryShareAdapter extends ArrayAdapter<Contact> {
	private LayoutInflater inflater;
	private List<Contact> contactList;
	private StoryShareFragment fragment;
	
	private int checkedCount = 0;
	
	static class ViewHolder {
		CheckBox cb;
		Switch sw;
		TextView tv;
		int pos;
		boolean isSection;
	}

	public StoryShareAdapter(Activity context, StoryShareFragment fragment, List<Contact> contacts) {
		super(context, R.layout.dialog_link_events_item, contacts);
		this.inflater = LayoutInflater.from(context);
		this.fragment = fragment;
		this.contactList = contacts;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder holder;
		
		Contact contact = this.getItem(position);
		
		if ( rowView == null || ((ViewHolder) rowView.getTag()).isSection != contact.isSection() ) {
			holder = new ViewHolder();
			holder.pos = position;
			holder.isSection = contact.isSection();
			if (contact.isSection()) {
				rowView = inflater.inflate(R.layout.fragment_share_story_section, null);
	            holder.tv = (TextView) rowView.findViewById(R.id.share_story_section_name);
	            holder.cb = null;
	            holder.sw = null;
			} else {
				rowView = inflater.inflate(R.layout.fragment_share_story_item, null);
	            holder.cb = (CheckBox) rowView.findViewById(R.id.share_story_item_name);
	            holder.sw = (Switch) rowView.findViewById(R.id.share_story_item_switch);
	            holder.tv = null;
			}
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
		
		if (contact.isSection()) {
			holder.tv.setTag(position);
			holder.tv.setText(contact.getName());
		} else {
			
			// Switch management
			if (contact.getRegId() != null)
				holder.sw.setVisibility(Switch.GONE);
			else {
				holder.sw.setTag(position);
				holder.sw.setVisibility(Switch.VISIBLE);
				holder.sw.setChecked(!contact.getSendNotifThroughMail());
				holder.sw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton v, boolean isChecked) {
						Contact c = contactList.get((Integer)v.getTag());
						c.setSendNotifThroughMail(!isChecked);
						notifyDataSetChanged();
					}
				});
			}
			
			// Checkbox management
			holder.cb.setTag(position);
			holder.cb.setText(contact.getName());
			holder.cb.setChecked(contact.isSelected());
			holder.cb.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	Contact c = contactList.get((Integer)v.getTag());
	            	c.setSelected(!c.isSelected());
	            	fragment.setNbContactsChecked(setCount(c));
	            	notifyDataSetChanged();
	            }
	        });
		}
		
		return rowView;
	}
	
    private int setCount(Contact c) {
        if (c.isSelected()) {
            checkedCount++;
        } else {
            if (checkedCount != 0) {
                checkedCount--;
            }
        }
        return checkedCount;
    }
}

package com.mjumel.mystories.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.mjumel.mystories.R;
import com.mjumel.mystories.StoryShareFragment;
import com.mjumel.mystories.tools.Gen;

public class StoryShareAdapter extends SimpleCursorAdapter {
	private LayoutInflater inflater;
	private Context context;
	private StoryShareFragment fragment;
	private Cursor cursor;
	private String[] from;
	private int[] to;
	private int flags;
	private List<Boolean> checked;
	
	private int nameIdx;
	
    int checkedCount = 0;
    
    static class ViewHolder {
		CheckBox cb;
	}
    
	public StoryShareAdapter(Context context, StoryShareFragment fragment, Cursor cursor, String[] from, int[] to, int flags) {
		super(context, R.layout.fragment_share_story_item, cursor, from, to, flags);
		this.inflater = LayoutInflater.from(context);
		this.context = context;
		this.fragment = fragment;
		this.cursor = cursor;
		this.from = from;
		this.to = to;
		this.flags = flags;
		
		Gen.appendLog("StoryShareAdapter::getView> Cursor size#"+cursor.getCount());
		Gen.appendLog("StoryShareAdapter::getView> Cursor ColumnCount#"+cursor.getColumnCount());
		Gen.appendLog("StoryShareAdapter::getView> From[0]: "+ from[0]);
		for(int i = 0;i<cursor.getColumnCount();i++)
			Gen.appendLog("StoryShareAdapter::getView> Column["+i+"]: "+ cursor.getColumnName(i));
		nameIdx = cursor.getColumnIndexOrThrow(from[0]);
		checked = new ArrayList<Boolean>();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder holder;
		
		Gen.appendLog("StoryShareAdapter::getView> Starting event#"+position);
		expandList(position);
		
		if ( rowView == null ) {
			rowView = inflater.inflate(R.layout.dialog_link_events_item, null);
			holder = new ViewHolder();
            holder.cb = (CheckBox) rowView.findViewById(R.id.dialog_link_events_cb);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
		
		holder.cb.setTag(position);
		holder.cb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	CheckBox cb = (CheckBox)v;
            	checked.set((Integer)cb.getTag(), cb.isChecked());
            	checkedCount = cb.isChecked() ? checkedCount + 1 : checkedCount - 1;
            	fragment.updateMenu(checkedCount > 0);
            }
        });
		holder.cb.setChecked(checked.get(position));
		
		Cursor c = (Cursor) getItem(position);
		holder.cb.setText(c.getString(nameIdx));
		
		//bindView(rowView, context, cursor);
		return rowView;
	}

    /*@Override
    public void bindView(View view, Context context, Cursor cursor) {
    	ViewHolder holder  =   (ViewHolder)    view.getTag();
    	if (cursor != null)
    		holder.cb.setText(cursor.getString(cursor.getColumnIndexOrThrow(from[0])));
        
        //((CheckBox)view).setText(cursor.getString(cursor.getColumnIndexOrThrow(from[0])));        
    }*/
    
    private void expandList(int pos) {
    	Gen.appendLog("StoryShareAdapter::expandList> pos:" + pos + " / size:" + checked.size());
    	if (pos >= checked.size()) {
    		for (int i = checked.size() ; i <= pos ; i++) {
    			Gen.appendLog("StoryShareAdapter::expandList> Adding pos#" + i);
    			checked.add(false);
    		}
    	}
    }
}

package com.mjumel.mystories;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mjumel.mystories.tools.Gen;

public class GenFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("GenFragment::onCreateView> Starting");
    	
		
		View view = inflater.inflate(R.layout.fragment_gen,container, false);
		TextView text = (TextView) view.findViewById(R.id.gen_textView);
		
		Bundle args = getArguments();
	    if (args != null)
	    	text.setText(args.getString("KEY_STRING"));
	    else
	    	text.setText(getActivity().getActionBar().getTitle());
		
		Gen.appendLog("GenFragment::onCreateView> Ending");
		return view;
    }
    
    public static GenFragment newInstance(String chaine) {
    	GenFragment fragment = new GenFragment();
    	Bundle args = new Bundle();
    	args.putString("KEY_STRING", chaine);
    	fragment.setArguments(args);
    	return fragment;
    }
}

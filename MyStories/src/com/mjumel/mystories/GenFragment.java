package com.mjumel.mystories;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mjumel.mystories.tools.Gen;

public class GenFragment extends Fragment {

    public GenFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    	
    	Gen.appendLog("GenFragment::onCreateView> Starting");
    	
		
		View view = inflater.inflate(R.layout.fragment_gen,container, false);
		TextView text = (TextView) view.findViewById(R.id.gen_textView);
		text.setText(getActivity().getActionBar().getTitle());
		
		Gen.appendLog("GenFragment::onCreateView> Ending");
		return view;
    }
}

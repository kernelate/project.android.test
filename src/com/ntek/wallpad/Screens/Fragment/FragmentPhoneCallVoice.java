package com.ntek.wallpad.Screens.Fragment;

import com.ntek.wallpad.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentPhoneCallVoice extends Fragment{

	private View view;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		view = inflater.inflate(R.layout.fragment_call_voice, container, false);
		
		return view;
	}

}

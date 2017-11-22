package com.example.heavymentaldelection.fragment;

import com.example.heavymentaldelection.R;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MyDialogFragment extends DialogFragment {
	private View dialogview;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		dialogview = inflater.inflate(R.layout.dialog_set_accumation_time, container, false);
		return dialogview;
	}
}

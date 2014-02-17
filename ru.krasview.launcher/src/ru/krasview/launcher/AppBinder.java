package ru.krasview.launcher;

import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

 class AppBinder implements SimpleAdapter.ViewBinder {
	 
	 int mWidth;
	 int mMargin;
	 float textSize;
	 

	public boolean setViewValue(View view, Object data,
			String textRepresentation) {

		switch(view.getId()){
		case R.id.text1:
			((TextView)view).setText((String)data);
			return true;
		case R.id.text2:
			((TextView)view).setText("Версия " + data);
			return true;
		}
		
		return false;
	}

  }
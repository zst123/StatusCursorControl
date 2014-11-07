/*
 * Copyright (C) 2013/2014 zst123
 * 
 * StatusCursorControl and XHaloFloatingWindow 
 * are free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.zst.xposed.statuscursorcontrol.preference;

import com.zst.xposed.statuscursorcontrol.R;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

public class WidgetNumberPicker extends DialogPreference {
	
	private NumberPicker picker;
	int mDefaultValue;
	int mMinValue;
	int mMaxValue;
	SharedPreferences mPref;
	
	public WidgetNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.dialog_number_picker);
		mDefaultValue = (Integer.parseInt(attrs.getAttributeValue(null, "defaultValue")));
		mMinValue = (Integer.parseInt(attrs.getAttributeValue(null, "minimum")));
		mMaxValue = (Integer.parseInt(attrs.getAttributeValue(null, "maximum")));
	}
	
	@Override
	protected View onCreateDialogView() {
		View view = super.onCreateDialogView();
		mPref = getPreferenceManager().getSharedPreferences();
		picker = (NumberPicker) view.findViewById(R.id.number_picker);
		picker.setMaxValue(mMaxValue);
		picker.setMinValue(mMinValue);
		
		return view;
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		picker.setValue(mPref.getInt(getKey(), mDefaultValue));
	}
	
	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		getDialog().setTitle(getTitle());
		((TextView) getDialog().findViewById(android.R.id.hint)).setText(getSummary());
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			mPref.edit().putInt(getKey(), picker.getValue()).commit();
		}
	}
}
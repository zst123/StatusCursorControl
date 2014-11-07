/*
 * Copyright (C) 2014 zst123
 * 
 * StatusCursorControl is free software: you can redistribute it and/or modify
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

/*
 * Copyright (C) 2013 Peter Gregus (xgravitybox@gmail.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * You may not distribute nor sell this software or parts of it in
 * Source, Object nor in any other form without explicit permission obtained
 * from the original author.
 */

package com.zst.xposed.statuscursorcontrol.hooks;

import com.zst.xposed.statuscursorcontrol.Common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.MotionEvent;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;

public class StatusBarHook {
	XSharedPreferences mPref;
	float mSensitivity;
	boolean mBlockBrightnessControl;
	
	Object mPhoneStatusBar;
	Context mContext;
	boolean mIMEShowing;
	float mScreenWidth;

	
	public StatusBarHook(ClassLoader classLoader) {
		final Class<?> clazz = XposedHelpers.findClass(
				"com.android.systemui.statusbar.phone.PhoneStatusBar", classLoader);
		
		XposedHelpers.findAndHookMethod(clazz, "makeStatusBarView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				mPhoneStatusBar = param.thisObject;
				mContext = (Context) XposedHelpers.getObjectField(mPhoneStatusBar, "mContext");
				mScreenWidth = (float) mContext.getResources().getDisplayMetrics().widthPixels;
				
				updatePref(mContext);
				
				IntentFilter filter = new IntentFilter();
				filter.addAction(Common.ACTION_IME_SHOWN);
				filter.addAction(Common.ACTION_IME_HIDDEN);
				filter.addAction(Common.ACTION_SETTINGS_CHANGED);
				mContext.registerReceiver(new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						if (intent.getAction().equals(Common.ACTION_IME_SHOWN)) {
							mIMEShowing = true;
						} else if (intent.getAction().equals(Common.ACTION_IME_HIDDEN)) {
							mIMEShowing = false;
						} else if (intent.getAction().equals(Common.ACTION_SETTINGS_CHANGED)) {
							updatePref(mContext);
						}
					}
				}, filter);
			}
		});
		
		XposedHelpers.findAndHookMethod(clazz, "interceptTouchEvent", MotionEvent.class,
				new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if (mIMEShowing) {
					if (processTouchEvent((MotionEvent) param.args[0])
							&& mBlockBrightnessControl) {
						((MotionEvent) param.args[0]).setAction(MotionEvent.ACTION_UP);
					}
				}
			}
			
		});
	}
	private void updatePref(Context c) {
		if (mPref == null) {
			mPref = new XSharedPreferences(Common.THIS_PACKAGE);
		} else {
			mPref.reload();
		}
		mSensitivity = Common.convertToDp(c,
				mPref.getInt(Common.Pref.Key.SLIDE_SENSITIVITY, Common.Pref.Def.SLIDE_SENSITIVITY));
		mBlockBrightnessControl = mPref.getBoolean(Common.Pref.Key.BLOCK_BRIGHTNESS_CONTROL,
				Common.Pref.Def.BLOCK_BRIGHTNESS_CONTROL);
	}
	
	private boolean mJustPeeked;
	private int mPeekHeight = -1;
	private int mInitialTouchX = -1;
	//private int mInitialTouchY = -1;
	
	/**
	 * @return true if need to intercept the view's touch events completely
	 */
	private boolean processTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final int x = (int) event.getRawX();
		final int y = (int) event.getRawY();
		final int notificationHeaderHeight = Build.VERSION.SDK_INT > 16 ?
				XposedHelpers.getIntField(mPhoneStatusBar, "mNotificationHeaderHeight") :
					XposedHelpers.getIntField(mPhoneStatusBar, "mNotificationPanelMinHeight");
				
				if (action == MotionEvent.ACTION_DOWN) {
					if (y < notificationHeaderHeight) {
						mInitialTouchX = x;
						// mInitialTouchY = y;
						mJustPeeked = true;
					}
					if (mPeekHeight == -1) {
						Resources res = mContext.getResources();
						mPeekHeight = Build.VERSION.SDK_INT > 16 ? res.getDimensionPixelSize(res
								.getIdentifier("peek_height", "dimen", "com.android.systemui")) : (int) TypedValue
								.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 84, res.getDisplayMetrics());
					}
				} else if (action == MotionEvent.ACTION_MOVE) {
					if (y < notificationHeaderHeight && mJustPeeked) {
						final int xDiff = x - mInitialTouchX;
						final int xDiffAbs = Math.abs(xDiff);
						// final int yDiff = Math.abs(y - mInitialTouchY);
						if (xDiffAbs > mSensitivity) { // Move is detected
							int units = (int) Math.floor(xDiffAbs / mSensitivity);
							if (units < 1) {
								units = 1;
							}
							if (xDiff < 0) {
								// negative --> moved left
								Intent intent = new Intent(Common.ACTION_IME_CURSOR_LEFT);
								intent.getIntExtra(Common.EXTRA_REPEAT, units);
								mContext.sendBroadcast(intent);
							} else if (xDiff >= 0) {
								// positive --> moved right
								Intent intent = new Intent(Common.ACTION_IME_CURSOR_RIGHT);
								intent.getIntExtra(Common.EXTRA_REPEAT, units);
								mContext.sendBroadcast(intent);
							}
							// Reset the values for calculation
							mInitialTouchX = x;
							// mInitialTouchY = y;
						}
						return true;
					} else {
						if (y > mPeekHeight) {
							mJustPeeked = false;
						}
					}
				}
				return false;
	}
	
}

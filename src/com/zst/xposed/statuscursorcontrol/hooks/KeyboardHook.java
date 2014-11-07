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

package com.zst.xposed.statuscursorcontrol.hooks;

import com.zst.xposed.statuscursorcontrol.Common;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class KeyboardHook {
	
	InputMethodService mKeyboard;
	Context mContext;
	
	public KeyboardHook() {
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Common.ACTION_IME_CURSOR_LEFT);
		filter.addAction(Common.ACTION_IME_CURSOR_RIGHT);
		final BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (mKeyboard == null) return;
				int repeatFactor = intent.getIntExtra(Common.EXTRA_REPEAT, 1);
				for (int x = 0; x < repeatFactor; x++) {
					if (intent.getAction().equals(Common.ACTION_IME_CURSOR_LEFT)) {
						mKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
					} else if (intent.getAction().equals(Common.ACTION_IME_CURSOR_RIGHT)) {
						mKeyboard.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
					}
				}
			}
		};
		
		XposedBridge.hookAllMethods(InputMethodService.class, "onWindowShown", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				mKeyboard = (InputMethodService) param.thisObject;
				mContext = mKeyboard.getApplicationContext();
				
				// Tell SystemUI process that we are shown
				mContext.sendBroadcast(new Intent(Common.ACTION_IME_SHOWN));
				
				// Start Receiver for left/right keyevents
				mContext.registerReceiver(receiver, filter);
			}
		});
		XposedBridge.hookAllMethods(InputMethodService.class, "onWindowHidden", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				mKeyboard = (InputMethodService) param.thisObject;
				mContext = mKeyboard.getApplicationContext();
				
				// Tell SystemUI process that we are hidden
				mContext.sendBroadcast(new Intent(Common.ACTION_IME_HIDDEN));
				
				// Stop Receiver for left/right keyevents
				mContext.unregisterReceiver(receiver);
			}
		});
	}
}

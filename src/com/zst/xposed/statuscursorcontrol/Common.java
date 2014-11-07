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
 * Copyright (C) 2013 XuiMod
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zst.xposed.statuscursorcontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class Common {
	
	public static final String THIS_PACKAGE = Common.class.getPackage().getName();
	
	public static final String ACTION_SETTINGS_CHANGED = THIS_PACKAGE + ".ACTION_SETTINGS_CHANGED";
	public static final String ACTION_IME_SHOWN = THIS_PACKAGE + ".ACTION_IME_SHOWN";
	public static final String ACTION_IME_HIDDEN = THIS_PACKAGE + ".ACTION_IME_HIDDEN";
	public static final String ACTION_IME_CURSOR_LEFT = THIS_PACKAGE + ".ACTION_IME_CURSOR_LEFT";
	public static final String ACTION_IME_CURSOR_RIGHT = THIS_PACKAGE + ".ACTION_IME_CURSOR_RIGHT";
	
	public static final String EXTRA_REPEAT = "repeat";
	
	public static final class Pref {
		public static final class Key {
			public static final String BLOCK_BRIGHTNESS_CONTROL = "block_brightness";
			public static final String SLIDE_SENSITIVITY = "slide_sensitivity";
		}
		public static final class Def {
			public static final boolean BLOCK_BRIGHTNESS_CONTROL = false;
			public static final int SLIDE_SENSITIVITY = 12;
		}
	}
	
	public static float convertToDp(Context c, int number) {
		float scale = c.getResources().getDisplayMetrics().density;
		return (number * scale + 0.5f);
	}
	
	public static void settingsChanged(final Context ctx) {
		final Handler handler = new Handler(ctx.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent i = new Intent(Common.ACTION_SETTINGS_CHANGED);
				ctx.sendBroadcast(i);
			}
		}, 1000);
		/* The 1 second delay is to give enough time for system to write the
		 * preferences. When the preference is read while it's being written,
		 * the hooks might retrieve the wrong value. */
	}
}

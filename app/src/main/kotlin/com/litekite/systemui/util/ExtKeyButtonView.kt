/*
 * Copyright 2020 LiteKite Startup. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.litekite.systemui.util

import android.app.ActivityManager
import android.view.KeyEvent
import android.view.View
import com.litekite.systemui.R
import com.litekite.systemui.widget.KeyButtonView

fun KeyButtonView.taskChanged(runningTaskInfo: ActivityManager.RunningTaskInfo) {
	val topActivity = runningTaskInfo.topActivity
	if (this.code == KeyEvent.KEYCODE_HOME) {
		// Highlights the home button when the launcher app in foreground
		isActivated = context.getString(R.string.pkg_launcher) == topActivity.packageName
				|| context.getString(R.string.component_launcher) == topActivity.flattenToShortString()
	} else if (code == KeyEvent.KEYCODE_BACK) {
		// Sets the visibility of back button based on the launcher app stack
		// Hidden when the launcher app was in the current stack
		// Otherwise shown.
		visibility = if (context.getString(R.string.component_launcher)
			== topActivity.flattenToShortString()
		) {
			View.GONE
		} else {
			View.VISIBLE
		}
	}
}
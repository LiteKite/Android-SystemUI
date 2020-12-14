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

package com.litekite.systemui.systembar.volumebar

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Vignesh S
 * @version 1.0, 14/12/2020
 * @since 1.0
 */
@Singleton
class VolumeBarWindowController @Inject constructor(private val context: Context) {

	private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
	private lateinit var lp: WindowManager.LayoutParams
	private lateinit var volumeBarView: View

	/**
	 * Adds the volume bar view to the window manager.
	 *
	 * @param volumeBarView The view to add.
	 */
	fun add(volumeBarView: View) {
		lp = WindowManager.LayoutParams(
			WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.TYPE_VOLUME_OVERLAY,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
					or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
					or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
			PixelFormat.TRANSLUCENT
		)
		lp.gravity = Gravity.BOTTOM
		lp.title = "VolumeBar"
		lp.packageName = context.packageName
		this.volumeBarView = volumeBarView
		windowManager.addView(this.volumeBarView, lp)
	}

	/**
	 * Removes the volume bar view from the window manager.
	 */
	fun remove() {
		windowManager.removeViewImmediate(volumeBarView)
	}

}
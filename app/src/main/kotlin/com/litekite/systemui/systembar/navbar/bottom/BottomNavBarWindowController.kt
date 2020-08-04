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

package com.litekite.systemui.systembar.navbar.bottom

import android.content.Context
import android.graphics.PixelFormat
import android.os.Binder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.litekite.systemui.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encapsulates all logic for the bottom navigation bar window state management.
 *
 * @author Vignesh S
 * @version 1.0, 02/08/2020
 * @since 1.0
 */
@Singleton
class BottomNavBarWindowController @Inject constructor(private val context: Context) {

	private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
	private lateinit var lp: WindowManager.LayoutParams
	private lateinit var bottomNavBarView: View
	private var barHeight = context.resources.getDimensionPixelSize(R.dimen.bottom_nav_bar_height)

	/**
	 * Adds the bottom navigation bar view to the window manager.
	 *
	 * @param bottomNavBarView The view to add.
	 */
	fun add(bottomNavBarView: View) {
		lp = WindowManager.LayoutParams(
			WindowManager.LayoutParams.MATCH_PARENT,
			barHeight,
			WindowManager.LayoutParams.TYPE_NAVIGATION_BAR,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					or WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
					or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
					or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
			PixelFormat.TRANSLUCENT
		)
		lp.token = Binder()
		lp.gravity = Gravity.BOTTOM
		lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
		lp.title = "BottomNavBar"
		lp.packageName = context.packageName
		this.bottomNavBarView = bottomNavBarView
		windowManager.addView(this.bottomNavBarView, lp)
	}

}
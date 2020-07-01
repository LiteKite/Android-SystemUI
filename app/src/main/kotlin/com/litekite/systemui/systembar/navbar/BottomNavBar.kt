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

package com.litekite.systemui.systembar.navbar

import android.view.View
import android.widget.FrameLayout
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI

/**
 * @author Vignesh S
 * @version 1.0, 01/07/2020
 * @since 1.0
 */
class BottomNavBar : SystemUI() {

	companion object {
		val TAG = BottomNavBar::class.java.simpleName
	}

	private lateinit var bottomNavBarView: View

	override fun start() {
		putComponent(BottomNavBar::class.java, this)
		makeBottomNavBar()
	}

	private fun makeBottomNavBar() {
		bottomNavBarView = View.inflate(context, R.layout.super_bottom_nav_bar, null)
				as FrameLayout
	}

}
/*
 * Copyright 2021 LiteKite Startup. All rights reserved.
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
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.litekite.systemui.systembar.volumebar.VolumeBar
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * @author Vignesh S
 * @version 1.0, 22/02/2021
 * @since 1.0
 */
class BottomNavBarWindow @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	companion object {
		val TAG = BottomNavBarWindow::class.java.simpleName
	}

	@EntryPoint
	@InstallIn(SingletonComponent::class)
	interface BottomNavBarWindowEntryPoint {

		fun getVolumeBar(): VolumeBar

	}

	private fun getEntryPointAccessor() = EntryPointAccessors.fromApplication(
		context,
		BottomNavBarWindowEntryPoint::class.java
	)

	override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		// Passes touch events to the volume bar component for the motion layout
		// to animate the volume bar based the swipe gesture
		if (ev?.action != MotionEvent.ACTION_OUTSIDE) {
			getEntryPointAccessor().getVolumeBar().getRootView().dispatchTouchEvent(ev)
		}
		return super.dispatchTouchEvent(ev)
	}

}
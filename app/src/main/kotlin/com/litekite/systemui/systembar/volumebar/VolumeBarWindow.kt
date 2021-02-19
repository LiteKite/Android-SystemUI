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

package com.litekite.systemui.systembar.volumebar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.constraintlayout.motion.widget.MotionLayout
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.databinding.SuperVolumeBarBinding

/**
 * @author Vignesh S
 * @version 1.0, 19/02/2021
 * @since 1.0
 */
class VolumeBarWindow @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	companion object {
		val TAG = VolumeBarWindow::class.java.simpleName

		private const val COLLAPSE_VOLUME_BAR_DELAY = 3000L
	}

	private var attached: Boolean = false
	private lateinit var superVolumeBarBinding: SuperVolumeBarBinding

	private val transitionListener = object : MotionLayout.TransitionListener {
		override fun onTransitionStarted(
			motionLayout: MotionLayout?,
			startId: Int,
			endId: Int
		) {
		}

		override fun onTransitionChange(
			motionLayout: MotionLayout?,
			startId: Int,
			endId: Int,
			progress: Float
		) {
		}

		override fun onTransitionCompleted(
			motionLayout: MotionLayout?,
			currentId: Int
		) {
			if (currentId == R.id.end) {
				handler.removeCallbacks(collapseVolumeBar)
				handler.postDelayed(collapseVolumeBar, COLLAPSE_VOLUME_BAR_DELAY)
			}
		}

		override fun onTransitionTrigger(
			motionLayout: MotionLayout?,
			startId: Int,
			endId: Boolean,
			progress: Float
		) {
		}
	}

	private val collapseVolumeBar = Runnable {
		superVolumeBarBinding.volumeBar.volumeBarContainer.transitionToStart()
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		SystemUI.printLog(TAG, "onAttachedToWindow:")
		if (!attached) {
			attached = true
			// volume bar view binding
			superVolumeBarBinding = SuperVolumeBarBinding.bind(this)
			// Listens for volume bar motion view transition changes and delays collapsing
			// volume bar if there is any pending animation.
			superVolumeBarBinding.volumeBar.volumeBarContainer.addTransitionListener(
				transitionListener
			)
		}
	}

	override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
		when (ev?.action) {
			MotionEvent.ACTION_DOWN,
			MotionEvent.ACTION_MOVE -> {
				handler.removeCallbacks(collapseVolumeBar)
			}
			MotionEvent.ACTION_UP,
			MotionEvent.ACTION_CANCEL -> {
				handler.postDelayed(collapseVolumeBar, COLLAPSE_VOLUME_BAR_DELAY)
			}
		}
		return super.onInterceptTouchEvent(ev)
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		// Watches outside touch events and closes the volume motion view by making the transition
		// back to its start position.
		if (event?.action == MotionEvent.ACTION_OUTSIDE) {
			superVolumeBarBinding.volumeBar.volumeBarContainer.transitionToStart()
		}
		return super.onTouchEvent(event)
	}

}
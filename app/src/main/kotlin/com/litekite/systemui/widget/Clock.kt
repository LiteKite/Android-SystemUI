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

package com.litekite.systemui.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.os.UserHandle
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.Display
import androidx.appcompat.widget.AppCompatTextView
import com.litekite.systemui.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Vignesh S
 * @version 1.0, 04/02/2020
 * @since 1.0
 */
class Clock @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

	private lateinit var calendar: Calendar
	private lateinit var sdfClock: SimpleDateFormat
	private var attached: Boolean = false
	private val showSeconds: Boolean

	init {
		val ta = context.obtainStyledAttributes(
			attrs,
			R.styleable.Clock,
			defStyleAttr,
			0
		)
		showSeconds = ta.getBoolean(R.styleable.Clock_showSeconds, false)
		ta.recycle()
	}

	private val receiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			when (intent?.action) {
				Intent.ACTION_TIMEZONE_CHANGED -> {
					handler.post {
						val tz = intent.getStringExtra("time-zone")
						calendar = Calendar.getInstance(TimeZone.getTimeZone(tz))
					}
				}
			}
			handler.post { updateClock() }
		}
	}

	private val secondsTickRunnable = Runnable {
		updateClock()
	}

	private val screenReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			when (intent?.action) {
				Intent.ACTION_SCREEN_OFF -> {
					handler.removeCallbacks(secondsTickRunnable)
				}
				Intent.ACTION_SCREEN_ON -> {
					handler.post { updateClock() }
				}
			}
		}
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		if (!attached) {
			attached = true
			val filter = IntentFilter()
			filter.addAction(Intent.ACTION_TIME_TICK)
			filter.addAction(Intent.ACTION_TIME_CHANGED)
			filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
			filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED)
			filter.addAction(Intent.ACTION_USER_SWITCHED)
			val handleThread = HandlerThread("TimeTick")
			val handler = Handler(handleThread.looper)
			context.registerReceiverAsUser(
				receiver,
				UserHandle.getUserHandleForUid(UserHandle.myUserId()),
				filter,
				null,
				handler
			)
			// Screen off, on receiver. register this only when showing seconds. because we need
			// to update seconds only when SCREEN is ON.
			if (showSeconds) {
				val screenFilter = IntentFilter()
				screenFilter.addAction(Intent.ACTION_SCREEN_OFF)
				screenFilter.addAction(Intent.ACTION_SCREEN_ON)
				context.registerReceiver(screenReceiver, screenFilter)
			}
		}
		// NOTE: It's safe to do these after registering the receiver since the receiver always runs
		// in the main thread, therefore the receiver can't run before this method returns.
		// The time zone may have changed while the receiver wasn't registered, so update the Time
		calendar = Calendar.getInstance(TimeZone.getDefault())
		// Make sure we update to the current time
		updateClock()
	}

	override fun onDetachedFromWindowInternal() {
		super.onDetachedFromWindowInternal()
		if (attached) {
			context.unregisterReceiver(receiver)
			if (showSeconds) {
				context.unregisterReceiver(screenReceiver)
			}
			attached = false
		}
	}

	private fun updateClock() {
		calendar.timeInMillis = System.currentTimeMillis()
		val is24HourFormat = DateFormat.is24HourFormat(context, UserHandle.myUserId())
		val format = if (showSeconds) {
			if (is24HourFormat) {
				"HH:mm:ss"
			} else {
				"hh:mm:ss"
			}
		} else if (is24HourFormat) {
			"HH:mm"
		} else {
			"hh:mm"
		}
		sdfClock = SimpleDateFormat(format, Locale.getDefault())
		sdfClock.timeZone = calendar.timeZone
		val strFormatted = sdfClock.format(calendar.time)
		text = strFormatted
		contentDescription = strFormatted
		// Update clock seconds if seconds are shown.
		updateSeconds()
	}

	private fun updateSeconds() {
		// Wait until we have a display to start trying to show seconds.
		if (showSeconds && display != null) {
			if (display.state == Display.STATE_ON) {
				handler.postAtTime(
					secondsTickRunnable,
					SystemClock.uptimeMillis() / 1000 * 1000 + 1000
				)
			}
		} else {
			handler.removeCallbacks(secondsTickRunnable)
		}
	}

}
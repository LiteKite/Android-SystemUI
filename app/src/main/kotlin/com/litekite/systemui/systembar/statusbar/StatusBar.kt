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

package com.litekite.systemui.systembar.statusbar

import android.app.WindowConfiguration
import android.content.res.Configuration
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.android.systemui.shared.system.ActivityManagerWrapper
import com.android.systemui.shared.system.TaskStackChangeListener
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.dependency.Dependency
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * @author Vignesh S
 * @version 1.0, 23/01/2020
 * @since 1.0
 */
@Suppress("UNUSED")
class StatusBar : SystemUI(), StatusBarServiceController.Callback {

	companion object {
		val TAG = StatusBar::class.java.simpleName
	}

	private val activityManagerWrapper = ActivityManagerWrapper.getInstance()
	private lateinit var statusBarServiceController: StatusBarServiceController
	private lateinit var statusBarWindowController: StatusBarWindowController
	private lateinit var statusBarWindow: FrameLayout
	private lateinit var statusBarView: View

	private val taskStackChangeListener = object : TaskStackChangeListener() {

		override fun onTaskStackChanged() {
			super.onTaskStackChanged()
			printLog(TAG, "onTaskStackChanged:")
			val topActivity = activityManagerWrapper.runningTask
			printLog(TAG, "topActivity: $topActivity")
			val activityType = WindowConfiguration.activityTypeToString(
				topActivity.configuration.windowConfiguration.activityType
			)
			printLog(TAG, "activityType: $activityType")
		}

	}

	override fun start() {
		putComponent(javaClass, this)
		// Connect in to the status bar manager service
		statusBarServiceController = Dependency.dependencyGraph.statusBarServiceController()
		statusBarServiceController.addCallback(this)
		// Status bar window manager
		statusBarWindowController = Dependency.dependencyGraph.statusBarWindowController()
		// Creates status bar view
		makeStatusBarView()
		// Creates navigation bar view
		makeNavigationBarView()
		// Listens for app task stack changes
		activityManagerWrapper.registerTaskStackListener(taskStackChangeListener)
	}

	private fun makeStatusBarView() {
		statusBarWindow =
			View.inflate(context, R.layout.super_status_bar, null) as FrameLayout
		statusBarView =
			View.inflate(context, R.layout.status_bar, statusBarWindow)
		statusBarWindowController.add(statusBarWindow)
	}

	private fun makeNavigationBarView() {

	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)
		printLog(TAG, "onConfigurationChanged:")
	}

	override fun onOverlayChanged() {
		super.onOverlayChanged()
		printLog(TAG, "onOverlayChanged:")
	}

	override fun setWindowState(displayId: Int, window: Int, state: Int) {
		printLog(TAG, "setWindowState:")
	}

	override fun setSystemUiVisibility(
		displayId: Int,
		vis: Int,
		fullscreenStackVis: Int,
		dockedStackVis: Int,
		mask: Int,
		fullscreenBounds: Rect?,
		dockedBounds: Rect?,
		navBarColorManagedByIme: Boolean
	) {
		printLog(TAG, "setSystemUiVisibility:")
	}

	override fun dump(fd: FileDescriptor?, pw: PrintWriter?, args: Array<out String>?) {
		super.dump(fd, pw, args)
		pw?.println("statusBarView: $statusBarView")
	}

}
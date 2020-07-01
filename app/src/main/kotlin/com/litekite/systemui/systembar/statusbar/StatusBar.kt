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
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.android.synthetic.main.status_bar.view.*
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * @author Vignesh S
 * @version 1.0, 23/01/2020
 * @since 1.0
 */
class StatusBar : SystemUI(), StatusBarServiceController.Callback {

	companion object {
		val TAG = StatusBar::class.java.simpleName
	}

	@EntryPoint
	@InstallIn(ApplicationComponent::class)
	interface StatusBarEntryPoint {

		fun getStatusBarServiceController(): StatusBarServiceController

		fun getStatusBarWindowController(): StatusBarWindowController

		fun getActivityManagerWrapper(): ActivityManagerWrapper

	}

	private lateinit var activityManagerWrapper: ActivityManagerWrapper
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
		putComponent(StatusBar::class.java, this)
		// Hilt Dependency Entry Point
		val entryPointAccessors = EntryPointAccessors.fromApplication(
			context,
			StatusBarEntryPoint::class.java
		)
		// Initiates the status bar manager service
		statusBarServiceController = entryPointAccessors.getStatusBarServiceController()
		// Attaching the status bar manager service
		statusBarServiceController.addCallback(this)
		// Initiates the status bar manager service
		statusBarWindowController = entryPointAccessors.getStatusBarWindowController()
		// Creates status bar view
		makeStatusBarView()
		// Listens for app task stack changes
		activityManagerWrapper = entryPointAccessors.getActivityManagerWrapper()
		activityManagerWrapper.registerTaskStackListener(taskStackChangeListener)
	}

	private fun makeStatusBarView() {
		statusBarWindow =
			View.inflate(context, R.layout.super_status_bar, null) as FrameLayout
		statusBarView = statusBarWindow.status_bar_container
		statusBarWindowController.add(statusBarWindow)
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
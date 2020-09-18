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

import android.app.ActivityManager
import android.content.res.Configuration
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.forEach
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.config.ConfigController
import com.litekite.systemui.taskstack.TaskStackController
import com.litekite.systemui.util.taskChanged
import com.litekite.systemui.widget.AppButtonView
import com.litekite.systemui.widget.KeyButtonView
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
class StatusBar : SystemUI(), StatusBarServiceController.Callback, ConfigController.Callback {

	companion object {
		val TAG = StatusBar::class.java.simpleName
	}

	@EntryPoint
	@InstallIn(ApplicationComponent::class)
	interface StatusBarEntryPoint {

		fun getStatusBarWindowController(): StatusBarWindowController

		fun getStatusBarServiceController(): StatusBarServiceController

		fun getConfigController(): ConfigController

		fun getTaskStackController(): TaskStackController

	}

	private lateinit var statusBarWindowController: StatusBarWindowController
	private lateinit var statusBarServiceController: StatusBarServiceController
	private lateinit var configController: ConfigController
	private lateinit var taskStackController: TaskStackController
	private lateinit var statusBarWindow: FrameLayout
	private lateinit var statusBarView: ViewGroup

	private val taskStackChangeCallback = object : TaskStackController.Callback {

		override fun onTaskStackChanged(runningTaskInfo: ActivityManager.RunningTaskInfo) {
			printLog(
				TAG,
				"onTaskStackChanged: ${runningTaskInfo.topActivity.flattenToShortString()}"
			)
			statusBarView.forEach { v ->
				if (v is AppButtonView) {
					v.taskChanged(runningTaskInfo)
				} else if (v is KeyButtonView) {
					v.taskChanged(runningTaskInfo)
				}
			}
		}

	}

	override fun start() {
		printLog(TAG, "start")
		putComponent(StatusBar::class.java, this)
		// Hilt Dependency Entry Point
		val entryPointAccessors = EntryPointAccessors.fromApplication(
			context,
			StatusBarEntryPoint::class.java
		)
		// Initiates the status bar window controller
		statusBarWindowController = entryPointAccessors.getStatusBarWindowController()
		// Creates status bar window view
		makeStatusBarView()
		// Listens for window and system ui visibility changes
		statusBarServiceController = entryPointAccessors.getStatusBarServiceController()
		statusBarServiceController.addCallback(this)
		// Listens for config changes
		configController = entryPointAccessors.getConfigController()
		configController.addCallback(this)
		// Listens for app task stack changes
		taskStackController = entryPointAccessors.getTaskStackController()
		taskStackController.addCallback(taskStackChangeCallback)
	}

	private fun makeStatusBarView() {
		statusBarWindow =
			View.inflate(context, R.layout.super_status_bar, null) as FrameLayout
		statusBarView = statusBarWindow.status_bar_container
		statusBarWindowController.add(statusBarWindow)
	}

	override fun onBootCompleted() {
		super.onBootCompleted()
		printLog(TAG, "onBootCompleted:")
	}

	override fun onConfigChanged(newConfig: Configuration) {
		super.onConfigChanged(newConfig)
		printLog(TAG, "onConfigChanged:")
	}

	override fun onDensityOrFontScaleChanged() {
		super.onDensityOrFontScaleChanged()
		printLog(TAG, "onDensityOrFontScaleChanged:")
	}

	override fun onLocaleChanged() {
		super.onLocaleChanged()
		printLog(TAG, "onLocaleChanged:")
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
		pw?.println("statusBarWindowController: $statusBarWindowController")
		pw?.println("statusBarServiceController: $statusBarServiceController")
		pw?.println("configController: $configController")
		pw?.println("TaskStackController: $TaskStackController")
		pw?.println("statusBarWindow: $statusBarWindow")
		pw?.println("statusBarView: $statusBarView")
	}

	override fun destroy() {
		printLog(TAG, "destroy:")
		// Removes window and system ui visibility change callback
		statusBarServiceController.removeCallback(this)
		// Removes config change callback
		configController.removeCallback(this)
		// Removes app task stack change callback
		taskStackController.removeCallback(taskStackChangeCallback)
		// Removes status bar window view
		statusBarWindowController.remove()
	}

}
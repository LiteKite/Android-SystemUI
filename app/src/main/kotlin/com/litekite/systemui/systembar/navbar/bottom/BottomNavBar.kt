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

import android.app.ActivityManager
import android.car.userlib.CarUserManagerHelper
import android.content.res.Configuration
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.forEach
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.systembar.statusbar.StatusBarServiceController
import com.litekite.systemui.taskstack.TaskStackController
import com.litekite.systemui.util.IntentUtils
import com.litekite.systemui.util.taskChanged
import com.litekite.systemui.widget.AppButtonView
import com.litekite.systemui.widget.KeyButtonView
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.android.synthetic.main.bottom_nav_bar.view.*
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * @author Vignesh S
 * @version 1.0, 01/07/2020
 * @since 1.0
 */
class BottomNavBar : SystemUI(), StatusBarServiceController.Callback {

	companion object {
		val TAG = BottomNavBar::class.java.simpleName
	}

	@EntryPoint
	@InstallIn(ApplicationComponent::class)
	interface BottomNavBarEntryPoint {

		fun getStatusBarServiceController(): StatusBarServiceController

		fun getBottomNavBarWindowController(): BottomNavBarWindowController

		fun getTaskStackController(): TaskStackController

		fun getUserController(): CarUserManagerHelper

	}

	private lateinit var userController: CarUserManagerHelper
	private lateinit var taskStackController: TaskStackController
	private lateinit var statusBarServiceController: StatusBarServiceController
	private lateinit var bottomNavBarWindowController: BottomNavBarWindowController
	private lateinit var bottomNavBarWindow: FrameLayout
	private lateinit var bottomNavBarView: ViewGroup

	private val taskStackChangeCallback = object : TaskStackController.Callback {

		override fun onTaskStackChanged(runningTaskInfo: ActivityManager.RunningTaskInfo) {
			val topActivity = runningTaskInfo.topActivity
			printLog(
				TAG,
				"onTaskStackChanged: ${topActivity.flattenToShortString()}"
			)
			bottomNavBarView.forEach { v ->
				if (v is AppButtonView) {
					v.taskChanged(runningTaskInfo)
				} else if (v is KeyButtonView) {
					v.taskChanged(runningTaskInfo)
				}
			}
		}

	}

	override fun start() {
		putComponent(BottomNavBar::class.java, this)
		// Hilt dependency entry point
		val entryPointAccessors = EntryPointAccessors.fromApplication(
			context,
			BottomNavBarEntryPoint::class.java
		)
		// Initiates the status bar manager service
		statusBarServiceController = entryPointAccessors.getStatusBarServiceController()
		// Attaching the status bar manager service
		statusBarServiceController.addCallback(this)
		// Initiates the bottom navigation bar window controller
		bottomNavBarWindowController = entryPointAccessors.getBottomNavBarWindowController()
		// Initiates user controller
		userController = entryPointAccessors.getUserController()
		// Initiates task stack controller
		taskStackController = entryPointAccessors.getTaskStackController()
		// Creates bottom navigation bar view
		makeBottomNavBar()
		// Updates current user avatar
		updateUserAvatar()
		// Registers event listener
		registerListeners()
	}

	private fun makeBottomNavBar() {
		bottomNavBarWindow = View.inflate(context, R.layout.super_bottom_nav_bar, null)
				as FrameLayout
		bottomNavBarView = bottomNavBarWindow.bottom_nav_bar_container
		bottomNavBarWindowController.add(bottomNavBarWindow)
	}

	private fun updateUserAvatar() {
		bottomNavBarView.cib_user_avatar.setImageBitmap(
			userController.getUserIcon(userController.currentForegroundUserInfo)
		)
	}

	private fun registerListeners() {
		// Listens for app task stack changes
		taskStackController.addCallback(taskStackChangeCallback)
		// Short press event that launches user settings activity
		bottomNavBarView.cib_user_avatar.setOnClickListener {
			val action = context.getString(R.string.action_user_settings)
			IntentUtils.launchActivity(context, action)
		}
	}

	override fun onBootCompleted() {
		super.onBootCompleted()
		printLog(TAG, "onBootCompleted:")
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
		pw?.println("bottomNavBarView: $bottomNavBarView")
	}

}
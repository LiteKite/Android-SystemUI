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

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.car.userlib.CarUserManagerHelper
import android.content.res.Configuration
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.forEach
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.config.ConfigController
import com.litekite.systemui.databinding.BottomNavBarBinding
import com.litekite.systemui.databinding.SuperBottomNavBarBinding
import com.litekite.systemui.systembar.statusbar.StatusBarServiceController
import com.litekite.systemui.taskstack.TaskStackController
import com.litekite.systemui.util.IntentUtil
import com.litekite.systemui.util.taskChanged
import com.litekite.systemui.widget.AppButtonView
import com.litekite.systemui.widget.KeyButtonView
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * @author Vignesh S
 * @version 1.0, 01/07/2020
 * @since 1.0
 */
class BottomNavBar : SystemUI(), StatusBarServiceController.Callback, ConfigController.Callback {

    companion object {
        val TAG = BottomNavBar::class.java.simpleName
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BottomNavBarEntryPoint {

        fun getBottomNavBarWindowController(): BottomNavBarWindowController

        fun getStatusBarServiceController(): StatusBarServiceController

        fun getConfigController(): ConfigController

        fun getTaskStackController(): TaskStackController

        fun getUserController(): CarUserManagerHelper
    }

    private lateinit var bottomNavBarWindowController: BottomNavBarWindowController
    private lateinit var statusBarServiceController: StatusBarServiceController
    private lateinit var bottomNavBarWindow: FrameLayout
    private lateinit var bottomNavBarViewBinding: BottomNavBarBinding
    private lateinit var bottomNavBarView: ViewGroup
    private lateinit var userController: CarUserManagerHelper
    private lateinit var configController: ConfigController
    private lateinit var taskStackController: TaskStackController

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
        super.start()
        printLog(TAG, "start:")
        putComponent(BottomNavBar::class.java, this)
        // Hilt dependency entry point
        val entryPointAccessors = EntryPointAccessors.fromApplication(
            context,
            BottomNavBarEntryPoint::class.java
        )
        // Initiates the bottom navigation bar window controller
        bottomNavBarWindowController = entryPointAccessors.getBottomNavBarWindowController()
        // Creates bottom navigation bar view
        makeBottomNavBar()
        // Initiates user controller
        userController = entryPointAccessors.getUserController()
        // Updates current user avatar
        updateUserAvatar()
        // Listens for window and system ui visibility changes
        statusBarServiceController = entryPointAccessors.getStatusBarServiceController()
        statusBarServiceController.addCallback(this)
        // Listens for config changes
        configController = entryPointAccessors.getConfigController()
        configController.addCallback(this)
        // Listens for app task stack changes
        taskStackController = entryPointAccessors.getTaskStackController()
        taskStackController.addCallback(taskStackChangeCallback)
        // Registers event listeners
        registerListeners()
    }

    private fun makeBottomNavBar() {
        val superBottomNavBarBinding =
            SuperBottomNavBarBinding.inflate(LayoutInflater.from(context))
        bottomNavBarWindow = superBottomNavBarBinding.root
        bottomNavBarViewBinding = superBottomNavBarBinding.bottomNavBar
        bottomNavBarView = bottomNavBarViewBinding.bottomNavBarContainer
        bottomNavBarWindowController.add(bottomNavBarWindow)
    }

    override fun getRootView(): View {
        return bottomNavBarWindow
    }

    private fun updateUserAvatar() {
        bottomNavBarViewBinding.cibUserAvatar.setImageBitmap(
            userController.getUserIcon(userController.currentForegroundUserInfo)
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun registerListeners() {
        // Short press event that launches user settings activity
        bottomNavBarViewBinding.cibUserAvatar.setOnClickListener {
            val action = context.getString(R.string.action_user_settings)
            IntentUtil.launchActivity(context, action)
        }
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
        pw?.println("bottomNavBarView: $bottomNavBarView")
        pw?.println("bottomNavBarWindow: $bottomNavBarWindow")
        pw?.println("userController: $userController")
        pw?.println("configController: $configController")
        pw?.println("taskStackController: $taskStackController")
        pw?.println("statusBarServiceController: $statusBarServiceController")
        pw?.println("bottomNavBarWindowController: $bottomNavBarWindowController")
    }

    override fun destroy() {
        super.destroy()
        printLog(TAG, "destroy:")
        // Removes window and system ui visibility change callback
        statusBarServiceController.removeCallback(this)
        // Removes config change callback
        configController.removeCallback(this)
        // Removes app task stack change callback
        taskStackController.removeCallback(taskStackChangeCallback)
        // Removes bottom nav bar window view
        bottomNavBarWindowController.remove()
    }
}

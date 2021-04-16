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
package com.litekite.systemui.systembar.statusbar

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
 * Encapsulates all logic for the status bar window state management.
 *
 * @author Vignesh S
 * @version 1.0, 24/01/2020
 * @since 1.0
 */
@Singleton
class StatusBarWindowController @Inject constructor(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private lateinit var lp: WindowManager.LayoutParams
    private lateinit var statusBarView: View

    /**
     * Adds the status bar view to the window manager.
     *
     * @param statusBarView The view to add.
     */
    fun add(statusBarView: View) {
        lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_STATUS_BAR,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            PixelFormat.TRANSLUCENT
        )
        lp.token = Binder()
        lp.gravity = Gravity.TOP
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        lp.title = "StatusBar"
        lp.packageName = context.packageName
        lp.windowAnimations = R.style.TopInOutAnim
        this.statusBarView = statusBarView
        windowManager.addView(this.statusBarView, lp)
    }

    /**
     * Removes the status bar view from the window manager.
     */
    fun remove() {
        windowManager.removeViewImmediate(statusBarView)
    }
}

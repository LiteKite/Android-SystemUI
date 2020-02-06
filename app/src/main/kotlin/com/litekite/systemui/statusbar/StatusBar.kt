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

package com.litekite.systemui.statusbar

import android.content.res.Configuration
import android.view.View
import android.widget.FrameLayout
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.component.Dependency
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * @author Vignesh S
 * @version 1.0, 23/01/2020
 * @since 1.0
 */
@Suppress("UNUSED")
class StatusBar : SystemUI() {

	private lateinit var statusBarWindowManager: StatusBarWindowManager
	private lateinit var statusBarWindow: FrameLayout
	private lateinit var statusBarView: View

	override fun start() {
		putComponent(javaClass, this)
		statusBarWindowManager = Dependency.getDependencyGraph().statusBarWindowManager()
		makeStatusBarView()
		makeNavigationBarView()
	}

	private fun makeStatusBarView() {
		statusBarWindow =
			View.inflate(context, R.layout.super_status_bar, null) as FrameLayout
		statusBarView =
			View.inflate(context, R.layout.status_bar, statusBarWindow)
		statusBarWindowManager.add(statusBarWindow)
	}

	private fun makeNavigationBarView() {

	}

	override fun onBootCompleted() {

	}

	override fun onConfigurationChanged(newConfig: Configuration) {

	}

	override fun dump(fd: FileDescriptor, pw: PrintWriter, args: Array<String>) {

	}

}
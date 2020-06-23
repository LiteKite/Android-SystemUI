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

package com.litekite.systemui.systembar.base

import android.content.res.Configuration
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * @author Vignesh S
 * @version 1.0, 23/01/2020
 * @since 1.0
 */
@Suppress("UNUSED")
class SystemBars : SystemUI() {

	companion object {
		val TAG = SystemBars::class.java.simpleName
	}

	private lateinit var statusBar: SystemUI

	override fun start() {
		printLog(TAG, "start")
		createStatusBarFromConfig()
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)
		statusBar.onConfigurationChanged(newConfig)
	}

	override fun onOverlayChanged() {
		super.onOverlayChanged()
		statusBar.onOverlayChanged()
	}

	override fun dump(fd: FileDescriptor?, pw: PrintWriter?, args: Array<out String>?) {
		super.dump(fd, pw, args)
		statusBar.dump(fd, pw, args)
	}

	private fun createStatusBarFromConfig() {
		val serviceComponent = context.resources.getString(R.string.config_statusBarComponent)
		statusBar = Class.forName(serviceComponent).newInstance() as SystemUI
		statusBar.context = context
		statusBar.components = components
		statusBar.start()
		printLog(TAG, "started: " + statusBar.javaClass.simpleName)
	}

}
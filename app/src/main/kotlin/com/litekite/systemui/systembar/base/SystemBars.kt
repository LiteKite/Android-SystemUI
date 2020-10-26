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
	private var navBarComponents: ArrayList<SystemUI> = ArrayList()

	override fun start() {
		printLog(TAG, "start")
		createStatusBarFromConfig()
		createNavBarsFromConfig()
	}

	override fun onBootCompleted() {
		super.onBootCompleted()
		printLog(TAG, "onBootCompleted:")
		// Notifying status bar about boot complete event
		statusBar.onBootCompleted()
		// Notifying nav bars about boot complete event
		navBarComponents.forEach { it.onBootCompleted() }
	}

	private fun createStatusBarFromConfig() {
		printLog(TAG, "createStatusBarFromConfig: staring status bar...")
		val serviceComponent = context.resources.getString(R.string.config_statusBarComponent)
		statusBar = Class.forName(serviceComponent).newInstance() as SystemUI
		statusBar.context = context
		statusBar.components = components
		statusBar.start()
		printLog(TAG, "started: " + statusBar.javaClass.simpleName)
	}

	private fun createNavBarsFromConfig() {
		printLog(TAG, "createNavBarsFromConfig: staring nav bars...")
		val serviceComponents = context.resources.getStringArray(R.array.config_navBarComponents)
		serviceComponents.forEach {
			val navBar = Class.forName(it).newInstance() as SystemUI
			navBar.context = context
			navBar.context = context
			navBar.components = components
			navBar.start()
			navBarComponents.add(navBar)
		}
	}

	override fun dump(fd: FileDescriptor?, pw: PrintWriter?, args: Array<out String>?) {
		super.dump(fd, pw, args)
		// Dumping status bar
		statusBar.dump(fd, pw, args)
		// Dumping nav bars
		navBarComponents.forEach { it.dump(fd, pw, args) }
	}

	override fun destroy() {
		super.destroy()
		printLog(TAG, "destroy")
		// Destroys status bar
		statusBar.destroy()
		// Destroys nav bars
		navBarComponents.forEach { it.destroy() }
	}

}
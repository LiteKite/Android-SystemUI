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

package com.litekite.systemui.base

import android.content.Context
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import com.litekite.systemui.fragment.FragmentHostController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ApplicationComponent
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * @author Vignesh S
 * @version 1.0, 23/01/2020
 * @since 1.0
 */
abstract class SystemUI : SystemUIServiceProvider {

	companion object {

		/**
		 * Logs messages for Debugging Purposes.
		 *
		 * @param tag     TAG is a class name in which the log come from.
		 * @param message Type of a Log Message.
		 */
		fun printLog(tag: String, message: String) {
			Log.d(tag, message)
		}

	}

	@EntryPoint
	@InstallIn(ApplicationComponent::class)
	interface SystemUIEntryPoint {

		fun getFragmentHostController(): FragmentHostController

	}

	private val fragmentHostController: FragmentHostController
	lateinit var context: Context
	lateinit var components: MutableMap<Class<*>, Any>

	init {
		// Hilt Dependency Entry Point
		val entryPointAccessors = EntryPointAccessors.fromApplication(
			context,
			SystemUIEntryPoint::class.java
		)
		// Initialize Fragment Host Controller
		fragmentHostController = entryPointAccessors.getFragmentHostController()
	}

	fun getSupportFragmentManager(): FragmentManager? {
		getRootView()?.let {
			return fragmentHostController.get(it).fragmentController.supportFragmentManager
		}
		return null
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> getComponent(interfaceType: Class<T>): T = components[interfaceType] as T

	fun <T, C : T> putComponent(interfaceType: Class<T>, component: C) {
		components[interfaceType] = component as Any
	}

	abstract fun start()

	open fun getRootView(): View? = null

	open fun onBootCompleted() {}

	open fun dump(fd: FileDescriptor?, pw: PrintWriter?, args: Array<out String>?) {}

	abstract fun destroy()

}
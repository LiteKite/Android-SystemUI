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
import android.content.res.Configuration
import android.util.Log

/**
 * @author Vignesh S
 * @version 1.0, 23/01/2020
 * @since 1.0
 */
abstract class SystemUI : SystemUIServiceProvider {

	lateinit var context: Context
	lateinit var components: MutableMap<Class<*>, Any>

	abstract fun start()

	open fun onConfigurationChanged(newConfig: Configuration) {

	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> getComponent(interfaceType: Class<T>): T = components[interfaceType] as T

	fun <T, C : T> putComponent(interfaceType: Class<T>, component: C) {
		components[interfaceType] = component as Any
	}

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

}
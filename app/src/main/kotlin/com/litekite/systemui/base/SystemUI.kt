package com.litekite.systemui.base

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import java.io.FileDescriptor
import java.io.PrintWriter

abstract class SystemUI : SystemUIServiceProvider {

	lateinit var context: Context
	lateinit var components: MutableMap<Class<*>, Any>

	abstract fun start()

	open fun onBootCompleted() {

	}

	open fun onConfigurationChanged(newConfig: Configuration) {

	}

	open fun dump(fd: FileDescriptor, pw: PrintWriter, args: Array<String>) {

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
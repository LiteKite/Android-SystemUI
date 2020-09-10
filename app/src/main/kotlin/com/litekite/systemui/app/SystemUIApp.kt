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

package com.litekite.systemui.app

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Process
import android.os.UserHandle
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.base.SystemUIServiceProvider
import com.litekite.systemui.config.ConfigController
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class.
 *
 * @author Vignesh S
 * @version 1.0, 22/01/2020
 * @since 1.0
 */
@HiltAndroidApp
class SystemUIApp : Application(), SystemUIServiceProvider {

	companion object {
		val TAG = SystemUIApp::class.java.simpleName
	}

	@Inject
	lateinit var configController: ConfigController
	private var bootCompleted: Boolean = false
	private var serviceStarted: Boolean = false

	/**
	 * Hold a reference on the stuff we start.
	 */
	internal var services: ArrayList<SystemUI> = ArrayList()
	private val components: MutableMap<Class<*>, Any> = HashMap()

	override fun onCreate() {
		super.onCreate()
		SystemUI.printLog(TAG, "onCreate: SystemUIApp started successfully")
		// Set the application theme that is inherited by all services. Note that setting the
		// application theme in the manifest does only work for activities. Keep this in sync with
		// the theme set there.
		setTheme(R.style.Theme_SystemUI)
		registerBootReceiver()
		if (Process.myUserHandle() == UserHandle.SYSTEM) {
			startSystemUIServices()
		} else {
			startSystemUISecondaryUserServices()
		}
	}

	private fun registerBootReceiver() {
		val filter = IntentFilter(Intent.ACTION_BOOT_COMPLETED)
		filter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY
		registerReceiver(object : BroadcastReceiver() {

			override fun onReceive(context: Context?, intent: Intent?) {
				SystemUI.printLog(TAG, "onReceive: BOOT_COMPLETED received")
				bootCompleted = true
				if (serviceStarted) {
					services.forEach { it.onBootCompleted() }
				}
			}

		}, filter)
	}

	/**
	 * Makes sure that all the SystemUI services are running. If they are already running, this is a
	 * no-op. This is needed to conditinally start all the services, as we only need to have it in
	 * the main process.
	 * <p>This method must only be called from the main thread.</p>
	 */
	@Synchronized
	internal fun startSystemUIServices() {
		if (serviceStarted) {
			SystemUI.printLog(
				TAG,
				"startServicesIfNeeded: already started. Skipping..."
			)
			return
		}
		val serviceComponents = resources.getStringArray(R.array.config_systemUIServiceComponents)
		startServices(serviceComponents)
	}

	/**
	 * Ensures that all the Secondary user SystemUI services are running. If they are already
	 * running, this is a no-op. This is needed to conditinally start all the services, as we only
	 * need to have it in the main process.
	 * <p>This method must only be called from the main thread.</p>
	 */
	@Synchronized
	fun startSystemUISecondaryUserServices() {
		if (serviceStarted) {
			SystemUI.printLog(
				TAG,
				"startSystemUISecondaryUserServices: already started. Skipping..."
			)
			return
		}
		val serviceComponents =
			resources.getStringArray(R.array.config_systemUIServiceComponentsPerUser)
		startServices(serviceComponents)
	}

	private fun startServices(serviceComponents: Array<String>) {
		serviceComponents.forEach {
			val systemUIService = Class.forName(it).newInstance() as SystemUI
			systemUIService.context = this
			systemUIService.components = components
			systemUIService.start()
			services.add(systemUIService)
		}
		serviceStarted = true
		// If boot complete event already been received, let SystemUI components aware of boot
		// complete event...
		if (bootCompleted) {
			services.forEach { it.onBootCompleted() }
		}
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)
		if (serviceStarted) {
			configController.onConfigChanged(newConfig)
		}
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> getComponent(interfaceType: Class<T>): T =
		components[interfaceType] as T

}
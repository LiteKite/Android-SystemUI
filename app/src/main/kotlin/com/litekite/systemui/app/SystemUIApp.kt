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
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.base.SystemUIServiceProvider
import dagger.hilt.android.HiltAndroidApp

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

	private var serviceStarted: Boolean = false

	/**
	 * Hold a reference on the stuff we start.
	 */
	internal var services: ArrayList<SystemUI> = ArrayList()
	private val components: MutableMap<Class<*>, Any> = HashMap()
	private val lastConfig = Configuration()

	override fun onCreate() {
		super.onCreate()
		// Set the application theme that is inherited by all services. Note that setting the
		// application theme in the manifest does only work for activities. Keep this in sync with
		// the theme set there.
		setTheme(R.style.Theme_SystemUI)
		SystemUI.printLog(TAG, "onCreate: SystemUIApp started successfully")
		startSystemUIServices()
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
			SystemUI.printLog(TAG, "startServicesIfNeeded: already started. Skipping...")
			return
		}
		val serviceComponents = resources.getStringArray(R.array.config_systemUIServiceComponents)
		serviceComponents.forEach {
			val systemUIService = Class.forName(it).newInstance() as SystemUI
			systemUIService.context = this
			systemUIService.components = components
			systemUIService.start()
			services.add(systemUIService)
		}
		serviceStarted = true
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		if (serviceStarted) {
			services.forEach { it.onConfigurationChanged(newConfig) }
		}
		if (lastConfig.updateFrom(newConfig) and ActivityInfo.CONFIG_ASSETS_PATHS != 0) {
			services.forEach { it.onOverlayChanged() }
		}
		super.onConfigurationChanged(newConfig)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> getComponent(interfaceType: Class<T>): T =
		components[interfaceType] as T

}
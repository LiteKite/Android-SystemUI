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

package com.litekite.systemui.power

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.*
import android.text.format.DateUtils
import com.litekite.systemui.base.SystemUI

/**
 * @author Vignesh S
 * @version 1.0, 14/02/2020
 * @since 1.0
 */
@Suppress("UNUSED")
class Power : SystemUI() {

	companion object {

		val TAG = Power::class.java.simpleName

		const val TEMPERATURE_INTERVAL = 1 * DateUtils.SECOND_IN_MILLIS

	}

	private val handler = Handler()
	private val lastConfiguration = Configuration()
	private lateinit var hardwarePropertiesManager: HardwarePropertiesManager

	// by using the same instance (method references are not guaranteed to be the same object
	// We create a method reference here so that we are guaranteed that we can remove a callback
	// each time they are created).
	private val updateTempCallback = Runnable { updateTemperatureWarning() }

	override fun start() {
		putComponent(javaClass, this)
		hardwarePropertiesManager = context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE)
				as HardwarePropertiesManager
		lastConfiguration.setTo(context.resources.configuration)
		registerThermalListener()
		updateTemperatureWarning()
	}

	private fun registerThermalListener() {
		// Enable push notifications of throttling from vendor thermal
		// management subsystem via thermal service, in addition to our
		// usual polling, to react to temperature jumps more quickly.
		val binder: IBinder? = ServiceManager.getService("thermalservice")
		if (binder != null) {
			val thermalService: IThermalService = IThermalService.Stub.asInterface(binder)
			try {
				thermalService.registerThermalEventListener(ThermalEventListener())
			} catch (e: RemoteException) {
				// Should never happen.
			}
		} else {
			printLog(TAG, "cannot find thermalservice, no throttling push notifications")
		}
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)
		val mask = ActivityInfo.CONFIG_MCC or ActivityInfo.CONFIG_MNC
		// Safe to modify mLastConfiguration here as it's only updated by the main thread (here).
		if (lastConfiguration.updateFrom(newConfig) and mask != 0) {
			// This initialization method may be called on a configuration change. Only one set of
			// ongoing callbacks should be occurring, so remove any now. updateTemperatureWarning will
			// schedule an ongoing callback.
			handler.removeCallbacks(updateTempCallback)
			handler.post(updateTempCallback)
		}
	}

	private fun updateTemperatureWarning() {
		val temps: FloatArray = hardwarePropertiesManager.getDeviceTemperatures(
			HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN,
			HardwarePropertiesManager.TEMPERATURE_CURRENT
		)
		if (temps.isNotEmpty()) {
			val temp: Float = temps[0]
			printLog(TAG, "temperature: $temp")
		}
		handler.postDelayed(updateTempCallback, TEMPERATURE_INTERVAL)
	}

	/**
	 * Thermal event received from vendor thermal management subsystem
	 */
	private inner class ThermalEventListener : IThermalEventListener.Stub() {
		override fun notifyThrottling(temperature: Temperature?) {
			// Trigger an update of the temperature warning.  Only one
			// callback can be enabled at a time, so remove any existing
			// callback; updateTemperatureWarning will schedule another one.
			handler.removeCallbacks(updateTempCallback)
			updateTemperatureWarning()
		}
	}

}
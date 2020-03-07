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

package com.litekite.systemui.car

import android.car.Car
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import com.litekite.systemui.dependency.Dependency

/**
 * @author Vignesh S
 * @version 1.0, 05/03/2020
 * @since 1.0
 */
class CarPropertyController {

	private lateinit var carPropertyManager: CarPropertyManager
	private val carController: CarController = Dependency.getDependencyGraph().carController()

	private val carConnectionCallback = object : CarController.ConnectionCallback {
		override fun onConnectionChanged(isConnected: Boolean) {
			if (isConnected) {
				createCarPropertyManager()
			}
		}
	}

	private val carPropertyCallback = object : CarPropertyManager.CarPropertyEventCallback {
		override fun onChangeEvent(p0: CarPropertyValue<*>?) {

		}

		override fun onErrorEvent(p0: Int, p1: Int) {

		}
	}

	init {
		carController.addCallback(carConnectionCallback)
		if (carController.isConnected) {
			createCarPropertyManager()
		}
	}

	private fun createCarPropertyManager() {
		carPropertyManager = carController.getManager(Car.PROPERTY_SERVICE) as CarPropertyManager
	}

	fun registerCarPropertyCallback(properties: IntArray) {
		properties.forEach {
			carPropertyManager.registerCallback(
				carPropertyCallback,
				it,
				CarPropertyManager.SENSOR_RATE_ONCHANGE
			)
		}
	}

	fun unregisterCarPropertyCallback(properties: IntArray) {
		properties.forEach {
			carPropertyManager.unregisterCallback(
				carPropertyCallback,
				it
			)
		}
	}

}
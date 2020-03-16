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
import android.car.VehicleAreaType
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.dependency.Dependency

/**
 * @author Vignesh S
 * @version 1.0, 05/03/2020
 * @since 1.0
 */
abstract class CarPropertyController {

	private val tag = javaClass.simpleName
	private val carController: CarController = Dependency.getDependencyGraph().carController()
	private var carPropertyManager: CarPropertyManager? = null

	private val carConnectionCallback = object : CarController.ConnectionCallback {
		override fun onConnectionChanged(isConnected: Boolean) {
			if (isConnected) {
				createCarPropertyManager()
			}
		}
	}

	private val carPropertyCallback = object : CarPropertyManager.CarPropertyEventCallback {

		override fun onChangeEvent(propertyValue: CarPropertyValue<*>?) {
			SystemUI.printLog(
				tag, "carPropertyCallback - onChangeEvent: ${propertyValue?.value}"
			)
			if (isCarPropertyValueAvailable(propertyValue)) {
				onCarPropertyChangeEvent(propertyValue)
			}
		}

		override fun onErrorEvent(propertyId: Int, areaId: Int) {
			SystemUI.printLog(
				tag, "carPropertyCallback - onErrorEvent: Property Id: $propertyId Area Id: $areaId"
			)
		}

		fun onGetEvent(propertyValue: CarPropertyValue<*>?) {
			SystemUI.printLog(
				tag, "carPropertyCallback - onGetEvent: ${propertyValue?.value}"
			)
			if (isCarPropertyValueAvailable(propertyValue)) {
				onCarPropertyGetEvent(propertyValue)
			}
		}

	}

	init {
		carController.addCallback(carConnectionCallback)
		createCarPropertyManager()
	}

	private fun createCarPropertyManager() {
		carPropertyManager = carController.getManager(Car.PROPERTY_SERVICE) as CarPropertyManager?
		if (carPropertyManager != null) {
			onCarPropertyManagerCreated()
		}
	}

	fun isCarPropertyValueAvailable(propertyValue: CarPropertyValue<*>?): Boolean {
		if (propertyValue == null) {
			SystemUI.printLog(tag, "isCarPropertyValueAvailable: propertyValue is null")
			return false
		}
		if (propertyValue.status != CarPropertyValue.STATUS_AVAILABLE) {
			SystemUI.printLog(
				tag,
				"isCarPropertyValueAvailable: not available for Property Id:"
						+ " ${propertyValue.propertyId} Status: ${propertyValue.status}"
			)
			return false
		}
		return true
	}

	fun getCarProperty(properties: IntArray) {
		if (!carController.isConnected) {
			SystemUI.printLog(tag, "getCarProperty: Car is not connected")
			return
		}
		properties.forEach {
			try {
				val propertyValue = carPropertyManager?.getProperty<Any>(
					it,
					VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL
				)
				carPropertyCallback.onGetEvent(propertyValue)
			} catch (e: RuntimeException) {
				SystemUI.printLog(tag, "getCarProperty - RuntimeException: $e")
			}
		}
	}

	fun setCarProperty(propertyId: Int, value: Any) {
		if (!carController.isConnected) {
			SystemUI.printLog(tag, "setCarProperty: Car is not connected")
			return
		}
		try {
			carPropertyManager?.setProperty(
				Any::class.java,
				propertyId,
				VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL,
				value
			)
		} catch (e: RuntimeException) {
			SystemUI.printLog(tag, "setCarProperty - RuntimeException: $e")
		}
	}

	fun registerCarPropertyCallback(properties: IntArray) {
		if (!carController.isConnected) {
			SystemUI.printLog(tag, "registerCarPropertyCallback: Car is not connected")
			return
		}
		properties.forEach {
			carPropertyManager?.registerCallback(
				carPropertyCallback,
				it,
				CarPropertyManager.SENSOR_RATE_ONCHANGE
			)
		}
	}

	fun unregisterCarPropertyCallback(properties: IntArray) {
		if (!carController.isConnected) {
			SystemUI.printLog(tag, "unregisterCarPropertyCallback: Car is not connected")
			return
		}
		properties.forEach {
			carPropertyManager?.unregisterCallback(
				carPropertyCallback,
				it
			)
		}
	}

	protected abstract fun onCarPropertyManagerCreated()

	protected abstract fun onCarPropertyGetEvent(propertyValue: CarPropertyValue<*>?)

	protected abstract fun onCarPropertyChangeEvent(propertyValue: CarPropertyValue<*>?)

}
/*
 * Copyright 2021-2022 LiteKite Startup. All rights reserved.
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
import android.os.RemoteException
import com.litekite.systemui.base.SystemUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Vignesh S
 * @version 1.0, 05/03/2020
 * @since 1.0
 */
abstract class CarPropertyController constructor(
    private val carController: CarController,
    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    companion object {
        val TAG: String = CarPropertyController::class.java.simpleName
    }

    private var carPropertyManager: CarPropertyManager? = null

    private val carConnectionCallback = object : CarController.Callback {
        override fun onConnectionChanged(isConnected: Boolean) {
            if (isConnected) {
                createCarPropertyManager()
            }
        }
    }

    private val carPropertyCallback = object : CarPropertyManager.CarPropertyEventCallback {

        override fun onChangeEvent(propertyValue: CarPropertyValue<*>?) {
            SystemUI.printLog(
                TAG, "carPropertyCallback - onChangeEvent: ${propertyValue?.value}"
            )
            if (isCarPropertyValueAvailable(propertyValue)) {
                onCarPropertyChangeEvent(propertyValue)
            }
        }

        override fun onErrorEvent(propertyId: Int, areaId: Int) {
            SystemUI.printLog(
                TAG, "carPropertyCallback - onErrorEvent: Property Id: $propertyId Area Id: $areaId"
            )
        }

        fun onGetEvent(propertyValue: CarPropertyValue<*>?) {
            SystemUI.printLog(
                TAG, "carPropertyCallback - onGetEvent: ${propertyValue?.value}"
            )
            if (isCarPropertyValueAvailable(propertyValue)) {
                onCarPropertyGetEvent(propertyValue)
            }
        }
    }

    protected open fun create() {
        SystemUI.printLog(TAG, "create:")
        carController.addCallback(carConnectionCallback)
    }

    private fun createCarPropertyManager() {
        carPropertyManager = carController.getManager(Car.PROPERTY_SERVICE) as CarPropertyManager?
        carPropertyManager?.let {
            onCarPropertyManagerCreated()
        }
    }

    protected abstract fun onCarPropertyManagerCreated()

    fun isCarPropertyValueAvailable(propertyValue: CarPropertyValue<*>?): Boolean {
        if (propertyValue == null) {
            SystemUI.printLog(TAG, "isCarPropertyValueAvailable: propertyValue is null")
            return false
        }
        if (propertyValue.status != CarPropertyValue.STATUS_AVAILABLE) {
            SystemUI.printLog(
                TAG,
                """isCarPropertyValueAvailable: not available for Property Id:
                    | ${propertyValue.propertyId} Status: ${propertyValue.status}""".trimMargin()
            )
            return false
        }
        return true
    }

    fun fetchCarProperty(properties: IntArray) {
        if (!carController.isConnected) {
            SystemUI.printLog(TAG, "fetchCarProperty: Car is not connected")
            return
        }
        properties.forEach { mainScope.launch { fetch(it) } }
    }

    private suspend fun fetch(property: Int) = withContext(mainScope.coroutineContext) {
        val deferred = async(ioScope.coroutineContext) {
            carPropertyManager?.getProperty<Any>(
                property,
                VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL
            )
        }
        try {
            val propertyValue = deferred.await()
            carPropertyCallback.onGetEvent(propertyValue)
        } catch (e: RemoteException) {
            SystemUI.printLog(TAG, "fetch - RemoteException: $e")
        }
    }

    protected abstract fun onCarPropertyGetEvent(propertyValue: CarPropertyValue<*>?)

    fun setCarProperty(propertyId: Int, value: Any) {
        if (!carController.isConnected) {
            SystemUI.printLog(TAG, "setCarProperty: Car is not connected")
            return
        }
        mainScope.launch { set(propertyId, value) }
    }

    private suspend fun set(propertyId: Int, value: Any) = withContext(ioScope.coroutineContext) {
        try {
            carPropertyManager?.setProperty(
                Any::class.java,
                propertyId,
                VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL,
                value
            )
        } catch (e: RemoteException) {
            SystemUI.printLog(TAG, "set - RemoteException: $e")
        }
    }

    fun registerCarPropertyCallback(properties: IntArray) {
        if (!carController.isConnected) {
            SystemUI.printLog(TAG, "registerCarPropertyCallback: Car is not connected")
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

    protected abstract fun onCarPropertyChangeEvent(propertyValue: CarPropertyValue<*>?)

    fun unregisterCarPropertyCallback(properties: IntArray) {
        if (!carController.isConnected) {
            SystemUI.printLog(TAG, "unregisterCarPropertyCallback: Car is not connected")
            return
        }
        properties.forEach {
            carPropertyManager?.unregisterCallback(
                carPropertyCallback,
                it
            )
        }
    }

    protected open fun destroy() {
        SystemUI.printLog(TAG, "destroy:")
        carController.removeCallback(carConnectionCallback)
    }
}

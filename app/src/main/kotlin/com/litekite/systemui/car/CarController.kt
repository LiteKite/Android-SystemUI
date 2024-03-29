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
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.litekite.systemui.base.CallbackProvider
import com.litekite.systemui.base.SystemUI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Vignesh S
 * @version 1.0, 03/03/2020
 * @since 1.0
 */
@Singleton
class CarController @Inject constructor(
    private val context: Context
) : CallbackProvider<CarController.Callback> {

    companion object {
        val TAG: String = CarController::class.java.simpleName
    }

    var isConnected: Boolean = false

    private lateinit var car: Car
    private val handler: Handler = Handler(Looper.getMainLooper())

    override var callbacks = ArrayList<Callback>()

    private val carServiceLifecycleListener = Car.CarServiceLifecycleListener { car, isConnected ->
        SystemUI.printLog(TAG, "onLifecycleChanged: $isConnected Car: $car")
        this.isConnected = isConnected
        notifyConnectionState()
        if (!isConnected) {
            startCar()
        }
    }

    init {
        startCar()
    }

    private fun startCar() {
        car = Car.createCar(
            context,
            handler,
            Car.CAR_WAIT_TIMEOUT_WAIT_FOREVER,
            carServiceLifecycleListener
        )
    }

    fun getManager(serviceName: String): Any? {
        if (!isConnected) {
            SystemUI.printLog(TAG, "getManager: Car is not connected")
            return null
        }
        return try {
            car.getCarManager(serviceName)
        } catch (e: IllegalStateException) {
            SystemUI.printLog(TAG, "getManager - IllegalStateException: $e")
            null
        }
    }

    override fun addCallback(cb: Callback) {
        super.addCallback(cb)
        if (isConnected) {
            notifyConnectionState()
        }
    }

    private fun notifyConnectionState() {
        callbacks.forEach { it.onConnectionChanged(isConnected) }
    }

    fun destroy() {
        if (isConnected) {
            car.disconnect()
        }
    }

    interface Callback {

        fun onConnectionChanged(isConnected: Boolean)
    }
}

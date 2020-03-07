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
import android.content.Context
import android.os.Handler
import com.litekite.systemui.base.SystemUI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Vignesh S
 * @version 1.0, 03/03/2020
 * @since 1.0
 */
@Singleton
class CarController @Inject constructor(private val context: Context) {

	private val tag = javaClass.simpleName
	private val handler: Handler = Handler()
	private lateinit var car: Car
	var isConnected: Boolean = false
	private var callbacks: ArrayList<ConnectionCallback> = ArrayList()

	private val carServiceLifecycleListener = Car.CarServiceLifecycleListener { car, isConnected ->
		SystemUI.printLog(tag, "onLifecycleChanged: $isConnected Car: $car")
		this.isConnected = isConnected
		notifyConnectionState(isConnected)
		if (!isConnected) {
			car.disconnect()
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
		return car.getCarManager(serviceName)
	}

	fun addCallback(cb: ConnectionCallback) {
		callbacks.add(cb)
	}

	fun removeCallback(cb: ConnectionCallback) {
		callbacks.add(cb)
	}

	private fun notifyConnectionState(isConnected: Boolean) {
		for (cb in callbacks) {
			cb.onConnectionChanged(isConnected)
		}
	}

	interface ConnectionCallback {

		fun onConnectionChanged(isConnected: Boolean)

	}

}
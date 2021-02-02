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
import android.car.media.CarAudioManager
import android.content.Context
import android.os.RemoteException
import com.litekite.systemui.base.CallbackProvider
import com.litekite.systemui.base.SystemUI
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Vignesh S
 * @version 1.0, 05/03/2020
 * @since 1.0
 */
@Singleton
class CarAudioController @Inject constructor(context: Context) :
	CallbackProvider<CarAudioController.Callback> {

	companion object {
		val TAG = CarAudioController::class.java.simpleName
	}

	@EntryPoint
	@InstallIn(SingletonComponent::class)
	interface CarAudioControllerEntryPoint {

		fun getCarController(): CarController

	}

	private val carController: CarController
	private var carAudioManager: CarAudioManager? = null
	override var callbacks = ArrayList<Callback>()

	private val carConnectionCallback = object :
		CarController.Callback {
		override fun onConnectionChanged(isConnected: Boolean) {
			if (isConnected) {
				createCarAudioManager()
			}
		}
	}

	private val carVolumeCallback = object : CarAudioManager.CarVolumeCallback() {
		override fun onMasterMuteChanged(zoneId: Int, flags: Int) {
			super.onMasterMuteChanged(zoneId, flags)
			SystemUI.printLog(TAG, "onMasterMuteChanged:")
		}

		override fun onGroupVolumeChanged(zoneId: Int, groupId: Int, flags: Int) {
			super.onGroupVolumeChanged(zoneId, groupId, flags)
			notifyGroupVolumeChanged()
		}
	}

	init {
		// Hilt Dependency Entry Point
		val entryPointAccessors = EntryPointAccessors.fromApplication(
			context,
			CarAudioControllerEntryPoint::class.java
		)
		// Listens for config changes
		carController = entryPointAccessors.getCarController()
		carController.addCallback(carConnectionCallback)
	}

	private fun createCarAudioManager() {
		carAudioManager = carController.getManager(Car.AUDIO_SERVICE) as CarAudioManager?
		carAudioManager?.let {
			notifyCarAudioManagerCreated()
			registerCarVolumeCallback()
		}
	}

	fun setGroupVolume(groupId: Int, volumeLevel: Int, flags: Int) {
		if (!carController.isConnected) {
			SystemUI.printLog(TAG, "setGroupVolume: Car is not connected")
			return
		}
		try {
			carAudioManager?.setGroupVolume(groupId, volumeLevel, flags)
		} catch (e: RuntimeException) {
			SystemUI.printLog(TAG, "setGroupVolume - RuntimeException: $e")
		}
	}

	fun getGroupVolume(groupId: Int) {
		if (!carController.isConnected) {
			SystemUI.printLog(TAG, "getGroupVolume: Car is not connected")
			return
		}
		try {
			carAudioManager?.getGroupVolume(groupId)
		} catch (e: RuntimeException) {
			SystemUI.printLog(TAG, "getGroupVolume - RuntimeException: $e")
		}
	}

	fun getGroupMaxVolume(groupId: Int) {
		if (!carController.isConnected) {
			SystemUI.printLog(TAG, "getGroupMaxVolume: Car is not connected")
			return
		}
		try {
			carAudioManager?.getGroupMaxVolume(groupId)
		} catch (e: RemoteException) {
			SystemUI.printLog(TAG, "getGroupMaxVolume - RuntimeException: $e")
		}
	}

	fun getGroupMinVolume(groupId: Int) {
		if (!carController.isConnected) {
			SystemUI.printLog(TAG, "getGroupMinVolume: Car is not connected")
			return
		}
		try {
			carAudioManager?.getGroupMinVolume(groupId)
		} catch (e: RuntimeException) {
			SystemUI.printLog(TAG, "getGroupMinVolume - RuntimeException: $e")
		}
	}

	private fun registerCarVolumeCallback() {
		if (!carController.isConnected) {
			SystemUI.printLog(TAG, "registerCarVolumeCallback: Car is not connected")
			return
		}
		carAudioManager?.registerCarVolumeCallback(carVolumeCallback)
	}

	private fun unregisterCarVolumeCallback() {
		if (!carController.isConnected) {
			SystemUI.printLog(TAG, "unregisterCarVolumeCallback: Car is not connected")
			return
		}
		carAudioManager?.unregisterCarVolumeCallback(carVolumeCallback)
	}

	fun destroy() {
		unregisterCarVolumeCallback()
	}

	private fun notifyCarAudioManagerCreated() {
		callbacks.forEach { it.onCarAudioManagerCreated() }
	}

	private fun notifyGroupVolumeChanged() {
		callbacks.forEach { it.onGroupVolumeChanged() }
	}

	interface Callback {

		fun onCarAudioManagerCreated()

		fun onGroupVolumeChanged()

	}

}
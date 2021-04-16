/*
 * Copyright 2021 LiteKite Startup. All rights reserved.
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
import android.media.AudioAttributes
import android.media.AudioPlaybackConfiguration
import android.media.IAudioService
import android.media.IPlaybackConfigDispatcher
import android.os.RemoteException
import android.os.ServiceManager
import android.telephony.TelephonyManager
import com.litekite.systemui.base.CallbackProvider
import com.litekite.systemui.base.SystemUI
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.stream.Collectors
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

        var activeGroupId: Int = 0
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CarAudioControllerEntryPoint {

        fun getCarController(): CarController
    }

    private val audioService =
        IAudioService.Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE))
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val carController: CarController
    private var carAudioManager: CarAudioManager? = null

    override var callbacks = ArrayList<Callback>()

    private val carConnectionCallback = object : CarController.Callback {

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
            notifyGroupVolumeChanged(groupId)
        }
    }

    private val playbackConfigDispatcher = object : IPlaybackConfigDispatcher.Stub() {

        override fun dispatchPlaybackConfigChange(
            configs: List<AudioPlaybackConfiguration>?,
            flush: Boolean
        ) {
            SystemUI.printLog(TAG, "dispatchPlaybackConfigChange:")
            activeGroupId = getVolumeGroupIdForUsage(getSuggestedAudioUsage())
            notifyActiveGroupChanged(activeGroupId)
        }
    }

    init {
        // Hilt Dependency Entry Point
        val entryPointAccessors = EntryPointAccessors.fromApplication(
            context,
            CarAudioControllerEntryPoint::class.java
        )
        // Listens for car service connection changes
        carController = entryPointAccessors.getCarController()
        carController.addCallback(carConnectionCallback)
        // Listens for audio service playback changes
        registerPlaybackCallback()
    }

    private fun createCarAudioManager() {
        carAudioManager = carController.getManager(Car.AUDIO_SERVICE) as CarAudioManager?
        carAudioManager?.let {
            notifyCarAudioManagerCreated()
            registerCarVolumeCallback()
            SystemUI.printLog(
                TAG,
                "createCarAudioManager: volume group count: ${getVolumeGroupCount()}"
            )
        }
    }

    private fun registerPlaybackCallback() {
        try {
            audioService.registerPlaybackCallback(playbackConfigDispatcher)
        } catch (e: RemoteException) {
            SystemUI.printLog(TAG, "registerPlaybackCallback - RemoteException: $e")
        }
    }

    private fun getVolumeGroupCount(): Int {
        if (!carController.isConnected) {
            SystemUI.printLog(TAG, "getVolumeGroupCount: Car is not connected")
            return 0
        }
        try {
            return carAudioManager?.getVolumeGroupCount(CarAudioManager.PRIMARY_AUDIO_ZONE) ?: 0
        } catch (e: RemoteException) {
            SystemUI.printLog(TAG, "getVolumeGroupCount - RemoteException: $e")
        }
        return 0
    }

    private fun getSuggestedAudioUsage(): Int {
        when (telephonyManager.callState) {
            TelephonyManager.CALL_STATE_RINGING -> {
                return AudioAttributes.USAGE_NOTIFICATION_RINGTONE
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                return AudioAttributes.USAGE_VOICE_COMMUNICATION
            }
            else -> {
                val configs = audioService.activePlaybackConfigurations
                    .stream()
                    .filter(AudioPlaybackConfiguration::isActive)
                    .collect(Collectors.toList())
                if (configs.isNullOrEmpty()) {
                    SystemUI.printLog(TAG, "getSuggestedAudioUsage: playback config not found.")
                    return AudioAttributes.USAGE_UNKNOWN
                }
                return configs[configs.size - 1].audioAttributes.usage
            }
        }
    }

    fun getVolumeGroupIdForUsage(usage: Int): Int {
        if (!carController.isConnected) {
            SystemUI.printLog(TAG, "getVolumeGroupIdForUsage: Car is not connected")
            return AudioAttributes.USAGE_UNKNOWN
        }
        try {
            return carAudioManager?.getVolumeGroupIdForUsage(usage) ?: AudioAttributes.USAGE_UNKNOWN
        } catch (e: RemoteException) {
            SystemUI.printLog(TAG, "getVolumeGroupIdForUsage - RemoteException: $e")
        }
        return AudioAttributes.USAGE_UNKNOWN
    }

    fun setGroupVolume(groupId: Int, volumeLevel: Int, flags: Int) {
        if (!carController.isConnected) {
            SystemUI.printLog(TAG, "setGroupVolume: Car is not connected")
            return
        }
        try {
            carAudioManager?.setGroupVolume(groupId, volumeLevel, flags)
        } catch (e: RemoteException) {
            SystemUI.printLog(TAG, "setGroupVolume - RemoteException: $e")
        }
    }

    fun getGroupVolume(groupId: Int): Int {
        if (!carController.isConnected) {
            SystemUI.printLog(TAG, "getGroupVolume: Car is not connected")
            return 0
        }
        try {
            return carAudioManager?.getGroupVolume(groupId) ?: 0
        } catch (e: RemoteException) {
            SystemUI.printLog(TAG, "getGroupVolume - RemoteException: $e")
        }
        return 0
    }

    fun getGroupMaxVolume(groupId: Int): Int {
        if (!carController.isConnected) {
            SystemUI.printLog(TAG, "getGroupMaxVolume: Car is not connected")
            return 0
        }
        try {
            return carAudioManager?.getGroupMaxVolume(groupId) ?: 0
        } catch (e: RemoteException) {
            SystemUI.printLog(TAG, "getGroupMaxVolume - RemoteException: $e")
        }
        return 0
    }

    fun getGroupMinVolume(groupId: Int): Int {
        if (!carController.isConnected) {
            SystemUI.printLog(TAG, "getGroupMinVolume: Car is not connected")
            return 0
        }
        try {
            return carAudioManager?.getGroupMinVolume(groupId) ?: 0
        } catch (e: RemoteException) {
            SystemUI.printLog(TAG, "getGroupMinVolume - RemoteException: $e")
        }
        return 0
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

    private fun notifyGroupVolumeChanged(groupId: Int) {
        callbacks.forEach { it.onGroupVolumeChanged(groupId) }
    }

    private fun notifyActiveGroupChanged(groupId: Int) {
        callbacks.forEach { it.onActiveGroupChanged(groupId) }
    }

    interface Callback {

        fun onCarAudioManagerCreated()

        fun onGroupVolumeChanged(groupId: Int)

        fun onActiveGroupChanged(groupId: Int)
    }
}

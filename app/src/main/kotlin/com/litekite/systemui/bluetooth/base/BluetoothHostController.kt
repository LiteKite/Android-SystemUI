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
package com.litekite.systemui.bluetooth.base

import android.bluetooth.BluetoothA2dpSink
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadsetClient
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.UserHandle
import com.litekite.systemui.base.SystemUI

/**
 * @author Vignesh S
 * @version 1.0, 22/09/2020
 * @since 1.0
 */
abstract class BluetoothHostController(private val context: Context) : BroadcastReceiver() {

    companion object {
        val TAG = BluetoothHostController::class.java.simpleName
    }

    @Suppress("unused")
    private enum class ProfileType(private val profile: Int) {

        HEADSET_CLIENT(BluetoothProfile.HEADSET_CLIENT),
        A2DP_SINK(BluetoothProfile.A2DP_SINK);

        companion object {

            fun valueOf(profileType: Int) = values().first { it.profile == profileType }

            fun profileTypeToString(profile: Int) = valueOf(profile).name
        }
    }

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    protected var bluetoothHeadsetClient: BluetoothHeadsetClient? = null
    protected var bluetoothA2dpSink: BluetoothA2dpSink? = null

    private val serviceListener = object : BluetoothProfile.ServiceListener {

        override fun onServiceDisconnected(profile: Int) {
            SystemUI.printLog(
                TAG,
                "serviceListener - onServiceDisconnected: ${ProfileType.profileTypeToString(profile)}"
            )
            if (profile == BluetoothProfile.HEADSET_CLIENT) {
                bluetoothHeadsetClient = null
            }
            if (profile == BluetoothProfile.A2DP_SINK) {
                bluetoothA2dpSink = null
            }
        }

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            SystemUI.printLog(
                TAG,
                "serviceListener - onServiceConnected: ${ProfileType.profileTypeToString(profile)}"
            )
            if (profile == BluetoothProfile.HEADSET_CLIENT) {
                bluetoothHeadsetClient = proxy as BluetoothHeadsetClient
            }
            if (profile == BluetoothProfile.A2DP_SINK) {
                bluetoothA2dpSink = proxy as BluetoothA2dpSink
            }
        }
    }

    fun registerHfpProfileListener() {
        // HEADSET_CLIENT service listener
        bluetoothAdapter?.getProfileProxy(
            context,
            serviceListener,
            BluetoothProfile.HEADSET_CLIENT
        )
    }

    fun registerA2dpProfileListener() {
        // A2DP_SINK service listener
        bluetoothAdapter?.getProfileProxy(
            context,
            serviceListener,
            BluetoothProfile.A2DP_SINK
        )
    }

    fun startListening() {
        context.registerReceiverAsUser(
            this,
            UserHandle.ALL,
            getIntentFilters(),
            null,
            null
        )
    }

    fun getConnectionState(devices: List<BluetoothDevice>): Int {
        return if (devices.isNotEmpty()) {
            BluetoothProfile.STATE_CONNECTED
        } else {
            BluetoothProfile.STATE_DISCONNECTED
        }
    }

    fun stopListening() {
        context.unregisterReceiver(this)
    }

    abstract fun getIntentFilters(): IntentFilter
}

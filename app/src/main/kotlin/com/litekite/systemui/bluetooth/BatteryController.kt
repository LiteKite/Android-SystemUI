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
package com.litekite.systemui.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadsetClient
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.litekite.systemui.base.CallbackProvider
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.bluetooth.base.BluetoothHostController

/**
 * @see BatteryController that is specific to the Auto use-case. For Auto, the battery icon
 * displays the battery status of a device that is connected via bluetooth and not the system's
 * battery.
 *
 * @author Vignesh S
 * @version 1.0, 21/02/2020
 * @since 1.0
 */
class BatteryController constructor(context: Context) :
    BluetoothHostController(context),
    CallbackProvider<BatteryController.Callback> {

    companion object {
        val TAG = BatteryController::class.java.simpleName
    }

    override val callbacks = ArrayList<Callback>()

    /**
     * According to the Bluetooth HFP 1.5 specification, battery levels are indicated by a
     * value from 1-5, where these values represent the following:
     * 0%% - 0, 1-25%% - 1, 26-50%% - 2, 51-75%% - 3, 76-99%% - 4, 100%% - 5
     * As a result, set the level as the average within that range.
     */
    enum class BatteryLevel(val level: Int) {

        INVALID(-1),
        EMPTY(0),
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FULL(5);

        companion object {
            fun valueOf(level: Int) = values().first { it.level == level }
        }
    }

    init {
        registerHfpProfileListener()
    }

    override fun getIntentFilters(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED)
        filter.addAction(BluetoothHeadsetClient.ACTION_AG_EVENT)
        return filter
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        SystemUI.printLog(TAG, "onReceive: action: ${intent?.action})")
        val device: BluetoothDevice? = intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        if (device == null) {
            SystemUI.printLog(TAG, "onReceive: device is null. returning...")
            return
        }
        when (intent.action) {
            BluetoothHeadsetClient.ACTION_AG_EVENT -> {
                updateBatteryLevel(device)
            }
            BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED -> {
                val newState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)
                val oldState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1)
                SystemUI.printLog(
                    TAG,
                    "onReceive: ACTION_CONNECTION_STATE_CHANGED: $oldState -> $newState"
                )
                updateConnectionState(device, newState)
            }
        }
    }

    /**
     * Notifies battery level states if it was a valid battery level
     */
    private fun updateBatteryLevel(device: BluetoothDevice) {
        // Check if battery information is available and immediately update.
        val bundle = bluetoothHeadsetClient?.getCurrentAgEvents(device)
        if (bundle == null) {
            SystemUI.printLog(TAG, "updateBatteryLevel: bundle is null. IGNORING...")
            return
        }
        val extraBatteryLevel = bundle.getInt(
            BluetoothHeadsetClient.EXTRA_BATTERY_LEVEL,
            BatteryLevel.INVALID.level
        )
        val batteryLevel = BatteryLevel.valueOf(extraBatteryLevel)
        if (batteryLevel == BatteryLevel.INVALID) {
            SystemUI.printLog(TAG, "updateBatteryLevel: invalid battery level. IGNORING...")
            return
        }
        SystemUI.printLog(TAG, "updateBatteryLevel: battery level: ${batteryLevel.level}")
        // Valid Battery Level
        notifyBatteryLevelChanged(batteryLevel)
    }

    /**
     * Notifies the battery state depending on the given connection state from the
     * @see BluetoothDevice given
     */
    private fun updateConnectionState(device: BluetoothDevice, newState: Int) {
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                SystemUI.printLog(TAG, "updateConnectionState: profile Connected!")
                updateBatteryLevel(device)
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                SystemUI.printLog(TAG, "updateConnectionState: profile disconnected!")
                notifyBatteryLevelUnavailable()
            }
        }
    }

    private fun notifyBatteryLevelChanged(batteryLevel: BatteryLevel) {
        callbacks.forEach { it.onBatteryLevelChanged(batteryLevel) }
    }

    private fun notifyBatteryLevelUnavailable() {
        callbacks.forEach { it.onBatteryLevelUnavailable() }
    }

    /**
     * A listener that will be notified whenever a change in battery level or to add or remove the
     * battery view based on its availability or its existence.
     */
    interface Callback {

        fun onBatteryLevelChanged(batteryLevel: BatteryLevel)

        fun onBatteryLevelUnavailable()
    }
}

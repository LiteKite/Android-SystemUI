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

package com.litekite.systemui.bluetooh

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadsetClient
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.litekite.systemui.base.SystemUI

/**
 * @see BatteryController that is specific to the Auto use-case. For Auto, the battery icon
 * displays the battery status of a device that is connected via bluetooth and not the system's
 * battery.
 *
 * @author Vignesh S
 * @version 1.0, 21/02/2020
 * @since 1.0
 */
class BatteryController constructor(val context: Context) : BroadcastReceiver() {

	private val tag = javaClass.simpleName
	private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
	private var bluetoothHeadsetClient: BluetoothHeadsetClient? = null
	private val callbacks: ArrayList<BatteryChangeCallback> = ArrayList()
	private var level: BatteryLevel = BatteryLevel.INVALID

	/**
	 * According to the Bluetooth HFP 1.5 specification, battery levels are indicated by a
	 * value from 1-5, where these values represent the following:
	 * 0%% - 0, 1-25%% - 1 (12%%), 26-50%% - 2 (28%%), 51-75%% - 3 (63%%), 76-99%% - 4 (87%%),
	 * 100%% - 5 (100%%)
	 * As a result, set the level as the average within that range.
	 */
	enum class BatteryLevel(val level: Int) {
		INVALID(-1),
		EMPTY(0),
		ONE(1),
		TWO(2),
		THREE(3),
		FOUR(4),
		FULL(5)
	}

	private val hfpServiceListener = object : BluetoothProfile.ServiceListener {

		override fun onServiceDisconnected(profile: Int) {
			if (profile == BluetoothProfile.HEADSET_CLIENT) {
				bluetoothHeadsetClient = null
			}
		}

		override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
			if (profile == BluetoothProfile.HEADSET_CLIENT) {
				bluetoothHeadsetClient = proxy as BluetoothHeadsetClient
			}
		}

	}

	init {
		bluetoothAdapter?.getProfileProxy(
			context,
			hfpServiceListener,
			BluetoothProfile.HEADSET_CLIENT
		)
	}

	// TODO Add Broadcast of Bluetooth ACL connection from BluetoothAdapter to remove icon
	private fun startListening() {
		val filter = IntentFilter()
		filter.addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED)
		filter.addAction(BluetoothHeadsetClient.ACTION_AG_EVENT)
		context.registerReceiver(this, filter)
	}

	private fun stopListening() {
		context.unregisterReceiver(this)
	}

	private fun addCallback(cb: BatteryChangeCallback) {
		callbacks.add(cb)
	}

	private fun removeCallback(cb: BatteryChangeCallback) {
		callbacks.remove(cb)
	}

	override fun onReceive(context: Context?, intent: Intent?) {
		SystemUI.printLog(tag, "onReceive - action: ${intent?.action})")
		when (intent?.action) {
			BluetoothHeadsetClient.ACTION_AG_EVENT -> {
				val extraBatteryLevel = intent.getIntExtra(
					BluetoothHeadsetClient.EXTRA_BATTERY_LEVEL,
					BatteryLevel.INVALID.level
				)
				val batteryLevel = BatteryLevel.valueOf(extraBatteryLevel.toString())
				updateBatteryLevel(batteryLevel)
			}
			BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED -> {
				val newState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)
				val oldState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1)
				SystemUI.printLog(
					tag, "ACTION_CONNECTION_STATE_CHANGED: $oldState -> $newState"
				)
				val device: BluetoothDevice? =
					intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
				updateBatteryState(device, newState)
			}
		}
	}

	/**
	 * Notifies battery level states if it was a valid battery level
	 */
	private fun updateBatteryLevel(batteryLevel: BatteryLevel) {
		if (batteryLevel == BatteryLevel.INVALID) {
			SystemUI.printLog(tag, "updateBatteryLevel: Invalid battery level. IGNORING...")
			return
		}
		level = batteryLevel
		SystemUI.printLog(tag, "Battery level: $batteryLevel; setting mLevel as: $level")
		// Valid Battery Level
		notifyBatteryStateAvailable()
		notifyBatteryLevelChanged()
	}

	/**
	 * Notifies the battery state depending on the given connection state from the
	 * @see BluetoothDevice given
	 */
	private fun updateBatteryState(device: BluetoothDevice?, newState: Int) {
		when (newState) {
			BluetoothProfile.STATE_CONNECTED -> {
				SystemUI.printLog(tag, "Device Connected!")
				if (bluetoothHeadsetClient == null || device == null) {
					return
				}
				// Check if battery information is available and immediately update.
				val featuresBundle: Bundle =
					bluetoothHeadsetClient?.getCurrentAgEvents(device) ?: return
				val batteryLevel = featuresBundle.getInt(
					BluetoothHeadsetClient.EXTRA_BATTERY_LEVEL,
					BatteryLevel.INVALID.level
				)
				updateBatteryLevel(BatteryLevel.valueOf(batteryLevel.toString()))
			}
			BluetoothProfile.STATE_DISCONNECTED -> {
				SystemUI.printLog(tag, "device disconnected!")
				notifyBatteryStateUnavailable()
			}
		}
	}

	private fun notifyBatteryLevelChanged() {
		for (cb in callbacks) {
			cb.onBatteryLevelChanged(level)
		}
	}

	private fun notifyBatteryStateAvailable() {
		for (cb in callbacks) {
			cb.onBatteryStateAvailable()
		}
	}

	private fun notifyBatteryStateUnavailable() {
		for (cb in callbacks) {
			cb.onBatteryStateUnavailable()
		}
	}

	/**
	 * A listener that will be notified whenever a change in battery level or to add or remove the
	 * battery view based on its availability or its existence.
	 */
	interface BatteryChangeCallback {

		fun onBatteryLevelChanged(level: BatteryLevel)

		fun onBatteryStateAvailable()

		fun onBatteryStateUnavailable()

	}

}
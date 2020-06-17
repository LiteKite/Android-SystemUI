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

package com.litekite.systemui.bluetooth

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
 * Controller that monitors signal strength for a device that is connected via bluetooth.
 *
 * @author Vignesh S
 * @version 1.0, 25/02/2020
 * @since 1.0
 */
class SignalController constructor(private val context: Context) : BroadcastReceiver() {

	companion object {
		val TAG = SignalController::class.java.simpleName
	}

	private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
	private var bluetoothHeadsetClient: BluetoothHeadsetClient? = null
	private val callbacks: ArrayList<SignalCallback> = ArrayList()
	private var level: SignalLevel = SignalLevel.INVALID

	/**
	 * All possible signal strength icons. According to the Bluetooth HFP 1.5 specification,
	 * signal strength is indicated by a value from 1-5, where these values represent the following:
	 *
	 * <p>0%% - 0, 1-25%% - 1, 26-50%% - 2, 51-75%% - 3, 76-99%% - 4, 100%% - 5
	 *
	 * <p>As a result, these are treated as an index into this array for the corresponding icon.
	 * Note that the icon is the same for 0 and 1.
	 */
	enum class SignalLevel(val level: Int) {
		INVALID(-1),
		EMPTY(0),
		ONE(1),
		TWO(2),
		THREE(3),
		FOUR(4),
		FULL(5)
	}

	/**
	 * The value that indicates if a network is unavailable. This value is according ot the
	 * Bluetooth HFP 1.5 spec, which indicates this value is one of two: 0 or 1. These stand
	 * for network unavailable and available respectively and -1 if it was invalid.
	 */
	enum class NetworkState(val state: Int) {
		INVALID(-1),
		UNAVAILABLE(0)
	}

	enum class RoamingState(val state: Int) {
		INVALID(-1),
		NO_ROAMING(0),
		ACTIVE_ROAMING(1)
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

	fun startListening() {
		val filter = IntentFilter()
		filter.addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED)
		filter.addAction(BluetoothHeadsetClient.ACTION_AG_EVENT)
		context.registerReceiver(this, filter)
	}

	fun stopListening() {
		context.unregisterReceiver(this)
	}

	fun addCallback(cb: SignalCallback) {
		callbacks.add(cb)
	}

	fun removeCallback(cb: SignalCallback) {
		callbacks.remove(cb)
	}

	override fun onReceive(context: Context?, intent: Intent?) {
		SystemUI.printLog(TAG, "onReceive - action: ${intent?.action})")
		when (intent?.action) {
			BluetoothHeadsetClient.ACTION_AG_EVENT -> {
				// Network State
				val extraNetworkState = intent.getIntExtra(
					BluetoothHeadsetClient.EXTRA_NETWORK_STATUS,
					NetworkState.INVALID.state
				)
				if (extraNetworkState != NetworkState.INVALID.state) {
					SystemUI.printLog(TAG, "EXTRA_NETWORK_STATUS:  $extraNetworkState")
					if (extraNetworkState == NetworkState.UNAVAILABLE.state) {
						updateSignalLevel(SignalLevel.EMPTY)
					}
				}
				// Signal State
				val extraSignalLevel = intent.getIntExtra(
					BluetoothHeadsetClient.EXTRA_NETWORK_SIGNAL_STRENGTH,
					SignalLevel.INVALID.level
				)
				val signalLevel = SignalLevel.valueOf(extraSignalLevel.toString())
				updateSignalLevel(signalLevel)
				// Roaming State
				val extraRoamingState = intent.getIntExtra(
					BluetoothHeadsetClient.EXTRA_NETWORK_ROAMING,
					RoamingState.INVALID.state
				)
				if (extraRoamingState != RoamingState.INVALID.state) {
					SystemUI.printLog(TAG, "EXTRA_NETWORK_ROAMING:  $extraRoamingState")
					if (extraRoamingState == RoamingState.ACTIVE_ROAMING.state) {
						notifyRoamingStateAvailable()
					} else if (extraRoamingState == RoamingState.NO_ROAMING.state) {
						notifyRoamingStateUnavailable()
					}
				}
			}
			BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED -> {
				val newState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)
				val oldState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1)
				SystemUI.printLog(
					TAG, "ACTION_CONNECTION_STATE_CHANGED: $oldState -> $newState"
				)
				val device: BluetoothDevice? =
					intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
				updateSignalState(device, newState)
			}
		}
	}

	/**
	 * Notifies signal level states if it was a valid signal level
	 */
	private fun updateSignalLevel(signalLevel: SignalLevel) {
		if (signalLevel == SignalLevel.INVALID) {
			SystemUI.printLog(TAG, "updateSignalLevel: Invalid signal level. IGNORING...")
			return
		}
		level = signalLevel
		SystemUI.printLog(TAG, "Signal level: $signalLevel; setting mLevel as: $level")
		// Valid Signal Level
		notifySignalLevelChanged()
	}

	/**
	 * Notifies the signal state depending on the given connection state from the
	 * @see BluetoothDevice given
	 */
	private fun updateSignalState(device: BluetoothDevice?, newState: Int) {
		when (newState) {
			BluetoothProfile.STATE_CONNECTED -> {
				SystemUI.printLog(TAG, "updateSignalState - profile Connected!")
				if (device == null) {
					SystemUI.printLog(TAG, "device is null. returning...")
					return
				}
				// Check if signal information is available and immediately update.
				val featuresBundle: Bundle =
					bluetoothHeadsetClient?.getCurrentAgEvents(device) ?: return
				val signalLevel = featuresBundle.getInt(
					BluetoothHeadsetClient.EXTRA_NETWORK_SIGNAL_STRENGTH,
					SignalLevel.INVALID.level
				)
				updateSignalLevel(SignalLevel.valueOf(signalLevel.toString()))
			}
			BluetoothProfile.STATE_DISCONNECTED -> {
				SystemUI.printLog(TAG, "updateSignalState - profile disconnected!")
				notifySignalLevelUnavailable()
			}
		}
	}

	private fun notifySignalLevelChanged() {
		for (cb in callbacks) {
			cb.onSignalLevelChanged(level)
		}
	}

	private fun notifySignalLevelUnavailable() {
		for (cb in callbacks) {
			cb.onSignalLevelUnavailable()
		}
	}

	private fun notifyRoamingStateAvailable() {
		for (cb in callbacks) {
			cb.onRoamingStateAvailable()
		}
	}

	private fun notifyRoamingStateUnavailable() {
		for (cb in callbacks) {
			cb.onRoamingStateUnavailable()
		}
	}

	/**
	 * A listener that will be notified whenever a change in signal level or to add or remove the
	 * signal view, roaming view based on its availability or its existence.
	 */
	interface SignalCallback {

		fun onSignalLevelChanged(signalLevel: SignalLevel)

		fun onSignalLevelUnavailable()

		fun onRoamingStateAvailable()

		fun onRoamingStateUnavailable()

	}

}
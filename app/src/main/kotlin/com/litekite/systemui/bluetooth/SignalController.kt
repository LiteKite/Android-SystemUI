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

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadsetClient
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.litekite.systemui.base.CallbackProvider
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.bluetooth.base.BluetoothHostController

/**
 * Controller that monitors signal strength for a device that is connected via bluetooth.
 *
 * @author Vignesh S
 * @version 1.0, 25/02/2020
 * @since 1.0
 */
class SignalController constructor(context: Context) : BluetoothHostController(context),
	CallbackProvider<SignalController.Callback> {

	companion object {
		val TAG = SignalController::class.java.simpleName
	}

	override val callbacks = ArrayList<Callback>()

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
		FULL(5);

		companion object {
			fun valueOf(level: Int) = values().first { it.level == level }
		}

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
		when (intent?.action) {
			BluetoothHeadsetClient.ACTION_AG_EVENT -> {
				updateSignalLevel(intent.extras)
				updateRoamingState(intent.extras)
			}
			BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED -> {
				val newState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)
				val oldState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1)
				SystemUI.printLog(
					TAG,
					"onReceive: ACTION_CONNECTION_STATE_CHANGED: $oldState -> $newState"
				)
				val device: BluetoothDevice? =
					intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
				updateConnectionState(device, newState)
			}
		}
	}

	/**
	 * Notifies signal level states if it was a valid signal level
	 */
	private fun updateSignalLevel(bundle: Bundle?) {
		if (bundle == null) {
			SystemUI.printLog(TAG, "updateSignalLevel: bundle is null. IGNORING...")
			return
		}
		// Network State
		val extraNetworkState = bundle.getInt(
			BluetoothHeadsetClient.EXTRA_NETWORK_STATUS,
			NetworkState.INVALID.state
		)
		if (extraNetworkState != NetworkState.INVALID.state) {
			SystemUI.printLog(TAG, "updateSignalLevel: EXTRA_NETWORK_STATUS:  $extraNetworkState")
			if (extraNetworkState == NetworkState.UNAVAILABLE.state) {
				// Network is unavailable. Updating signal level as empty...
				notifySignalLevelChanged(SignalLevel.EMPTY)
				return
			}
		}
		// Signal Level
		val extraSignalLevel = bundle.getInt(
			BluetoothHeadsetClient.EXTRA_NETWORK_SIGNAL_STRENGTH,
			SignalLevel.INVALID.level
		)
		val signalLevel = SignalLevel.valueOf(extraSignalLevel)
		if (signalLevel == SignalLevel.INVALID) {
			SystemUI.printLog(TAG, "updateSignalLevel: Invalid signal level. IGNORING...")
			return
		}
		SystemUI.printLog(TAG, "Signal level: ${signalLevel.level}")
		// Valid Signal Level
		notifySignalLevelChanged(signalLevel)
	}

	private fun updateRoamingState(bundle: Bundle?) {
		if (bundle == null) {
			SystemUI.printLog(TAG, "updateRoamingState: bundle is null. IGNORING...")
			return
		}
		// Roaming State
		val extraRoamingState = bundle.getInt(
			BluetoothHeadsetClient.EXTRA_NETWORK_ROAMING,
			RoamingState.INVALID.state
		)
		if (extraRoamingState != RoamingState.INVALID.state) {
			SystemUI.printLog(TAG, "updateRoamingState: EXTRA_NETWORK_ROAMING:  $extraRoamingState")
			if (extraRoamingState == RoamingState.ACTIVE_ROAMING.state) {
				notifyRoamingStateAvailable()
			} else if (extraRoamingState == RoamingState.NO_ROAMING.state) {
				notifyRoamingStateUnavailable()
			}
		}
	}

	/**
	 * Notifies the signal state depending on the given connection state from the
	 * @see BluetoothDevice given
	 */
	private fun updateConnectionState(device: BluetoothDevice?, newState: Int) {
		when (newState) {
			BluetoothProfile.STATE_CONNECTED -> {
				SystemUI.printLog(TAG, "updateConnectionState: profile Connected!")
				if (device == null) {
					SystemUI.printLog(TAG, "updateConnectionState: device is null. returning...")
					return
				}
				// Check if signal information is available and immediately update.
				val bundle = bluetoothHeadsetClient?.getCurrentAgEvents(device) ?: return
				updateSignalLevel(bundle)
				updateRoamingState(bundle)
			}
			BluetoothProfile.STATE_DISCONNECTED -> {
				SystemUI.printLog(TAG, "updateConnectionState: profile disconnected!")
				notifySignalLevelUnavailable()
			}
		}
	}

	private fun notifySignalLevelChanged(signalLevel: SignalLevel) {
		callbacks.forEach { it.onSignalLevelChanged(signalLevel) }
	}

	private fun notifySignalLevelUnavailable() {
		callbacks.forEach { it.onSignalLevelUnavailable() }
	}

	private fun notifyRoamingStateAvailable() {
		callbacks.forEach { it.onRoamingStateAvailable() }
	}

	private fun notifyRoamingStateUnavailable() {
		callbacks.forEach { it.onRoamingStateUnavailable() }
	}

	/**
	 * A listener that will be notified whenever a change in signal level or to add or remove the
	 * signal view, roaming view based on its availability or its existence.
	 */
	interface Callback {

		fun onSignalLevelChanged(signalLevel: SignalLevel)

		fun onSignalLevelUnavailable()

		fun onRoamingStateAvailable()

		fun onRoamingStateUnavailable()

	}

}
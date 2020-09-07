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

import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.litekite.systemui.base.CallbackProvider
import com.litekite.systemui.base.SystemUI

/**
 * @author Vignesh S
 * @version 1.0, 26/02/2020
 * @since 1.0
 */
class BluetoothController constructor(private val context: Context) : BroadcastReceiver(),
	CallbackProvider<BluetoothController.Callback> {

	companion object {
		val TAG = BluetoothController::class.java.simpleName
	}

	private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
	private var bluetoothHeadsetClient: BluetoothHeadsetClient? = null
	private var bluetoothA2dpSink: BluetoothA2dpSink? = null
	override val callbacks = ArrayList<Callback>()

	private val serviceListener = object : BluetoothProfile.ServiceListener {

		override fun onServiceDisconnected(profile: Int) {
			if (profile == BluetoothProfile.HEADSET_CLIENT) {
				bluetoothHeadsetClient = null
			}
			if (profile == BluetoothProfile.A2DP_SINK) {
				bluetoothA2dpSink = null
			}
		}

		override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
			if (profile == BluetoothProfile.HEADSET_CLIENT) {
				bluetoothHeadsetClient = proxy as BluetoothHeadsetClient
			}
			if (profile == BluetoothProfile.A2DP_SINK) {
				bluetoothA2dpSink = proxy as BluetoothA2dpSink
			}
		}

	}

	init {
		// HEADSET_CLIENT service listener
		bluetoothAdapter?.getProfileProxy(
			context,
			serviceListener,
			BluetoothProfile.HEADSET_CLIENT
		)
		// A2DP_SINK service listener
		bluetoothAdapter?.getProfileProxy(
			context,
			serviceListener,
			BluetoothProfile.A2DP_SINK
		)
	}

	fun startListening() {
		val filter = IntentFilter()
		filter.addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED)
		filter.addAction(BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED)
		context.registerReceiver(this, filter)
	}

	fun stopListening() {
		context.unregisterReceiver(this)
	}

	override fun onReceive(context: Context?, intent: Intent?) {
		SystemUI.printLog(TAG, "onReceive - action: ${intent?.action})")
		when (intent?.action) {
			BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED,
			BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED -> {
				val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
				if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
					SystemUI.printLog(TAG, "bluetooth is disabled. returning...")
					notifyBluetoothDisconnected()
					return
				}
				// Checks whether HEADSET_CLIENT connected
				val headsetClientDevices: List<BluetoothDevice> =
					bluetoothHeadsetClient?.connectedDevices ?: ArrayList()
				val headsetClientState = getProfileConnectionState(headsetClientDevices)
				// Checks whether A2DP_SINK connected
				val a2dpSinkDevices: List<BluetoothDevice> =
					bluetoothA2dpSink?.connectedDevices ?: ArrayList()
				val a2dpSinkState = getProfileConnectionState(a2dpSinkDevices)
				SystemUI.printLog(TAG, "headsetClientState: $headsetClientState")
				SystemUI.printLog(TAG, "a2dpSinkState: $a2dpSinkState")
				if (headsetClientState == BluetoothProfile.STATE_CONNECTED
					|| a2dpSinkState == BluetoothProfile.STATE_CONNECTED
				) {
					notifyBluetoothConnected()
				} else {
					notifyBluetoothDisconnected()
				}
			}
		}
	}

	private fun getProfileConnectionState(devices: List<BluetoothDevice>): Int {
		return if (devices.isNotEmpty()) {
			BluetoothProfile.STATE_CONNECTED
		} else {
			BluetoothProfile.STATE_DISCONNECTED
		}
	}

	private fun notifyBluetoothConnected() {
		for (cb in callbacks) {
			cb.onBluetoothConnected()
		}
	}

	private fun notifyBluetoothDisconnected() {
		for (cb in callbacks) {
			cb.onBluetoothDisconnected()
		}
	}

	interface Callback {

		fun onBluetoothConnected()

		fun onBluetoothDisconnected()

	}

}
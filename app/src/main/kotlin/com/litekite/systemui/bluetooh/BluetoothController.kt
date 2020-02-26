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

import android.bluetooth.BluetoothA2dpSink
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadsetClient
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.litekite.systemui.base.SystemUI

/**
 * @author Vignesh S
 * @version 1.0, 26/02/2020
 * @since 1.0
 */
class BluetoothController constructor(val context: Context) : BroadcastReceiver() {

	private val tag = javaClass.simpleName
	private val callbacks: ArrayList<BluetoothCallback> = ArrayList()

	private fun startListening() {
		val filter = IntentFilter()
		filter.addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED)
		filter.addAction(BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED)
		context.registerReceiver(this, filter)
	}

	private fun stopListening() {
		context.unregisterReceiver(this)
	}

	private fun addCallback(cb: BluetoothCallback) {
		callbacks.add(cb)
	}

	private fun removeCallback(cb: BluetoothCallback) {
		callbacks.remove(cb)
	}

	override fun onReceive(context: Context?, intent: Intent?) {
		SystemUI.printLog(tag, "onReceive - action: ${intent?.action})")
		when (intent?.action) {
			BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED,
			BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED -> {
				val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
				if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
					notifyBluetoothDisconnected()
					return
				}
				val headsetClientState =
					bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET_CLIENT)
				val a2dpSinkState =
					bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP_SINK)
				if (headsetClientState == BluetoothHeadsetClient.STATE_CONNECTED
					|| a2dpSinkState == BluetoothA2dpSink.STATE_CONNECTED
				) {
					notifyBluetoothConnected()
				} else {
					notifyBluetoothDisconnected()
				}
			}
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

	interface BluetoothCallback {

		fun onBluetoothConnected()

		fun onBluetoothDisconnected()

	}

}
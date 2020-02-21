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

package com.litekite.systemui.widget

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadsetClient
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
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
class BatteryController @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

	private val tag = javaClass.simpleName
	private var attached: Boolean = false
	private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
	private var bluetoothHeadsetClient: BluetoothHeadsetClient? = null

	/**
	 * According to the Bluetooth HFP 1.5 specification, battery levels are indicated by a
	 * value from 1-5, where these values represent the following:
	 * 0%% - 0, 1-25%% - 1, 26-50%% - 2, 51-75%% - 3, 76-99%% - 4, 100%% - 5
	 * As a result, set the level as the average within that range.
	 */
	private enum class BatteryLevel(val level: Int) {
		INVALID(-1),
		EMPTY(0),
		ONE(12),
		TWO(28),
		THREE(63),
		FOUR(87),
		FULL(100)
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

	private val receiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			SystemUI.printLog(tag, "onReceive - action: ${intent?.action})")
			when (intent?.action) {
				BluetoothHeadsetClient.ACTION_AG_EVENT -> {

				}
				BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED -> {

				}
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

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		if (!attached) {
			attached = true
			val filter = IntentFilter()
			filter.addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED)
			filter.addAction(BluetoothHeadsetClient.ACTION_AG_EVENT)
			context.registerReceiver(receiver, filter)
		}
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		if (attached) {
			attached = false
		}
	}

}
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

package com.litekite.systemui.systembar.statusbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.litekite.systemui.R
import com.litekite.systemui.bluetooth.BatteryController
import com.litekite.systemui.bluetooth.BluetoothController
import com.litekite.systemui.bluetooth.SignalController
import com.litekite.systemui.wifi.WifiController
import kotlinx.android.synthetic.main.status_bar.view.*

/**
 * @author Vignesh S
 * @version 1.0, 10/03/2020
 * @since 1.0
 */
class StatusBarIconContainer @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

	private val wifiController = WifiController(context)
	private val bluetoothController = BluetoothController(context)
	private val signalController = SignalController(context)
	private val batteryController = BatteryController(context)
	private val sbOrderedIconsArray = resources.getIntArray(R.array.config_status_bar_ordered_icons)
	private val sbWifiIconsArray = resources.getIntArray(R.array.config_status_bar_wifi_icons)
	private var attached: Boolean = false
	private lateinit var ivWifiIcon: AppCompatImageView

	private val wifiCallback = object : WifiController.WifiCallback {

		override fun onWifiLevelChanged(wifiLevel: WifiController.WifiLevel) {
			ivWifiIcon.setImageResource(sbWifiIconsArray[wifiLevel.level])
		}

		override fun onWifiNotConnected() {
			ivWifiIcon.setImageResource(R.drawable.ic_wifi_not_connected)
		}

		override fun onWifiDisabled() {
			ivWifiIcon.setImageResource(R.drawable.ic_wifi_disabled)
		}

	}

	private val bluetoothCallback = object : BluetoothController.BluetoothCallback {

		override fun onBluetoothConnected() {
			updateStatusBarIconIV(R.id.iv_bt_icon, R.drawable.ic_bluetooth_connected)
		}

		override fun onBluetoothDisconnected() {
			removeStatusBarIconIV(R.id.iv_bt_icon)
		}

	}

	private val signalCallback = object : SignalController.SignalCallback {

		override fun onSignalLevelChanged(level: SignalController.SignalLevel) {
			TODO("Not yet implemented")
		}

		override fun onSignalLevelUnavailable() {
			TODO("Not yet implemented")
		}

		override fun onRoamingStateAvailable() {
			TODO("Not yet implemented")
		}

		override fun onRoamingStateUnavailable() {
			TODO("Not yet implemented")
		}

	}

	private val batteryCallback = object : BatteryController.BatteryCallback {

		override fun onBatteryLevelChanged(level: BatteryController.BatteryLevel) {
			TODO("Not yet implemented")
		}

		override fun onBatteryLevelUnavailable() {
			TODO("Not yet implemented")
		}

	}

	override fun onFinishInflate() {
		super.onFinishInflate()
		// Setting Default Wifi Icon - Always visible and shown all the time...
		ivWifiIcon = iv_wifi_icon as AppCompatImageView
		ivWifiIcon.setImageResource(R.drawable.ic_wifi_disabled)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		if (!attached) {
			attached = true
			// Listens for Wifi Changes
			wifiController.addCallback(wifiCallback)
			wifiController.startListening()
			// Listens for Bluetooth Changes
			bluetoothController.addCallback(bluetoothCallback)
			bluetoothController.startListening()
			// Listens for HFP Signal Changes
			signalController.addCallback(signalCallback)
			signalController.startListening()
			// Listens for HFP Battery Changes
			batteryController.addCallback(batteryCallback)
			batteryController.startListening()
		}
	}

	@Synchronized
	private fun addStatusBarIconIV(@IdRes id: Int, drawableRes: Int) {
		val ivIcon = View.inflate(
			context,
			R.layout.widget_status_bar_container_icon,
			null
		) as AppCompatImageView
		val lp = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT,
			LinearLayout.LayoutParams.MATCH_PARENT
		)
		ivIcon.layoutParams = lp
		ivIcon.id = id
		ivIcon.setImageResource(drawableRes)
		val iconIndex = sbOrderedIconsArray.indexOfFirst { it == id }
		if (iconIndex < childCount) {
			addView(ivIcon, iconIndex)
		} else {
			addView(ivIcon)
		}
	}

	@Synchronized
	private fun updateStatusBarIconIV(@IdRes id: Int, drawableRes: Int) {
		val ivIcon = findViewById<AppCompatImageView>(id)
		if (ivIcon != null) {
			ivIcon.setImageResource(drawableRes)
			return
		}
		// Status Bar Icon is null and it is not added before. Adding a new view.
		addStatusBarIconIV(id, drawableRes)
	}

	@Synchronized
	private fun removeStatusBarIconIV(@IdRes id: Int) {
		val ivIcon = findViewById<AppCompatImageView>(id)
		ivIcon?.let { removeView(it) }
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		if (attached) {
			// Clears Wifi Change Listener
			wifiController.stopListening()
			wifiController.removeCallback(wifiCallback)
			// Clears Bluetooth Change Listener
			bluetoothController.stopListening()
			bluetoothController.removeCallback(bluetoothCallback)
			// Clears Signal Change Listener
			signalController.stopListening()
			signalController.removeCallback(signalCallback)
			// Clears Battery Change Listener
			batteryController.stopListening()
			batteryController.removeCallback(batteryCallback)
			attached = false
		}
	}

}
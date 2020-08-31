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

package com.litekite.systemui.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.litekite.systemui.base.CallbackProvider
import com.litekite.systemui.base.SystemUI

/**
 * @author Vignesh S
 * @version 1.0, 26/02/2020
 * @since 1.0
 */
class WifiController constructor(private val context: Context) : BroadcastReceiver(),
	CallbackProvider<WifiController.Callback> {

	companion object {
		val TAG = WifiController::class.java.simpleName
	}

	private val wifiManager: WifiManager? =
		context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
	private val connectivityManager: ConnectivityManager? =
		context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
	override val callbacks = ArrayList<Callback>()
	private var ssid: String? = null

	enum class WifiLevel(val level: Int) {

		EMPTY(0),
		ONE(1),
		TWO(2),
		THREE(3),
		FOUR(4);

		companion object {
			fun valueOf(level: Int) = values().first { it.level == level }
		}

	}

	private val networkCallback = object : ConnectivityManager.NetworkCallback() {

		override fun onAvailable(
			network: Network?,
			networkCapabilities: NetworkCapabilities?,
			linkProperties: LinkProperties?,
			blocked: Boolean
		) {
			super.onAvailable(network, networkCapabilities, linkProperties, blocked)
			ssid = networkCapabilities?.ssid
			updateWifiState()
		}

		override fun onCapabilitiesChanged(
			network: Network?,
			networkCapabilities: NetworkCapabilities?
		) {
			super.onCapabilitiesChanged(network, networkCapabilities)
			ssid = networkCapabilities?.ssid
			updateWifiState()
		}

		override fun onLost(network: Network?) {
			super.onLost(network)
			ssid = connectivityManager?.getNetworkCapabilities(network)?.ssid
			updateWifiState()
		}

	}

	fun startListening() {
		val filter = IntentFilter()
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION)
		context.registerReceiver(this, filter)
		// just receive notifications for scanned networks without switching network
		connectivityManager?.registerNetworkCallback(getNetworkRequest(), networkCallback)
	}

	private fun getNetworkRequest(): NetworkRequest = NetworkRequest.Builder()
		.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
		.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
		.build()

	fun stopListening() {
		context.unregisterReceiver(this)
		connectivityManager?.unregisterNetworkCallback(networkCallback)
	}

	override fun onReceive(context: Context?, intent: Intent?) {
		SystemUI.printLog(TAG, "onReceive - action: ${intent?.action})")
		when (intent?.action) {
			WifiManager.WIFI_STATE_CHANGED_ACTION,
			WifiManager.RSSI_CHANGED_ACTION -> {
				updateWifiState()
			}
		}
	}

	private fun updateWifiState() {
		wifiManager?.let {
			if (!wifiManager.isWifiEnabled) {
				ssid = null
				notifyWifiDisabled()
				return
			}
			if (ssid == null) {
				notifyWifiNotConnected()
				return
			}
			val wifiInfo: WifiInfo = wifiManager.connectionInfo ?: return
			val level = WifiManager.calculateSignalLevel(wifiInfo.rssi, WifiManager.RSSI_LEVELS)
			val wifiLevel = WifiLevel.valueOf(level)
			SystemUI.printLog(TAG, "Wifi level: ${wifiLevel.level}")
			notifyWifiLevelChanged(wifiLevel)
		}
	}

	private fun notifyWifiDisabled() {
		for (cb in callbacks) {
			cb.onWifiDisabled()
		}
	}

	private fun notifyWifiNotConnected() {
		for (cb in callbacks) {
			cb.onWifiNotConnected()
		}
	}

	private fun notifyWifiLevelChanged(wifiLevel: WifiLevel) {
		for (cb in callbacks) {
			cb.onWifiLevelChanged(wifiLevel)
		}
	}

	interface Callback {

		fun onWifiLevelChanged(wifiLevel: WifiLevel)

		fun onWifiNotConnected()

		fun onWifiDisabled()

	}

}
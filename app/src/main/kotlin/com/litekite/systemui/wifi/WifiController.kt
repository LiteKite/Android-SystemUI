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
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiSsid
import com.litekite.systemui.base.SystemUI

/**
 * @author Vignesh S
 * @version 1.0, 26/02/2020
 * @since 1.0
 */
class WifiController constructor(val context: Context) : BroadcastReceiver() {

	private val tag = javaClass.simpleName
	private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
	private val callbacks: ArrayList<WifiCallback> = ArrayList()
	private var ssid: String? = null

	enum class WifiLevel(val rssiLevel: Int) {
		EMPTY(0),
		ONE(1),
		TWO(2),
		THREE(3),
		FOUR(4)
	}

	private fun startListening() {
		val filter = IntentFilter()
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION)
		context.registerReceiver(this, filter)
	}

	private fun stopListening() {
		context.unregisterReceiver(this)
	}

	private fun addCallback(cb: WifiCallback) {
		callbacks.add(cb)
	}

	private fun removeCallback(cb: WifiCallback) {
		callbacks.remove(cb)
	}

	override fun onReceive(context: Context?, intent: Intent?) {
		SystemUI.printLog(tag, "onReceive - action: ${intent?.action})")
		when (intent?.action) {
			WifiManager.WIFI_STATE_CHANGED_ACTION,
			WifiManager.RSSI_CHANGED_ACTION -> {
				updateWifiState()
			}
			WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
				val networkInfo: NetworkInfo? =
					intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
				if (networkInfo != null && networkInfo.isConnected) {
					val wifiInfo: WifiInfo? = wifiManager.connectionInfo
					ssid = getValidSsid(wifiInfo)
				}
				updateWifiState()
			}
		}
	}

	private fun updateWifiState() {
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
		val wifiLevel = WifiLevel.valueOf(level.toString())
		notifyWifiLevelChanged(wifiLevel)
	}

	private fun getValidSsid(wifiInfo: WifiInfo?): String? {
		if (wifiInfo == null) {
			return null
		}
		if (wifiInfo.ssid != null && WifiSsid.NONE != wifiInfo.ssid) {
			return wifiInfo.ssid
		}
		//Ok, it's not in the connection info; we have to go hunting for it.
		val networks: List<WifiConfiguration> = wifiManager.configuredNetworks
		networks.forEach { if (it.networkId == wifiInfo.networkId) return it.SSID }
		return null
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

	interface WifiCallback {

		fun onWifiLevelChanged(wifiLevel: WifiLevel)

		fun onWifiNotConnected()

		fun onWifiDisabled()

	}

}
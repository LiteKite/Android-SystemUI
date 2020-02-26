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
import android.net.wifi.WifiManager
import com.litekite.systemui.base.SystemUI

/**
 * @author Vignesh S
 * @version 1.0, 26/02/2020
 * @since 1.0
 */
class WifiController constructor(val context: Context) : BroadcastReceiver() {

	private val tag = javaClass.simpleName
	private val callbacks: ArrayList<WifiCallback> = ArrayList()

	private fun startListening() {
		val filter = IntentFilter()
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
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
			WifiManager.WIFI_STATE_CHANGED_ACTION -> {

			}
			WifiManager.NETWORK_STATE_CHANGED_ACTION -> {

			}
		}
	}

	interface WifiCallback {

	}

}
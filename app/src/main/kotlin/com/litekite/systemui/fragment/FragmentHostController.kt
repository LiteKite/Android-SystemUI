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

package com.litekite.systemui.fragment

import android.content.Context
import android.content.res.Configuration
import android.util.ArrayMap
import android.view.View
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.config.ConfigController
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds a map of root views to FragmentHostProviders and generates them as needed.
 * Also dispatches the configuration changes to all current FragmentHostProviders.
 *
 * @author Vignesh S
 * @version 1.0, 07/10/2020
 * @since 1.0
 */
@Singleton
class FragmentHostController @Inject constructor(private val context: Context) :
	ConfigController.Callback {

	companion object {
		val TAG = FragmentHostController::class.java.simpleName
	}

	@Inject
	lateinit var configController: ConfigController
	private val fragmentHostsMap: ArrayMap<View, FragmentHostProvider> = ArrayMap()

	init {
		// Listens for config changes
		configController.addCallback(this)
	}

	override fun onConfigChanged(newConfig: Configuration) {
		super.onConfigChanged(newConfig)
		SystemUI.printLog(TAG, "onConfigChanged:")
		// Notifies config changes to all fragments
		fragmentHostsMap.values.forEach { it.configChanged(newConfig) }
	}

	fun get(view: View): FragmentHostProvider {
		val rootView = view.rootView
		var fragmentHostProvider: FragmentHostProvider? = fragmentHostsMap[rootView]
		if (fragmentHostProvider == null) {
			fragmentHostProvider = FragmentHostProvider(context, rootView)
			fragmentHostsMap[rootView] = fragmentHostProvider
		}
		return fragmentHostProvider
	}

	fun destroy(view: View) {
		fragmentHostsMap[view]?.destroyFragmentController()
		fragmentHostsMap.remove(view)
	}

	fun destroyAll() {
		fragmentHostsMap.values.forEach { it.destroyFragmentController() }
		fragmentHostsMap.clear()
	}

}
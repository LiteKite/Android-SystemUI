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

package com.litekite.systemui.config

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import com.litekite.systemui.base.CallbackProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Vignesh S
 * @version 1.0, 08/09/2020
 * @since 1.0
 */
@Singleton
class ConfigController @Inject constructor(context: Context) :
	CallbackProvider<ConfigController.Callback> {

	private val inCarMode: Boolean
	private var lastConfig: Configuration = context.resources.configuration
	private var uiMode: Int
	override val callbacks: ArrayList<Callback> = ArrayList()

	init {
		inCarMode = ((lastConfig.uiMode and Configuration.UI_MODE_TYPE_MASK)
				== Configuration.UI_MODE_TYPE_CAR)
		uiMode = lastConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
	}

	fun configChanged(newConfig: Configuration) {
		// Configuration change
		callbacks.forEach { it.onConfigChanged(newConfig) }
		// Density or font scale change
		val newUiMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
		if (lastConfig.densityDpi != newConfig.densityDpi
			|| lastConfig.fontScale != newConfig.fontScale
			|| (inCarMode && uiMode != newUiMode)
		) {
			callbacks.forEach { it.onDensityOrFontScaleChanged() }
			uiMode = newUiMode
		}
		// Locale change
		if (lastConfig.locales != newConfig.locales) {
			callbacks.forEach { it.onLocaleChanged() }
		}
		// Overlay change
		if ((lastConfig.updateFrom(newConfig) and ActivityInfo.CONFIG_ASSETS_PATHS) != 0) {
			callbacks.forEach { it.onOverlayChanged() }
		}
	}

	interface Callback {

		fun onConfigChanged(newConfig: Configuration) {}

		fun onDensityOrFontScaleChanged() {}

		fun onLocaleChanged() {}

		fun onOverlayChanged() {}

	}

}
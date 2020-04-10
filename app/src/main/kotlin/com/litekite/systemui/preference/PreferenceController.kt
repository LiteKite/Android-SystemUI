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

package com.litekite.systemui.preference

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Vignesh S
 * @version 1.0, 09/04/2020
 * @since 1.0
 */
@Suppress("UNUSED")
@Singleton
class PreferenceController @Inject constructor(private val context: Context) {

	companion object {
		const val PREFERENCES_SYSTEM_UI = "preferences_system_ui"
	}

	fun getBoolean(key: String): Boolean {
		return getPreference().getBoolean(key, false)
	}

	fun getInt(key: String): Int {
		return getPreference().getInt(key, 0)
	}

	fun getFloat(key: String): Float {
		return getPreference().getFloat(key, 0F)
	}

	fun getDouble(key: String): Double {
		return java.lang.Double.longBitsToDouble(
			getPreference().getLong(key, 0)
		)
	}

	fun getString(key: String): String {
		return getPreference().getString(key, "") ?: ""
	}

	fun store(key: String, value: Boolean) {
		getEditor().putBoolean(key, value).apply()
	}

	fun store(key: String, value: Int) {
		getEditor().putInt(key, value).apply()
	}

	fun store(key: String, value: Float) {
		getEditor().putFloat(key, value).apply()
	}

	fun store(key: String, value: Double) {
		getEditor().putLong(
			key,
			java.lang.Double.doubleToRawLongBits((value))
		).apply()
	}

	fun store(key: String, value: String) {
		getEditor().putString(key, value).apply()
	}

	private fun getEditor(): SharedPreferences.Editor {
		return getPreference().edit()
	}

	private fun getPreference(): SharedPreferences {
		return context.createDeviceProtectedStorageContext().getSharedPreferences(
			PREFERENCES_SYSTEM_UI,
			Context.MODE_PRIVATE
		)
	}

}
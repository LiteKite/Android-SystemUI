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

	fun getBooleanPreference(preferenceName: String, key: String): Boolean {
		return getSharedPreference(preferenceName).getBoolean(key, false)
	}

	fun getIntPreference(preferenceName: String, key: String): Int {
		return getSharedPreference(preferenceName).getInt(key, 0)
	}

	private fun getDoublePreference(preferenceName: String, key: String): Double {
		return java.lang.Double.longBitsToDouble(
			getSharedPreference(preferenceName).getLong(key, 0)
		)
	}

	fun storePreference(preferenceName: String, key: String, value: Boolean) {
		getPreferenceEditor(preferenceName).putBoolean(key, value).apply()
	}

	fun storePreference(preferenceName: String, key: String, value: Int) {
		getPreferenceEditor(preferenceName).putInt(key, value).apply()
	}

	fun storePreference(preferenceName: String, key: String, value: Double) {
		getPreferenceEditor(preferenceName).putLong(
			key,
			java.lang.Double.doubleToRawLongBits((value))
		).apply()
	}

	private fun getPreferenceEditor(preferenceName: String): SharedPreferences.Editor {
		return getSharedPreference(preferenceName).edit()
	}

	private fun getSharedPreference(preferenceName: String): SharedPreferences {
		return context.createDeviceProtectedStorageContext().getSharedPreferences(
			preferenceName,
			Context.MODE_PRIVATE
		)
	}

}
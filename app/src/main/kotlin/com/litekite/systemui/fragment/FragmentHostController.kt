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
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentController
import androidx.fragment.app.FragmentHostCallback

/**
 * @author Vignesh S
 * @version 1.0, 24/09/2020
 * @since 1.0
 */
class FragmentHostController(private val context: Context) {

	private val handler = Handler(Looper.getMainLooper())

	init {
		createFragmentController()
	}

	private fun createFragmentController() {
		FragmentController.createController(HostCallbacks())
	}

	inner class HostCallbacks : FragmentHostCallback<FragmentHostController>(context, handler, 0) {

		override fun onGetHost(): FragmentHostController? {
			return this@FragmentHostController
		}

	}

}
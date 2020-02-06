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

package com.litekite.systemui.base

import android.content.Context

/**
 * The interface for getting core components of SysUI. Exists for Testability
 * since tests don't have SystemUIApp as their ApplicationContext.
 *
 * @author Vignesh S
 * @version 1.0, 23/01/2020
 * @since 1.0
 */
interface SystemUIServiceProvider {

	fun <T> getComponent(interfaceType: Class<T>): T

	companion object {
		fun <T> getComponent(context: Context, interfaceType: Class<T>): T =
			(context.applicationContext as SystemUIServiceProvider).getComponent(interfaceType)
	}

}
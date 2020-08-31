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

package com.litekite.systemui.di

import android.car.userlib.CarUserManagerHelper
import android.content.Context
import com.litekite.systemui.car.CarAudioController
import com.litekite.systemui.car.CarController
import com.litekite.systemui.preference.PreferenceController
import com.litekite.systemui.systembar.navbar.bottom.BottomNavBarWindowController
import com.litekite.systemui.systembar.statusbar.StatusBarWindowController
import com.litekite.systemui.taskstack.TaskStackController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

/**
 * @author Vignesh S
 * @version 1.0, 27/01/2020
 * @since 1.0
 */
@Module
@InstallIn(ApplicationComponent::class)
object SystemUIControllers {

	@Provides
	@Singleton
	fun provideTaskStackController(): TaskStackController = TaskStackController()

	@Provides
	@Singleton
	fun provideCarAudioController() = CarAudioController()

	@Provides
	@Singleton
	fun provideCarController(@ApplicationContext context: Context) = CarController(context)

	@Provides
	@Singleton
	fun provideUserController(@ApplicationContext context: Context) = CarUserManagerHelper(context)

	@Provides
	@Singleton
	fun providePreferenceController(@ApplicationContext context: Context) =
		PreferenceController(context)

	@Provides
	@Singleton
	fun provideStatusBarWindowController(@ApplicationContext context: Context) =
		StatusBarWindowController(context)

	@Provides
	@Singleton
	fun provideBottomNavBarWindowController(@ApplicationContext context: Context) =
		BottomNavBarWindowController(context)

}
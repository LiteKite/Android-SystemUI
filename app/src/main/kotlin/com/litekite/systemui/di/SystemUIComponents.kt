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

import android.content.Context
import com.litekite.systemui.base.SystemUIServiceProvider
import com.litekite.systemui.power.Power
import com.litekite.systemui.systembar.navbar.bottom.BottomNavBar
import com.litekite.systemui.systembar.statusbar.StatusBar
import com.litekite.systemui.systembar.volumebar.VolumeBar
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

/**
 * @author Vignesh S
 * @version 1.0, 23/06/2020
 * @since 1.0
 */
@Module
@InstallIn(ApplicationComponent::class)
object SystemUIComponents {

	@Provides
	@Singleton
	fun providePower(@ApplicationContext context: Context): Power =
		SystemUIServiceProvider.getComponent(context, Power::class.java)

	@Provides
	@Singleton
	fun provideStatusBar(@ApplicationContext context: Context): StatusBar =
		SystemUIServiceProvider.getComponent(context, StatusBar::class.java)

	@Provides
	@Singleton
	fun provideBottomNavBar(@ApplicationContext context: Context): BottomNavBar =
		SystemUIServiceProvider.getComponent(context, BottomNavBar::class.java)

	@Provides
	@Singleton
	fun provideVolumeBar(@ApplicationContext context: Context): VolumeBar =
		SystemUIServiceProvider.getComponent(context, VolumeBar::class.java)

}
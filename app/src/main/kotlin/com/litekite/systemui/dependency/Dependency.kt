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

package com.litekite.systemui.dependency

import android.content.Context
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.car.CarController
import com.litekite.systemui.preference.PreferenceController
import com.litekite.systemui.systembar.statusbar.StatusBarServiceController
import com.litekite.systemui.systembar.statusbar.StatusBarWindowController
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author Vignesh S
 * @version 1.0, 27/01/2020
 * @since 1.0
 */
class Dependency : SystemUI() {

	override fun start() {
		printLog(TAG, "start")
		createDependencies()
	}

	private fun createDependencies() {
		dependencyGraph =
			DaggerDependency_DependencyGraph.builder().dependencyModule(DependencyModule()).build()
	}

	@Module
	inner class DependencyModule {

		@Provides
		fun provideContext(): Context = context

		@Provides
		@Singleton
		fun providePreferenceController(context: Context): PreferenceController =
			PreferenceController(context)

		@Provides
		@Singleton
		fun provideStatusBarWindowController(context: Context): StatusBarWindowController =
			StatusBarWindowController(context)

		@Provides
		@Singleton
		fun provideCarController(context: Context): CarController = CarController(context)

	}

	@Singleton
	@Component(modules = [DependencyModule::class])
	interface DependencyGraph {

		fun preferenceController(): PreferenceController

		fun statusBarWindowController(): StatusBarWindowController

		fun carController(): CarController

		fun statusBarServiceController(): StatusBarServiceController

	}

	companion object {

		val TAG = Dependency::class.java.simpleName

		private lateinit var dependencyGraph: DependencyGraph

		fun getDependencyGraph() = dependencyGraph

	}

}
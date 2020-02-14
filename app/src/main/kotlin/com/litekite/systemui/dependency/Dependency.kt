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
import com.litekite.systemui.systembar.statusbar.StatusBarWindowManager
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

	private val tag = javaClass.simpleName

	override fun start() {
		printLog(tag, "start")
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
		fun provideStatusBarWindowManager(context: Context): StatusBarWindowManager =
			StatusBarWindowManager(context)

	}

	@Singleton
	@Component(modules = [DependencyModule::class])
	interface DependencyGraph {

		fun statusBarWindowManager(): StatusBarWindowManager

	}

	companion object {

		private lateinit var dependencyGraph: DependencyGraph

		fun getDependencyGraph() = dependencyGraph

	}

}
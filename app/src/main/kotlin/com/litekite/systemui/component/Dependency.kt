package com.litekite.systemui.component

import android.content.Context
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.statusbar.StatusBarWindowManager
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

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
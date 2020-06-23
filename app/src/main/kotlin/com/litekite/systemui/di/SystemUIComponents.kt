package com.litekite.systemui.di

import android.content.Context
import com.litekite.systemui.base.SystemUIServiceProvider
import com.litekite.systemui.power.Power
import com.litekite.systemui.systembar.statusbar.StatusBar
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

}
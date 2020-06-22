package com.litekite.systemui.di

import com.litekite.systemui.power.Power
import com.litekite.systemui.systembar.base.SystemBars
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

/**
 * @author Vignesh S
 * @version 1.0, 22/06/2020
 * @since 1.0
 */
@EntryPoint
@InstallIn(ApplicationComponent::class)
interface SystemUIServiceComponents {

	fun getPower() : Power

	fun getSystemBars() : SystemBars

}
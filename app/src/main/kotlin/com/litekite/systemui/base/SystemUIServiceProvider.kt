package com.litekite.systemui.base

import android.content.Context

/**
 * The interface for getting core components of SysUI. Exists for Testability
 * since tests don't have SystemUIApp as their ApplicationContext.
 */
interface SystemUIServiceProvider {

	fun <T> getComponent(interfaceType: Class<T>): T

	companion object {
		fun <T> getComponent(context: Context, interfaceType: Class<T>): T =
			(context.applicationContext as SystemUIServiceProvider).getComponent(interfaceType)
	}

}
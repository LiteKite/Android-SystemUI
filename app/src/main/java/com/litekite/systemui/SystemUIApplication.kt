package com.litekite.systemui

import android.app.Application
import android.util.Log

class SystemUIApplication: Application() {

	private val tag = "SystemUIApplication"
	/**
	 * Hold a reference on the stuff we start.
	 */
	private val services: Array<SystemUI>? = null

	override fun onCreate() {
		super.onCreate()

		// Set the application theme that is inherited by all services. Note that setting the
		// application theme in the manifest does only work for activities. Keep this in sync with
		// the theme set there.
		setTheme(R.style.Theme_SystemUI)

		Log.d(tag, " started successfully")

		startServicesIfNeeded()
	}

	/**
	 * Makes sure that all the SystemUI services are running. If they are already running, this is a
	 * no-op. This is needed to conditinally start all the services, as we only need to have it in
	 * the main process.
	 * <p>This method must only be called from the main thread.</p>
	 */
	private fun startServicesIfNeeded() {
		services = resources.getStringArray(R.array.config_systemUIServiceComponents)
	}

}
package com.litekite.systemui

import android.app.Application
import android.content.res.Configuration

class SystemUIApplication: Application(), SystemUIServiceProvider {

	private val tag = "SystemUIApplication"
	private var serviceStarted: Boolean = false
	/**
	 * Hold a reference on the stuff we start.
	 */
	private lateinit var services: ArrayList<SystemUI>
	private lateinit var components: MutableMap<Class<*>, Any>

	override fun onCreate() {
		super.onCreate()
		// Set the application theme that is inherited by all services. Note that setting the
		// application theme in the manifest does only work for activities. Keep this in sync with
		// the theme set there.
		setTheme(R.style.Theme_SystemUI)
		SystemUI.printLog(tag, "onCreate: SystemUIApplication started successfully")
		startServicesIfNeeded()
	}

	/**
	 * Makes sure that all the SystemUI services are running. If they are already running, this is a
	 * no-op. This is needed to conditinally start all the services, as we only need to have it in
	 * the main process.
	 * <p>This method must only be called from the main thread.</p>
	 */
	private fun startServicesIfNeeded() {
		val serviceComponents = resources.getStringArray(R.array.config_systemUIServiceComponents)
		for (service in serviceComponents) {
			val systemUIService = Class.forName(service).newInstance() as SystemUI
			systemUIService.context = this
			systemUIService.start()
			services.add(systemUIService)
		}
		serviceStarted = true
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		if (serviceStarted) {
			for (service in services) {
				service.onConfigurationChanged(newConfig)
			}
		}
		super.onConfigurationChanged(newConfig)
	}

	@Suppress("UNCHECKED_CAST")
	override fun <T> getComponent(interfaceType: Class<T>): T =
		components[interfaceType] as T

}
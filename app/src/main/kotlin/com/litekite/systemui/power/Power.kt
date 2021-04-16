/*
 * Copyright 2021 LiteKite Startup. All rights reserved.
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
package com.litekite.systemui.power

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Handler
import android.os.HardwarePropertiesManager
import android.os.IBinder
import android.os.IThermalEventListener
import android.os.IThermalService
import android.os.RemoteException
import android.os.ServiceManager
import android.os.Temperature
import android.text.format.DateUtils
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.config.ConfigController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * @author Vignesh S
 * @version 1.0, 14/02/2020
 * @since 1.0
 */
class Power : SystemUI(), ConfigController.Callback {

    companion object {

        val TAG = Power::class.java.simpleName

        const val TEMPERATURE_INTERVAL = 1 * DateUtils.SECOND_IN_MILLIS
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PowerEntryPoint {

        fun getConfigController(): ConfigController
    }

    private val handler = Handler()
    private val lastConfiguration = Configuration()
    private lateinit var hardwarePropertiesManager: HardwarePropertiesManager
    private var thermalService: IThermalService? = null
    private val thermalEventListener = ThermalEventListener()
    private lateinit var configController: ConfigController

    // by using the same instance (method references are not guaranteed to be the same object
    // We create a method reference here so that we are guaranteed that we can remove a callback
    // each time they are created).
    private val updateTempCallback = Runnable { updateTemperatureWarning() }

    override fun start() {
        super.start()
        printLog(TAG, "start:")
        putComponent(Power::class.java, this)
        // Hilt Dependency Entry Point
        val entryPointAccessors = EntryPointAccessors.fromApplication(
            context,
            PowerEntryPoint::class.java
        )
        hardwarePropertiesManager = context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE)
            as HardwarePropertiesManager
        lastConfiguration.setTo(context.resources.configuration)
        registerThermalListener()
        updateTemperatureWarning()
        // Listens for config changes
        configController = entryPointAccessors.getConfigController()
        configController.addCallback(this)
    }

    private fun registerThermalListener() {
        // Enable push notifications of throttling from vendor thermal
        // management subsystem via thermal service, in addition to our
        // usual polling, to react to temperature jumps more quickly.
        val binder: IBinder? = ServiceManager.getService("thermalservice")
        if (binder != null) {
            thermalService = IThermalService.Stub.asInterface(binder)
            try {
                thermalService?.registerThermalEventListener(thermalEventListener)
            } catch (e: RemoteException) {
                // Should never happen.
            }
        } else {
            printLog(TAG, "cannot find thermalservice, no throttling push notifications")
        }
    }

    override fun onConfigChanged(newConfig: Configuration) {
        super.onConfigChanged(newConfig)
        printLog(TAG, "onConfigChanged:")
        val mask = ActivityInfo.CONFIG_MCC or ActivityInfo.CONFIG_MNC
        // Safe to modify lastConfiguration here as it's only updated by the main thread (here).
        if (lastConfiguration.updateFrom(newConfig) and mask != 0) {
            // This initialization method may be called on a configuration change. Only one set of
            // ongoing callbacks should be occurring, so remove any now. updateTemperatureWarning will
            // schedule an ongoing callback.
            handler.removeCallbacks(updateTempCallback)
            handler.post(updateTempCallback)
        }
    }

    private fun updateTemperatureWarning() {
        val temps: FloatArray = hardwarePropertiesManager.getDeviceTemperatures(
            HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN,
            HardwarePropertiesManager.TEMPERATURE_CURRENT
        )
        if (temps.isNotEmpty()) {
            val temp: Float = temps[0]
            printLog(TAG, "temperature: $temp")
        }
        handler.postDelayed(updateTempCallback, TEMPERATURE_INTERVAL)
    }

    override fun dump(fd: FileDescriptor?, pw: PrintWriter?, args: Array<out String>?) {
        super.dump(fd, pw, args)
        pw?.println("hardwarePropertiesManager: $hardwarePropertiesManager")
        pw?.println("thermalService: $thermalService")
        pw?.println("configController: $configController")
    }

    override fun destroy() {
        super.destroy()
        printLog(TAG, "destroy:")
        // Removes thermal event listener
        thermalService?.unregisterThermalEventListener(thermalEventListener)
        // Remove update temperature callback
        handler.removeCallbacks(updateTempCallback)
        // Removes config change callback
        configController.removeCallback(this)
    }

    /**
     * Thermal event received from vendor thermal management subsystem
     */
    private inner class ThermalEventListener : IThermalEventListener.Stub() {
        override fun notifyThrottling(temperature: Temperature?) {
            // Trigger an update of the temperature warning.  Only one
            // callback can be enabled at a time, so remove any existing
            // callback; updateTemperatureWarning will schedule another one.
            handler.removeCallbacks(updateTempCallback)
            updateTemperatureWarning()
        }
    }
}

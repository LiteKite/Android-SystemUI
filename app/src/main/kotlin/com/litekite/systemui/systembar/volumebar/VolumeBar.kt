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

package com.litekite.systemui.systembar.volumebar

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.motion.widget.MotionLayout
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.car.CarAudioController
import com.litekite.systemui.config.ConfigController
import com.litekite.systemui.databinding.SuperVolumeBarBinding
import com.litekite.systemui.databinding.VolumeBarBinding
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * @author Vignesh S
 * @version 1.0, 14/12/2020
 * @since 1.0
 */
class VolumeBar : SystemUI(), ConfigController.Callback {

	companion object {
		val TAG = VolumeBar::class.java.simpleName
	}

	@EntryPoint
	@InstallIn(SingletonComponent::class)
	interface VolumeBarEntryPoint {

		fun getVolumeBarWindowController(): VolumeBarWindowController

		fun getConfigController(): ConfigController

		fun getCarAudioController(): CarAudioController

	}

	private lateinit var volumeBarWindowController: VolumeBarWindowController
	private lateinit var configController: ConfigController
	private lateinit var carAudioController: CarAudioController
	private lateinit var volumeBarWindow: FrameLayout
	private lateinit var volumeBarViewBinding: VolumeBarBinding
	private lateinit var volumeBarView: MotionLayout

	override fun start() {
		super.start()
		printLog(TAG, "start:")
		putComponent(VolumeBar::class.java, this)
		// Hilt dependency entry point
		val entryPointAccessors = EntryPointAccessors.fromApplication(
			context,
			VolumeBarEntryPoint::class.java
		)
		// Initiates the volume bar window controller
		volumeBarWindowController = entryPointAccessors.getVolumeBarWindowController()
		// Creates volume navigation bar view
		makeVolumeBar()
		// Listens for config changes
		configController = entryPointAccessors.getConfigController()
		configController.addCallback(this)
	}

	private fun makeVolumeBar() {
		val superVolumeBarBinding = SuperVolumeBarBinding.inflate(LayoutInflater.from(context))
		volumeBarWindow = superVolumeBarBinding.root
		volumeBarViewBinding = superVolumeBarBinding.volumeBar
		volumeBarView = volumeBarViewBinding.volumeBarContainer
		volumeBarWindowController.add(volumeBarWindow)
	}

	override fun getRootView(): View {
		return volumeBarWindow
	}

	override fun onBootCompleted() {
		super.onBootCompleted()
		printLog(TAG, "onBootCompleted:")
	}

	override fun onConfigChanged(newConfig: Configuration) {
		super.onConfigChanged(newConfig)
		printLog(TAG, "onConfigChanged:")
	}

	override fun onDensityOrFontScaleChanged() {
		super.onDensityOrFontScaleChanged()
		printLog(TAG, "onDensityOrFontScaleChanged:")
	}

	override fun onLocaleChanged() {
		super.onLocaleChanged()
		printLog(TAG, "onLocaleChanged:")
	}

	override fun onOverlayChanged() {
		super.onOverlayChanged()
		printLog(TAG, "onOverlayChanged:")
	}

	override fun dump(fd: FileDescriptor?, pw: PrintWriter?, args: Array<out String>?) {
		super.dump(fd, pw, args)
		pw?.println("volumeBarView: $volumeBarView")
		pw?.println("volumeBarWindow: $volumeBarWindow")
		pw?.println("configController: $configController")
		pw?.println("carAudioController: $carAudioController")
		pw?.println("volumeBarWindowController: $volumeBarWindowController")
	}

	override fun destroy() {
		super.destroy()
		printLog(TAG, "destroy:")
		// Removes config change callback
		configController.removeCallback(this)
		// Removes volume bar window view
		volumeBarWindowController.remove()
	}

}
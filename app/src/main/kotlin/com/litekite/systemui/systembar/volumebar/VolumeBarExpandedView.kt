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

import android.content.Context
import android.media.AudioManager
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import com.litekite.systemui.car.CarAudioController
import com.litekite.systemui.databinding.VolumeBarBinding
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * @author Vignesh S
 * @version 1.0, 09/02/2021
 * @since 1.0
 */
class VolumeBarExpandedView @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

	companion object {
		val TAG = VolumeBarExpandedView::class.java.simpleName
	}

	@EntryPoint
	@InstallIn(SingletonComponent::class)
	interface VolumeBarExpandedEntryPoint {

		fun getCarAudioController(): CarAudioController

	}

	private var attached: Boolean = false
	private lateinit var volumeBarBinding: VolumeBarBinding
	private lateinit var carAudioController: CarAudioController
	private var userInteractingWithSeekBar: Boolean = false

	private val carAudioControllerCallback = object : CarAudioController.Callback {

		override fun onCarAudioManagerCreated() {
			SystemUI.printLog(TAG, "onCarAudioManagerCreated:")
			updateVolume()
		}

		override fun onGroupVolumeChanged(groupId: Int) {
			SystemUI.printLog(TAG, "onGroupVolumeChanged: groupId: $groupId")
		}

		override fun onActiveGroupChanged(groupId: Int) {
			SystemUI.printLog(TAG, "onActiveGroupChanged: groupId: $groupId")
			updateVolume()
		}

	}

	private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {

		private var userTrackingGroupId = CarAudioController.activeGroupId

		override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
			if (!fromUser) {
				SystemUI.printLog(TAG, "onProgressChanged: not from user. Ignoring...")
				return
			}
			if (userTrackingGroupId != CarAudioController.activeGroupId) {
				SystemUI.printLog(
					TAG,
					"""onProgressChanged: user tracking group id does not match with the current 
						|active group id. Ignoring...""".trimMargin()
				)
				return
			}
			carAudioController.setGroupVolume(
				userTrackingGroupId,
				progress,
				AudioManager.FLAG_PLAY_SOUND
			)
			volumeBarBinding.tvVolumeGroupLevel.text = progress.toString()
		}

		override fun onStartTrackingTouch(seekBar: SeekBar?) {
			userInteractingWithSeekBar = true
			userTrackingGroupId = CarAudioController.activeGroupId
		}

		override fun onStopTrackingTouch(seekBar: SeekBar?) {
			userInteractingWithSeekBar = false
			userTrackingGroupId = CarAudioController.activeGroupId
			updateVolume()
		}

	}

	init {
		// Hilt dependency entry point
		val entryPointAccessors = EntryPointAccessors.fromApplication(
			context,
			VolumeBarExpandedEntryPoint::class.java
		)
		carAudioController = entryPointAccessors.getCarAudioController()
		carAudioController.addCallback(carAudioControllerCallback)
	}

	override fun onFinishInflate() {
		super.onFinishInflate()
		SystemUI.printLog(TAG, "onFinishInflate:")
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		SystemUI.printLog(TAG, "onAttachedToWindow:")
		if (!attached) {
			attached = true
			// volume bar view binding
			volumeBarBinding = VolumeBarBinding.bind(parent as ViewGroup)
			volumeBarBinding.sbVolume.setOnSeekBarChangeListener(seekBarChangeListener)
			updateVolume()
		}
	}

	private fun updateVolume() {
		if (userInteractingWithSeekBar) {
			SystemUI.printLog(TAG, "updateVolume: user interacting with volume. Ignoring...")
			return
		}
		// Min volume limit
		volumeBarBinding.sbVolume.min =
			carAudioController.getGroupMinVolume(CarAudioController.activeGroupId)
		// Max volume limit
		volumeBarBinding.sbVolume.max =
			carAudioController.getGroupMaxVolume(CarAudioController.activeGroupId)
		// current active volume
		val currentVolume = carAudioController.getGroupVolume(CarAudioController.activeGroupId)
		volumeBarBinding.sbVolume.progress = currentVolume
		// updates current volume group name
		val volumeGroups = context.resources.getStringArray(R.array.config_volume_groups)
		volumeBarBinding.tvVolumeGroupName.text = volumeGroups[CarAudioController.activeGroupId]
		// updates current volume group level
		volumeBarBinding.tvVolumeGroupLevel.text = currentVolume.toString()
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		SystemUI.printLog(TAG, "onDetachedFromWindow:")
		if (attached) {
			carAudioController.removeCallback(carAudioControllerCallback)
			volumeBarBinding.sbVolume.setOnSeekBarChangeListener(null)
			attached = false
		}
	}

}
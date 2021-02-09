package com.litekite.systemui.systembar.volumebar

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
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

	private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {

		override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
			carAudioController.setGroupVolume(
				AudioAttributes.USAGE_UNKNOWN,
				progress,
				AudioManager.FLAG_PLAY_SOUND
			)
			volumeBarBinding.tvVolumeGroupLevel.text = progress.toString()
		}

		override fun onStartTrackingTouch(seekBar: SeekBar?) {

		}

		override fun onStopTrackingTouch(seekBar: SeekBar?) {

		}

	}

	init {
		// Hilt dependency entry point
		val entryPointAccessors = EntryPointAccessors.fromApplication(
			context,
			VolumeBarExpandedEntryPoint::class.java
		)
		carAudioController = entryPointAccessors.getCarAudioController()
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
			volumeBarBinding.sbVolume.min =
				carAudioController.getGroupMinVolume(AudioAttributes.USAGE_UNKNOWN)
			volumeBarBinding.sbVolume.max =
				carAudioController.getGroupMaxVolume(AudioAttributes.USAGE_UNKNOWN)
			val currentVolume = carAudioController.getGroupVolume(AudioAttributes.USAGE_UNKNOWN)
			volumeBarBinding.sbVolume.progress = currentVolume
			volumeBarBinding.sbVolume.setOnSeekBarChangeListener(seekBarChangeListener)
			volumeBarBinding.tvVolumeGroupLevel.text = currentVolume.toString()
		}
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		SystemUI.printLog(TAG, "onDetachedFromWindow:")
		if (attached) {
			volumeBarBinding.sbVolume.setOnSeekBarChangeListener(null)
			attached = false
		}
	}

}
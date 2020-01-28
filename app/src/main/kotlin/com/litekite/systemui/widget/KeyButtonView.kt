package com.litekite.systemui.widget

import android.app.Instrumentation
import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import androidx.appcompat.widget.AppCompatImageView
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import kotlin.math.abs

class KeyButtonView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

	private val tag = javaClass.simpleName
	private var touchDownX: Int = 0
	private var touchDownY: Int = 0
	private var longClicked: Boolean = false
	private var downTime: Long = 0
	private var onClickListener: OnClickListener? = null
	private val audioManager: AudioManager
	private val code: Int
	private val supportsLongPress: Boolean
	private val playSounds: Boolean
	private var contentDescriptionRes: Int = 0

	init {
		val ta = context.obtainStyledAttributes(
			attrs,
			R.styleable.KeyButtonView,
			defStyleAttr,
			0
		)
		code = ta.getInteger(R.styleable.KeyButtonView_keyCode, 0)
		supportsLongPress = ta.getBoolean(R.styleable.KeyButtonView_keyRepeat, false)
		playSounds = ta.getBoolean(R.styleable.KeyButtonView_playSound, false)
		val value = TypedValue()
		if (ta.getValue(R.styleable.KeyButtonView_android_contentDescription, value)) {
			contentDescriptionRes = value.resourceId
		}
		ta.recycle()
		isClickable = true
		audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
		forceHasOverlappingRendering(false)
	}

	private val checkLongPress = Runnable {
		if (isPressed) {
			if (isLongClickable) {
				performLongClick()
				longClicked = true
			} else if (supportsLongPress) {
				sendEvent(KeyEvent.ACTION_DOWN, KeyEvent.FLAG_LONG_PRESS)
				sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED)
				longClicked = true
			}
		}
	}

	override fun isClickable(): Boolean {
		return code != 0 || super.isClickable()
	}

	override fun setOnClickListener(onClickListener: OnClickListener?) {
		super.setOnClickListener(onClickListener)
		this.onClickListener = onClickListener
	}

	override fun onConfigurationChanged(newConfig: Configuration?) {
		super.onConfigurationChanged(newConfig)
		if (contentDescriptionRes != 0) {
			contentDescription = context.getString(contentDescriptionRes)
		}
	}

	override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo?) {
		super.onInitializeAccessibilityNodeInfo(info)
		if (code != 0) {
			info?.addAction(AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, null))
			if (supportsLongPress || isLongClickable) {
				info?.addAction(
					AccessibilityAction(AccessibilityNodeInfo.ACTION_LONG_CLICK, null)
				)
			}
		}
	}

	override fun onWindowVisibilityChanged(visibility: Int) {
		super.onWindowVisibilityChanged(visibility)
		if (visibility != View.VISIBLE) {
			jumpDrawablesToCurrentState()
		}
	}

	override fun onTouchEvent(event: MotionEvent?): Boolean {
		val x: Int
		val y: Int
		when (event?.action) {
			MotionEvent.ACTION_DOWN -> {
				downTime = SystemClock.uptimeMillis()
				isPressed = true
				longClicked = false
				// Use raw X and Y to detect gestures in case a parent changes the x and y values
				touchDownX = event.rawX.toInt()
				touchDownY = event.rawY.toInt()
				if (code != 0) {
					sendEvent(KeyEvent.ACTION_DOWN, 0, downTime)
				} else {
					// Provide the same haptic feedback that the system offers for virtual keys.
					performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
				}
				playSoundEffect(SoundEffectConstants.CLICK)
				removeCallbacks(checkLongPress)
				postDelayed(checkLongPress, ViewConfiguration.getLongPressTimeout().toLong())
			}
			MotionEvent.ACTION_MOVE -> {
				x = event.rawX.toInt()
				y = event.rawY.toInt()
				val exceededTouchSlopX: Boolean = abs(x - touchDownX) > getQuickStepTouchSlopPx()
				val exceededTouchSlopY: Boolean = abs(y - touchDownY) > getQuickStepTouchSlopPx()
				if (exceededTouchSlopX || exceededTouchSlopY) {
					// When quick step is enabled, prevent animating the ripple triggered by
					// setPressed and decide to run it on touch up
					isPressed = false
					removeCallbacks(checkLongPress)
				}
			}
			MotionEvent.ACTION_UP -> {
				val doIt = isPressed && !longClicked
				isPressed = false
				val doHapticFeedback: Boolean = SystemClock.uptimeMillis() - downTime > 150
				if (doHapticFeedback && !longClicked) {
					// Always send a release ourselves because it doesn't seem to be sent elsewhere
					// and it feels weird to sometimes get a release haptic and other times not.
					performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY_RELEASE)
				}
				if (code != 0) {
					if (doIt) {
						playSoundEffect(SoundEffectConstants.CLICK)
						sendEvent(KeyEvent.ACTION_UP, 0)
						sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED)
					} else {
						sendEvent(KeyEvent.ACTION_UP, KeyEvent.FLAG_CANCELED)
					}
				} else {
					// no key code, just a regular ImageView
					if (doIt && onClickListener != null) {
						onClickListener?.onClick(this)
						sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED)
					}
				}
				performClick()
				removeCallbacks(checkLongPress)
			}
			MotionEvent.ACTION_CANCEL -> {
				isPressed = false
				if (code != 0) {
					sendEvent(KeyEvent.ACTION_UP, KeyEvent.FLAG_CANCELED)
				}
				removeCallbacks(checkLongPress)
			}
		}
		return true
	}

	override fun performClick(): Boolean {
		SystemUI.printLog(tag, "key button performed click. code: $code")
		return super.performClick()
	}

	private fun getQuickStepTouchSlopPx(): Int {
		return context.resources.getDimensionPixelSize(R.dimen.quick_step_touch_slop)
	}

	override fun playSoundEffect(soundConstant: Int) {
		if (playSounds) {
			super.playSoundEffect(soundConstant)
		}
	}

	private fun sendEvent(action: Int, flags: Int) {
		sendEvent(action, flags, SystemClock.uptimeMillis())
	}

	private fun sendEvent(action: Int, flags: Int, whenDownTime: Long) {
		val repeatCount = if (flags and KeyEvent.FLAG_LONG_PRESS != 0) 1 else 0
		val event = KeyEvent(
			downTime,
			whenDownTime,
			action,
			code,
			repeatCount,
			0,
			KeyCharacterMap.VIRTUAL_KEYBOARD,
			0,
			flags or KeyEvent.FLAG_FROM_SYSTEM or KeyEvent.FLAG_VIRTUAL_HARD_KEY,
			InputDevice.SOURCE_KEYBOARD
		)
		Instrumentation().sendKeySync(event)
		// TODO this is an Hidden API. Try to check the above or use reflection or use custom sdk.
		//InputManager.getInstance().injectInputEvent(ev, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC)
	}

}
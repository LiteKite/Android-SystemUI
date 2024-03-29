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
package com.litekite.systemui.widget

import android.content.Context
import android.hardware.input.InputManager
import android.os.SystemClock
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import androidx.appcompat.widget.AppCompatImageButton
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import kotlin.math.abs

/**
 * @author Vignesh S
 * @version 1.0, 28/01/2020
 * @since 1.0
 */
class KeyButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr),
    View.OnLongClickListener,
    View.OnClickListener {

    companion object {
        val TAG = KeyButtonView::class.java.simpleName
    }

    val code: Int
    private var touchDownX: Int = 0
    private var touchDownY: Int = 0
    private var longClicked: Boolean = false
    private var downTime: Long = 0
    private val supportsLongPress: Boolean
    private val playSounds: Boolean

    private val performLongPress = Runnable {
        if (isPressed) {
            if (isLongClickable) {
                // Just an old-fashioned ImageView
                performLongClick()
                longClicked = true
            }
        }
    }

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
        ta.recycle()
        isClickable = true
        forceHasOverlappingRendering(false)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        // Registers short press events
        setOnClickListener(this)
        // Registers long press events
        setOnLongClickListener(this)
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo?) {
        super.onInitializeAccessibilityNodeInfo(info)
        if (code != 0) {
            info?.addAction(AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, null))
            if (isLongClickable) {
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
                removeCallbacks(performLongPress)
                postDelayed(performLongPress, ViewConfiguration.getLongPressTimeout().toLong())
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
                    removeCallbacks(performLongPress)
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
                        sendEvent(KeyEvent.ACTION_UP, 0)
                        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED)
                    } else {
                        sendEvent(KeyEvent.ACTION_UP, KeyEvent.FLAG_CANCELED)
                    }
                } else {
                    if (doIt) {
                        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED)
                    }
                }
                playSoundEffect(SoundEffectConstants.CLICK)
                performClick()
                removeCallbacks(performLongPress)
            }
            MotionEvent.ACTION_CANCEL -> {
                isPressed = false
                if (code != 0) {
                    sendEvent(KeyEvent.ACTION_UP, KeyEvent.FLAG_CANCELED)
                }
                removeCallbacks(performLongPress)
            }
        }
        return true
    }

    override fun isClickable(): Boolean {
        return code != 0 || super.isClickable()
    }

    override fun performClick(): Boolean {
        SystemUI.printLog(TAG, "performClick: code: $code")
        return super.performClick()
    }

    override fun onClick(v: View?) {
        SystemUI.printLog(TAG, "onClick: code: $code")
    }

    override fun isLongClickable(): Boolean {
        return supportsLongPress || super.isLongClickable()
    }

    override fun onLongClick(v: View?): Boolean {
        SystemUI.printLog(TAG, "onLongClick: code: $code")
        if (v != null) {
            if (supportsLongPress && code != 0) {
                sendEvent(KeyEvent.ACTION_DOWN, KeyEvent.FLAG_LONG_PRESS)
                sendEvent(KeyEvent.ACTION_UP, 0)
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED)
            } else {
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED)
            }
        }
        return true
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
        InputManager.getInstance().injectInputEvent(
            event,
            InputManager.INJECT_INPUT_EVENT_MODE_ASYNC
        )
    }
}

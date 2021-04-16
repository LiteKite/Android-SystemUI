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
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.WindowManager
import androidx.constraintlayout.motion.widget.MotionLayout
import com.litekite.systemui.R

/**
 * FlexMotionLayout that modifies height of the parent container
 * based on the motion animation
 *
 * @author Vignesh S
 * @version 1.0, 17/12/2020
 * @since 1.0
 */
class FlexMotionLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {

    private var attached: Boolean = false
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val transitionListener = object : TransitionListener {
        override fun onTransitionStarted(
            motionLayout: MotionLayout?,
            startId: Int,
            endId: Int
        ) {
            (parent as? ViewGroup)?.apply {
                layoutParams.height = maxHeight
                windowManager.updateViewLayout(this, layoutParams)
            }
        }

        override fun onTransitionChange(
            motionLayout: MotionLayout?,
            startId: Int,
            endId: Int,
            progress: Float
        ) {
        }

        override fun onTransitionCompleted(
            motionLayout: MotionLayout?,
            currentId: Int
        ) {
            if (currentId == R.id.start) {
                (parent as? ViewGroup)?.apply {
                    layoutParams.height = minHeight
                    windowManager.updateViewLayout(this, layoutParams)
                }
            }
        }

        override fun onTransitionTrigger(
            motionLayout: MotionLayout?,
            startId: Int,
            endId: Boolean,
            progress: Float
        ) {
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!attached) {
            attached = true
            addTransitionListener(transitionListener)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (attached) {
            removeTransitionListener(transitionListener)
            attached = false
        }
    }
}

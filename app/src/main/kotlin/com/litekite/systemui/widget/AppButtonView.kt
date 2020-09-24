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

package com.litekite.systemui.widget

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import com.litekite.systemui.R
import com.litekite.systemui.util.IntentUtil

/**
 * @author Vignesh S
 * @version 1.0, 30/08/2020
 * @since 1.0
 */
class AppButtonView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr), View.OnClickListener {

	companion object {
		val TAG = AppButtonView::class.java.simpleName
	}

	val action: String
	val pkg: String
	val component: String

	init {
		val ta = context.obtainStyledAttributes(
			attrs,
			R.styleable.AppButtonView,
			defStyleAttr,
			0
		)
		action = ta.getString(R.styleable.AppButtonView_action) ?: Intent.ACTION_MAIN
		pkg = ta.getString(R.styleable.AppButtonView_pkg) ?: ""
		component = ta.getString(R.styleable.AppButtonView_component) ?: ""
		ta.recycle()
		forceHasOverlappingRendering(false)
	}

	override fun onFinishInflate() {
		super.onFinishInflate()
		// Registers short press events
		setOnClickListener(this)
	}

	override fun onClick(v: View?) {
		if (v != null) {
			IntentUtil.launchActivity(context, action, pkg, component)
		}
	}

}
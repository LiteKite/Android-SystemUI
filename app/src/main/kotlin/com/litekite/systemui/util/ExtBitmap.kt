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

package com.litekite.systemui.util

import android.content.Context
import android.graphics.Bitmap
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlur

/**
 * Makes Gaussian Blur Effect with the provided Bitmap using Renderscript and ScriptIntrinsicBlur
 *
 * @author Vignesh S
 * @version 1.0, 24/12/2020
 * @since 1.0
 */
@Suppress("UNUSED")
fun Bitmap.blur(context: Context, radius: Float = 25F): Bitmap? {
	val bitmap = Bitmap.createBitmap(
		this.width,
		this.height,
		Bitmap.Config.ARGB_8888
	)
	val rs = RenderScript.create(context)
	val allocIn = Allocation.createFromBitmap(rs, this)
	val allocOut = Allocation.createFromBitmap(rs, bitmap)
	val blur = ScriptIntrinsicBlur.create(
		rs,
		Element.U8_4(rs)
	)
	blur.setInput(allocIn)
	blur.setRadius(radius)
	blur.forEach(allocOut)
	allocOut.copyTo(bitmap)
	rs.destroy()
	return bitmap
}
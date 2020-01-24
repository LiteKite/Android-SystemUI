package com.litekite.systemui.statusbar

import android.content.Context
import android.graphics.PixelFormat
import android.os.Binder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.litekite.systemui.R

/**
 * Encapsulates all logic for the status bar window state management.
 */
class StatusBarWindowManager(private val context: Context) {

	private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
	private lateinit var lp: WindowManager.LayoutParams
	private lateinit var statusBarView: View
	private var barHeight: Int? = null

	/**
	 * Adds the status bar view to the window manager.
	 *
	 * @param statusBarView The view to add.
	 */
	fun add(statusBarView: View) {
		val barHeight = context.resources.getDimensionPixelSize(R.dimen.status_bar_height)
		lp = WindowManager.LayoutParams(
			WindowManager.LayoutParams.MATCH_PARENT,
			barHeight,
			WindowManager.LayoutParams.TYPE_STATUS_BAR,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					or WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
					or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
					or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
			PixelFormat.TRANSLUCENT)
		lp.token = Binder()
		lp.gravity = Gravity.TOP
		lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
		lp.title = "StatusBar"
		lp.packageName = context.packageName
		this.statusBarView = statusBarView
		this.barHeight = barHeight
		windowManager.addView(this.statusBarView, lp)
	}

}
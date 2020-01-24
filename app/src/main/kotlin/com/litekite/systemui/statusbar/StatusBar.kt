package com.litekite.systemui.statusbar

import android.content.res.Configuration
import android.view.View
import android.widget.FrameLayout
import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import java.io.FileDescriptor
import java.io.PrintWriter

@Suppress("UNUSED")
class StatusBar : SystemUI() {

	private lateinit var statusBarWindowManager: StatusBarWindowManager
	private lateinit var statusBarWindow: FrameLayout
	private lateinit var statusBarView: View

	override fun start() {
		putComponent(javaClass, this)
		statusBarWindowManager = StatusBarWindowManager(context)
		makeStatusBarView()
		makeNavigationBarView()
	}

	private fun makeStatusBarView() {
		statusBarWindow =
			View.inflate(context, R.layout.super_status_bar, null) as FrameLayout
		statusBarView =
			View.inflate(context, R.layout.status_bar, statusBarWindow)
		statusBarWindowManager.add(statusBarWindow)
	}

	private fun makeNavigationBarView() {

	}

	override fun onBootCompleted() {

	}

	override fun onConfigurationChanged(newConfig: Configuration) {

	}

	override fun dump(fd: FileDescriptor, pw: PrintWriter, args: Array<String>) {

	}

}
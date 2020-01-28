package com.litekite.systemui.component

import com.litekite.systemui.R
import com.litekite.systemui.base.SystemUI
import java.io.FileDescriptor
import java.io.PrintWriter

@Suppress("UNUSED")
class SystemBars : SystemUI() {

	private val tag = javaClass.simpleName
	private lateinit var statusBar: SystemUI

	override fun start() {
		printLog(tag, "start")
		createStatusBarFromConfig()
	}

	private fun createStatusBarFromConfig() {
		val serviceComponent = context.resources.getString(R.string.config_statusBarComponent)
		statusBar = Class.forName(serviceComponent).newInstance() as SystemUI
		statusBar.context = context
		statusBar.components = components
		statusBar.start()
		printLog(tag, "started: " + statusBar.javaClass.simpleName)
	}

	override fun dump(fd: FileDescriptor, pw: PrintWriter, args: Array<String>) {
		statusBar.dump(fd, pw, args)
	}

}
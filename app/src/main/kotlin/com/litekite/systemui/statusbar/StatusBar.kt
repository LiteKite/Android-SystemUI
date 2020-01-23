package com.litekite.systemui.statusbar

import android.content.res.Configuration
import com.litekite.systemui.SystemUI
import java.io.FileDescriptor
import java.io.PrintWriter

class StatusBar: SystemUI() {

	override fun start() {
		putComponent(javaClass, this)
	}

	override fun onBootCompleted() {

	}

	override fun onConfigurationChanged(newConfig: Configuration) {

	}

	override fun dump(fd: FileDescriptor, pw: PrintWriter, args: Array<String>) {

	}

}
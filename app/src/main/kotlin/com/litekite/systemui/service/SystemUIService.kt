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

package com.litekite.systemui.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.litekite.systemui.app.SystemUIApp
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * @author Vignesh S
 * @version 1.0, 22/01/2020
 * @since 1.0
 */
class SystemUIService : Service() {

	override fun onCreate() {
		super.onCreate()
		(application as SystemUIApp).startServicesIfNeeded()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		return START_STICKY
	}

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}

	override fun dump(fd: FileDescriptor?, pw: PrintWriter?, args: Array<out String>?) {
		val services = (application as SystemUIApp).services
		if (args == null || args.isEmpty()) {
			services.forEach {
				pw?.println("dumping service: " + it.javaClass.name)
				it.dump(fd, pw, args)
			}
		} else {
			val svc = args[0]
			services.forEach {
				val name = it.javaClass.name
				if (name.endsWith(svc)) it.dump(fd, pw, args)
			}
		}
	}

}
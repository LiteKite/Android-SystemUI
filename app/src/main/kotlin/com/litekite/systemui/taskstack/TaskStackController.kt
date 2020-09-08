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

package com.litekite.systemui.taskstack

import android.app.ActivityManager
import android.app.WindowConfiguration
import com.android.systemui.shared.system.ActivityManagerWrapper
import com.android.systemui.shared.system.TaskStackChangeListener
import com.litekite.systemui.base.CallbackProvider
import com.litekite.systemui.base.SystemUI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Vignesh S
 * @version 1.0, 30/08/2020
 * @since 1.0
 */
@Singleton
class TaskStackController @Inject constructor() :
	CallbackProvider<TaskStackController.Callback> {

	companion object {
		val TAG = TaskStackController::class.java.simpleName
	}

	private val activityManagerWrapper = ActivityManagerWrapper.getInstance()
	override val callbacks: ArrayList<Callback> = ArrayList()

	private val taskStackChangeListener = object : TaskStackChangeListener() {

		override fun onTaskStackChanged() {
			super.onTaskStackChanged()
			val runningTask = activityManagerWrapper.runningTask
			val topActivity = runningTask.topActivity
			SystemUI.printLog(TAG, "onTaskStackChanged: ${topActivity.flattenToShortString()}")
			val activityType = WindowConfiguration.activityTypeToString(
				runningTask.configuration.windowConfiguration.activityType
			)
			SystemUI.printLog(TAG, "activityType: $activityType")
			notifyTaskStackChanged(runningTask)
		}

		override fun onTaskDisplayChanged(taskId: Int, newDisplayId: Int) {
			super.onTaskDisplayChanged(taskId, newDisplayId)
			SystemUI.printLog(TAG, "onTaskDisplayChanged:")
		}

	}

	init {
		registerListener()
	}

	private fun registerListener() {
		activityManagerWrapper.registerTaskStackListener(taskStackChangeListener)
	}

	private fun notifyTaskStackChanged(runningTaskInfo: ActivityManager.RunningTaskInfo) {
		callbacks.forEach { it.onTaskStackChanged(runningTaskInfo) }
	}

	/**
	 * A listener that will be notified whenever a change in activity task stack.
	 */
	interface Callback {

		fun onTaskStackChanged(runningTaskInfo: ActivityManager.RunningTaskInfo)

	}

}
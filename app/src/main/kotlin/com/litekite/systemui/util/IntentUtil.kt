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
package com.litekite.systemui.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.UserHandle

/**
 * @author Vignesh S
 * @version 1.0, 30/08/2020
 * @since 1.0
 */
object IntentUtil {

    fun launchActivity(
        context: Context,
        action: String = Intent.ACTION_MAIN,
        pkg: String = "",
        component: String = ""
    ) {
        val intent = Intent()
        if (action.isNotEmpty()) {
            intent.action = action
        }
        if (pkg.isNotEmpty()) {
            intent.`package` = pkg
        }
        if (component.isNotEmpty()) {
            intent.component = ComponentName.unflattenFromString(component)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivityAsUser(intent, UserHandle.CURRENT)
    }
}

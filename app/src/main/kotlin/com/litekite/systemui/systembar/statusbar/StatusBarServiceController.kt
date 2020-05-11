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

package com.litekite.systemui.systembar.statusbar

import android.content.ComponentName
import android.content.Context
import android.graphics.Rect
import android.hardware.biometrics.IBiometricServiceReceiverInternal
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.os.ServiceManager
import com.android.internal.statusbar.IStatusBar
import com.android.internal.statusbar.IStatusBarService
import com.android.internal.statusbar.StatusBarIcon
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class takes the functions from IStatusBar that come in on
 * binder pool threads and posts messages to get them onto the main
 * thread, and calls onto Callbacks.  It also takes care of
 * coalescing these calls so they don't stack up.  For the calls
 * are coalesced, note that they are all idempotent.
 *
 * @author Vignesh S
 * @version 1.0, 11/05/2020
 * @since 1.0
 */
@Singleton
class StatusBarServiceController @Inject constructor(private val context: Context) :
	IStatusBar.Stub() {

	private val callbacks: ArrayList<Callback> = ArrayList()

	init {
		connectIStatusBarService()
	}

	private fun connectIStatusBarService() {
		val statusBarService = IStatusBarService.Stub.asInterface(
			ServiceManager.getService(Context.STATUS_BAR_SERVICE)
		)
		try {
			statusBarService.registerStatusBar(this)
		} catch (e: RemoteException) {
			e.rethrowFromSystemServer()
		}
	}

	override fun hideRecentApps(triggeredFromAltTab: Boolean, triggeredFromHomeKey: Boolean) {}

	override fun animateExpandSettingsPanel(subPanel: String?) {}

	override fun startAssist(args: Bundle?) {}

	override fun showShutdownUi(isReboot: Boolean, reason: String?) {}

	override fun preloadRecentApps() {}

	override fun showPinningEscapeToast() {}

	override fun setImeWindowStatus(
		displayId: Int,
		token: IBinder?,
		vis: Int,
		backDisposition: Int,
		showImeSwitcher: Boolean
	) {
	}

	override fun onRecentsAnimationStateChanged(running: Boolean) {}

	override fun onCameraLaunchGestureDetected(source: Int) {}

	override fun onBiometricError(error: String?) {}

	override fun clickQsTile(tile: ComponentName?) {}

	override fun showWirelessChargingAnimation(batteryLevel: Int) {}

	override fun onBiometricAuthenticated(authenticated: Boolean, failureReason: String?) {}

	override fun onDisplayReady(displayId: Int) {}

	override fun topAppWindowChanged(displayId: Int, menuVisible: Boolean) {}

	override fun onProposedRotationChanged(rotation: Int, isValid: Boolean) {}

	override fun handleSystemKey(key: Int) {}

	override fun appTransitionCancelled(displayId: Int) {}

	override fun disable(displayId: Int, state1: Int, state2: Int) {}

	override fun removeIcon(slot: String?) {}

	override fun addQsTile(tile: ComponentName?) {}

	override fun showPictureInPictureMenu() {}

	override fun showGlobalActionsMenu() {}

	override fun setIcon(slot: String?, icon: StatusBarIcon?) {}

	override fun animateExpandNotificationsPanel() {}

	override fun showRecentApps(triggeredFromAltTab: Boolean) {}

	override fun toggleSplitScreen() {}

	override fun togglePanel() {}

	override fun appTransitionStarting(
		displayId: Int,
		statusBarAnimationsStartTime: Long,
		statusBarAnimationsDuration: Long
	) {
	}

	override fun onBiometricHelp(message: String?) {}

	override fun appTransitionPending(displayId: Int) {}

	override fun hideBiometricDialog() {}

	override fun dismissKeyboardShortcutsMenu() {}

	override fun setWindowState(display: Int, window: Int, state: Int) {}

	override fun animateCollapsePanels() {}

	override fun showBiometricDialog(
		bundle: Bundle?,
		receiver: IBiometricServiceReceiverInternal?,
		type: Int,
		requireConfirmation: Boolean,
		userId: Int
	) {
	}

	override fun appTransitionFinished(displayId: Int) {}

	override fun toggleKeyboardShortcutsMenu(deviceId: Int) {}

	override fun remQsTile(tile: ComponentName?) {}

	override fun showPinningEnterExitToast(entering: Boolean) {}

	override fun setTopAppHidesStatusBar(hidesStatusBar: Boolean) {}

	override fun showAssistDisclosure() {}

	override fun toggleRecentApps() {}

	override fun showScreenPinningRequest(taskId: Int) {}

	override fun setSystemUiVisibility(
		displayId: Int,
		vis: Int,
		fullscreenStackVis: Int,
		dockedStackVis: Int,
		mask: Int,
		fullscreenBounds: Rect?,
		dockedBounds: Rect?,
		navbarColorManagedByIme: Boolean
	) {
	}

	override fun cancelPreloadRecentApps() {}

	fun addCallback(cb: Callback) {
		callbacks.add(cb)
	}

	fun removeCallback(cb: Callback) {
		callbacks.remove(cb)
	}

	interface Callback {

		fun setWindowState(display: Int, window: Int, state: Int) {}

	}

}
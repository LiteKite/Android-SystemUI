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

import android.app.StatusBarManager
import android.app.StatusBarManager.WindowVisibleState
import android.content.ComponentName
import android.content.Context
import android.graphics.Rect
import android.hardware.biometrics.IBiometricServiceReceiverInternal
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.os.ServiceManager
import android.view.KeyEvent
import com.android.internal.statusbar.IStatusBar
import com.android.internal.statusbar.IStatusBarService
import com.android.internal.statusbar.StatusBarIcon
import com.litekite.systemui.base.SystemUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
class StatusBarServiceController @Inject constructor() : IStatusBar.Stub(), CoroutineScope {

	companion object {
		val TAG = StatusBarServiceController::class.java.simpleName
	}

	private enum class WindowType(@StatusBarManager.WindowType private val window: Int) {

		WINDOW_STATUS_BAR(StatusBarManager.WINDOW_STATUS_BAR),
		WINDOW_NAVIGATION_BAR(StatusBarManager.WINDOW_NAVIGATION_BAR);

		companion object {

			fun valueOf(@StatusBarManager.WindowType window: Int) =
				values().first { it.window == window }

			fun windowTypeString(@StatusBarManager.WindowType window: Int) = valueOf(window).name

		}

	}

	private val callbacks: ArrayList<Callback> = ArrayList()

	override val coroutineContext = Dispatchers.Main

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

	fun addCallback(cb: Callback) {
		callbacks.add(cb)
	}

	fun removeCallback(cb: Callback) {
		callbacks.remove(cb)
	}

	private fun getSystemKeyString(key: Int) = KeyEvent.keyCodeToString(key)

	private fun getWindowStateString(@WindowVisibleState state: Int) =
		StatusBarManager.windowStateToString(state)

	private fun getWindowTypeString(@StatusBarManager.WindowType window: Int) =
		WindowType.windowTypeString(window)

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

	override fun handleSystemKey(key: Int) {
		SystemUI.printLog(TAG, "handleSystemKey: ${getSystemKeyString(key)}")
		launch { callbacks.forEach { it.handleSystemKey(key) } }
	}

	override fun appTransitionCancelled(displayId: Int) {
		SystemUI.printLog(TAG, "appTransitionCancelled: displayId: $displayId")
		launch { callbacks.forEach { it.appTransitionCancelled(displayId) } }
	}

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
		startTime: Long,
		duration: Long
	) {
		SystemUI.printLog(
			TAG,
			"appTransitionStarting: displayId: $displayId startTime: $startTime duration: $duration"
		)
		launch {
			callbacks.forEach {
				it.appTransitionStarting(displayId, startTime, duration, false)
			}
		}
	}

	override fun onBiometricHelp(message: String?) {}

	override fun appTransitionPending(displayId: Int) {
		SystemUI.printLog(TAG, "appTransitionPending: displayId: $displayId")
		launch { callbacks.forEach { it.appTransitionPending(displayId, false) } }
	}

	override fun hideBiometricDialog() {}

	override fun dismissKeyboardShortcutsMenu() {}

	override fun setWindowState(displayId: Int, window: Int, state: Int) {
		SystemUI.printLog(
			TAG,
			"setWindowState: displayId: $displayId window: ${getWindowTypeString(window)} "
					+ "state: ${getWindowStateString(state)}"
		)
		launch { callbacks.forEach { it.setWindowState(displayId, window, state) } }
	}

	override fun animateCollapsePanels() {}

	override fun showBiometricDialog(
		bundle: Bundle?,
		receiver: IBiometricServiceReceiverInternal?,
		type: Int,
		requireConfirmation: Boolean,
		userId: Int
	) {
	}

	override fun appTransitionFinished(displayId: Int) {
		SystemUI.printLog(TAG, "appTransitionFinished: displayId: $displayId")
		launch { callbacks.forEach { it.appTransitionFinished(displayId) } }
	}

	override fun toggleKeyboardShortcutsMenu(deviceId: Int) {}

	override fun remQsTile(tile: ComponentName?) {}

	override fun showPinningEnterExitToast(entering: Boolean) {}

	override fun setTopAppHidesStatusBar(hidesStatusBar: Boolean) {
		SystemUI.printLog(TAG, "setTopAppHidesStatusBar: hidesStatusBar: $hidesStatusBar")
		launch { callbacks.forEach { it.setTopAppHidesStatusBar(hidesStatusBar) } }
	}

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
		navBarColorManagedByIme: Boolean
	) {
		SystemUI.printLog(TAG, buildString {
			append("setSystemUiVisibility ")
			append("displayId=$displayId vis=${Integer.toHexString(vis)} ")
			append("mask=${Integer.toHexString(mask)} ")
			append("fullscreenBounds=${fullscreenBounds.toString()} ")
			append("dockedBounds=${dockedBounds.toString()} ")
			append("navBarColorManagedByIme=$navBarColorManagedByIme ")
		})
		launch {
			callbacks.forEach {
				it.setSystemUiVisibility(
					displayId,
					vis,
					fullscreenStackVis,
					dockedStackVis,
					mask,
					fullscreenBounds,
					dockedBounds,
					navBarColorManagedByIme
				)
			}
		}
	}

	override fun cancelPreloadRecentApps() {}

	interface Callback {

		/**
		 * Called for system navigation gestures.
		 */
		fun handleSystemKey(key: Int) {}

		/**
		 * Called to notify System UI that an application transition is canceled.
		 * @see IStatusBar.appTransitionCancelled(int).
		 *
		 * @param displayId The id of the display to notify.
		 */
		fun appTransitionCancelled(displayId: Int) {}

		/**
		 * Called to notify System UI that an application transition is pending.
		 * @see IStatusBar.appTransitionPending(int).
		 *
		 * @param displayId The id of the display to notify.
		 * @param forced {@code true} to force transition pending.
		 */
		fun appTransitionPending(displayId: Int, forced: Boolean) {}

		/**
		 * Called to notify System UI that an application transition is starting.
		 * @see IStatusBar.appTransitionStarting(int, long, long).
		 *
		 * @param displayId The id of the display to notify.
		 * @param startTime Transition start time.
		 * @param duration Transition duration.
		 * @param forced {@code true} to force transition pending.
		 */
		fun appTransitionStarting(
			displayId: Int,
			startTime: Long,
			duration: Long,
			forced: Boolean
		) {
		}

		/**
		 * Called to notify System UI that an application transition is finished.
		 * @see IStatusBar.appTransitionFinished(int)
		 *
		 * @param displayId The id of the display to notify.
		 */
		fun appTransitionFinished(displayId: Int) {}

		/**
		 * Called to notify window state changes.
		 * @see IStatusBar.setWindowState(int, int, int)
		 *
		 * @param displayId The id of the display to notify.
		 * @param window Window type. It should be one of {@link StatusBarManager#WINDOW_STATUS_BAR}
		 *               or {@link StatusBarManager#WINDOW_NAVIGATION_BAR}
		 * @param state Window visible state.
		 */
		fun setWindowState(
			displayId: Int,
			@StatusBarManager.WindowType window: Int,
			@WindowVisibleState state: Int
		) {
		}

		fun setTopAppHidesStatusBar(hidesStatusBar: Boolean) {}

		/**
		 * Called to notify visibility flag changes.
		 * @see IStatusBar.setSystemUiVisibility(int, int, int, int, int, Rect, Rect).
		 *
		 * @param displayId The id of the display to notify.
		 * @param vis The visibility flags except SYSTEM_UI_FLAG_LIGHT_STATUS_BAR which will
		 *            be reported separately in fullscreenStackVis and dockedStackVis.
		 * @param fullscreenStackVis The flags which only apply in the region of the fullscreen
		 *                           stack, which is currently only SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.
		 * @param dockedStackVis The flags that only apply in the region of the docked stack, which
		 *                       is currently only SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.
		 * @param mask Which flags to change.
		 * @param fullscreenBounds The current bounds of the fullscreen stack, in screen
		 *                              coordinates.
		 * @param dockedBounds The current bounds of the docked stack, in screen coordinates.
		 * @param navBarColorManagedByIme {@code true} if navigation bar color is managed by IME.
		 */
		fun setSystemUiVisibility(
			displayId: Int,
			vis: Int,
			fullscreenStackVis: Int,
			dockedStackVis: Int,
			mask: Int,
			fullscreenBounds: Rect?,
			dockedBounds: Rect?,
			navBarColorManagedByIme: Boolean
		) {
		}

	}

}
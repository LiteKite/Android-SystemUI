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

package com.litekite.systemui.fragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentController
import androidx.fragment.app.FragmentHostCallback
import androidx.fragment.app.FragmentManager
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 * @author Vignesh S
 * @version 1.0, 24/09/2020
 * @since 1.0
 */
class FragmentHostProvider(private val context: Context, private val rootView: View) {

	private val handler = Handler(Looper.getMainLooper())
	private val callbacks: HashMap<String, ArrayList<FragmentCallback>?> = HashMap()
	private lateinit var fragmentController: FragmentController

	private val fragmentLifeCycleCallback = object : FragmentManager.FragmentLifecycleCallbacks() {

		override fun onFragmentViewCreated(
			fragmentManager: FragmentManager,
			fragment: Fragment,
			view: View,
			savedInstanceState: Bundle?
		) {
			super.onFragmentViewCreated(fragmentManager, fragment, view, savedInstanceState)
			fragmentViewCreated(fragment)
		}

		override fun onFragmentViewDestroyed(fragmentManager: FragmentManager, fragment: Fragment) {
			super.onFragmentViewDestroyed(fragmentManager, fragment)
			fragmentViewDestroyed(fragment)
		}

	}

	init {
		createFragmentController(null)
	}

	private fun createFragmentController(savedState: Parcelable?) {
		fragmentController = FragmentController.createController(HostCallbacks())
		fragmentController.attachHost(null)
		// Registers fragment lifecycle callbacks
		getFragmentManager().registerFragmentLifecycleCallbacks(
			fragmentLifeCycleCallback,
			true
		)
		// Restores saved state if there is any...
		if (savedState != null) {
			fragmentController.restoreSaveState(savedState)
		}
		// For now just keep all fragments in the resumed state.
		fragmentController.dispatchCreate()
		fragmentController.dispatchStart()
		fragmentController.dispatchResume()
	}

	/**
	 * Called when the configuration changes
	 */
	fun configChanged(newConfig: Configuration) {
		fragmentController.dispatchConfigurationChanged(newConfig)
	}

	private fun getFragmentManager() = fragmentController.supportFragmentManager

	fun addTagCallback(tag: String, fragmentCallback: FragmentCallback): FragmentHostProvider {
		var fragmentCallbacks = callbacks[tag]
		if (fragmentCallbacks == null) {
			fragmentCallbacks = ArrayList()
			callbacks[tag] = fragmentCallbacks
		}
		fragmentCallbacks.add(fragmentCallback)
		val fragment = getFragmentManager().findFragmentByTag(tag)
		if (fragment != null && fragment.view != null) {
			fragmentCallback.onFragmentViewCreated(tag, fragment)
		}
		return this
	}

	fun removeTagCallback(tag: String, fragmentCallback: FragmentCallback) {
		val fragmentCallbacks = callbacks[tag]
		if (fragmentCallbacks != null && fragmentCallbacks.remove(fragmentCallback) &&
			fragmentCallbacks.size == 0
		) {
			callbacks.remove(tag)
		}
	}

	private fun fragmentViewCreated(fragment: Fragment) {
		val tag = fragment.tag ?: ""
		callbacks[tag]?.forEach { it.onFragmentViewCreated(tag, fragment) }
	}

	private fun <T : View> findViewById(id: Int): T = rootView.findViewById(id)

	private fun fragmentViewDestroyed(fragment: Fragment) {
		val tag = fragment.tag ?: ""
		callbacks[tag]?.forEach { it.onFragmentViewDestroyed(tag, fragment) }
	}

	fun reloadFragments() {
		// Save the old state.
		val savedState = destroyFragmentController()
		// Generate a new fragment host and restore its state.
		createFragmentController(savedState)
	}

	internal fun destroyFragmentController(): Parcelable? {
		fragmentController.dispatchPause()
		val savedState = fragmentController.saveAllState()
		fragmentController.dispatchStop()
		fragmentController.dispatchDestroy()
		getFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifeCycleCallback)
		return savedState
	}

	private fun dump(writer: PrintWriter) {
		writer.println("fragmentController: $fragmentController")
	}

	inner class HostCallbacks : FragmentHostCallback<FragmentHostProvider>
		(context, handler, 0) {

		override fun onGetHost(): FragmentHostProvider? {
			return this@FragmentHostProvider
		}

		override fun onGetLayoutInflater(): LayoutInflater {
			return LayoutInflater.from(context)
		}

		override fun onHasWindowAnimations(): Boolean {
			return false
		}

		override fun onFindViewById(id: Int): View? {
			return findViewById(id)
		}

		override fun onDump(
			prefix: String,
			fileDescriptor: FileDescriptor?,
			writer: PrintWriter,
			args: Array<out String>?
		) {
			super.onDump(prefix, fileDescriptor, writer, args)
			dump(writer)
		}

	}

	interface FragmentCallback {

		fun onFragmentViewCreated(tag: String, fragment: Fragment)

		fun onFragmentViewDestroyed(tag: String, fragment: Fragment)

	}

}
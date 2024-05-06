package com.dynamic.island.oasis.dynamic_island.listeners

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Handler
import android.util.Log
import com.dynamic.island.oasis.dynamic_island.Logs
import com.google.firebase.crashlytics.FirebaseCrashlytics


class ExceptionListener(
    private val clipboard: ClipboardManager,
    private val showToast: (String) -> Unit
) {


    init {
        val uncaughtExceptionHandler: Thread.UncaughtExceptionHandler =
            Thread.UncaughtExceptionHandler { t, e ->
                FirebaseCrashlytics.getInstance().recordException(Exception(e))

                val stacktrace = Log.getStackTraceString(e)

                copyToClipboard(stacktrace)
                showToast("Dynamic Oasis crashed, logs copied to clipboard. Send it to support and issue will be fixed")
                Logs.log("AccessibilityException; ${stacktrace}")

                Runtime.getRuntime().exit(0)
            }
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)
    }


    private fun copyToClipboard(text: String) {
        val clip = ClipData.newPlainText("Crash Log", text)
        clipboard.setPrimaryClip(clip)
    }
}
package com.lyra.aura

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.util.Date

@HiltAndroidApp
class LyraAuraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupCrashHandler()
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = File(filesDir, "last_crash.txt")
                file.writeText(buildString {
                    appendLine("=== Lyra Aura Crash Log ===")
                    appendLine("Time: ${Date()}")
                    appendLine("Thread: ${thread.name}")
                    appendLine("Android: ${android.os.Build.VERSION.SDK_INT} (${android.os.Build.VERSION.RELEASE})")
                    appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                    appendLine("")
                    appendLine(throwable.stackTraceToString())
                })
            } catch (_: Throwable) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}

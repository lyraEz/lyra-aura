package com.lyra.aura

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltAndroidApp
class LyraAuraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupCrashHandler()
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val report = buildCrashReport(thread, throwable)

            // Best-effort: never let the logger create a second crash.
            runCatching { writeCrashReportEverywhere(report) }
                .onFailure { Log.e("LyraAuraCrash", "Failed to write crash report", it) }

            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun buildCrashReport(thread: Thread, throwable: Throwable): String {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US).format(Date())

        return buildString {
            appendLine("=== Lyra Aura Crash Log ===")
            appendLine("Time: $time")
            appendLine("Thread: ${thread.name}")
            appendLine("Package: $packageName")
            appendLine("App version: ${runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull() ?: "unknown"}")
            appendLine("Android SDK: ${Build.VERSION.SDK_INT}")
            appendLine("Android Release: ${Build.VERSION.RELEASE}")
            appendLine("Manufacturer: ${Build.MANUFACTURER}")
            appendLine("Model: ${Build.MODEL}")
            appendLine("Device: ${Build.DEVICE}")
            appendLine("Board: ${Build.BOARD}")
            appendLine("")
            appendLine("=== Exception ===")
            appendLine(throwable.stackTraceToString())
            appendLine("")
            appendLine("=== Cause chain ===")
            var cause = throwable.cause
            var index = 1
            while (cause != null) {
                appendLine("Cause #$index: ${cause::class.java.name}: ${cause.message}")
                appendLine(cause.stackTraceToString())
                cause = cause.cause
                index++
            }
        }
    }

    private fun writeCrashReportEverywhere(report: String) {
        // 1) Private internal app storage.
        writeTextSafely(File(filesDir, "last_crash.txt"), report)

        // 2) App-specific external storage. This usually appears under:
        // Android/data/<package>/files/LyraAuraLogs/last_crash.txt
        getExternalFilesDir(null)?.let { externalRoot ->
            writeTextSafely(File(externalRoot, "LyraAuraLogs/last_crash.txt"), report)
        }

        // 3) Public Downloads folder.
        // Android 10+ uses MediaStore, because raw writes to /Download are blocked by scoped storage.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeCrashReportToDownloadsMediaStore(report)
        } else {
            @Suppress("DEPRECATION")
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            writeTextSafely(File(downloads, "LyraAura/last_crash.txt"), report)
        }
    }

    private fun writeTextSafely(file: File, text: String) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(text)
        }.onFailure {
            Log.e("LyraAuraCrash", "Failed writing ${file.absolutePath}", it)
        }
    }

    private fun writeCrashReportToDownloadsMediaStore(report: String) {
        runCatching {
            val fileName = "last_crash.txt"
            val relativePath = Environment.DIRECTORY_DOWNLOADS + "/LyraAura"

            // Remove older version created by this app so the filename stays simple.
            val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            contentResolver.delete(
                collection,
                "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
                arrayOf(fileName, "$relativePath/")
            )

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val uri = contentResolver.insert(collection, values) ?: return@runCatching

            contentResolver.openOutputStream(uri, "w")?.use { output ->
                output.write(report.toByteArray(Charsets.UTF_8))
            }

            ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }.also { doneValues ->
                contentResolver.update(uri, doneValues, null, null)
            }
        }.onFailure {
            Log.e("LyraAuraCrash", "Failed writing crash report to Downloads", it)
        }
    }
}

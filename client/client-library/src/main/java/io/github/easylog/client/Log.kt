package io.github.easylog.client

import android.content.Context
import io.github.easylog.model.LogEntry
import io.github.easylog.model.LogLevel
import java.time.ZonedDateTime
import java.util.UUID

class Log {

    companion object {

        private lateinit var tag: String
        private var enabled: Boolean = true

        private var sessionId: String = UUID.randomUUID().toString()

        @JvmStatic
        fun init(context: Context, logTag: String, enable: Boolean) {
            tag = logTag
            enabled = enable
        }

        @JvmStatic
        fun i(message: String) {
            if (!enabled) {
                return
            }

            android.util.Log.i(tag, message)
            LogQueue.add(buildSaveLogRequest(LogLevel.INFO, tag, message))
        }

        @JvmStatic
        fun d(message: String) {
            if (!enabled) {
                return
            }

            android.util.Log.d(tag, message)
            LogQueue.add(buildSaveLogRequest(LogLevel.DEBUG, tag, message))
        }

        @JvmStatic
        fun w(message: String) {
            if (!enabled) {
                return
            }

            android.util.Log.w(tag, message)
            LogQueue.add(buildSaveLogRequest(LogLevel.WARN, tag, message))
        }

        @JvmStatic
        fun e(message: String, throwable: Throwable?) {
            if (!enabled) {
                return
            }

            android.util.Log.e(tag, message, throwable)
            LogQueue.add(buildSaveLogRequest(LogLevel.INFO, tag, message))
        }

        fun buildSaveLogRequest(logLevel: LogLevel, logTag: String, message: String): LogEntry {
            val request = LogEntry()
            request.correlationId = UUID.randomUUID().toString()
            request.message = message
            request.tag = logTag
            request.logLevel = logLevel
            request.sessionId = sessionId
            request.timestamp = ZonedDateTime.now()
            return request
        }
    }
}
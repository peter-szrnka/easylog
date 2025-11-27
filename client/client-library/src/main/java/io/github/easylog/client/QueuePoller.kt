package io.github.easylog.client

import android.util.Log
import io.github.easylog.model.LogEntry
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class QueuePoller {

    companion object {

        private val executor = Executors.newScheduledThreadPool(10) { r ->
            Thread(r, "QueuePoller-Thread").apply { isDaemon = false }
        }

        private var scheduledTask: ScheduledFuture<*>? = null

        fun queryStart(config: EasyLogClientConfig) {
            if (scheduledTask != null && !scheduledTask!!.isCancelled) return

            Log.d(config.logTag, "EasyLog queuePoller initialized")

            val queuePoller = Runnable {
                if (!EasyLogState.pollEnabled) return@Runnable

                try {
                    val messages = ArrayList<LogEntry>()
                    while (!LogQueue.getQueue().isEmpty()) {
                        val message = LogQueue.getQueue().poll()
                        if (message != null) {
                            messages.add(message)
                        }
                    }

                    if (messages.isNotEmpty()) {
                        LogSenderClient.sendLog(config, messages)
                    }
                    Thread.sleep(1000)
                } catch (t: Throwable) {
                    Log.e("TracelyApp", "Error in QueuePoller: ${t.message}", t)
                    EasyLogState.pollEnabled = false
                }
            }

            executor.scheduleWithFixedDelay(queuePoller, 0, 1, TimeUnit.SECONDS)
        }

        fun queryStop() {
            scheduledTask?.cancel(true)
            scheduledTask = null
        }
    }
}
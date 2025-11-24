package io.github.easylog.client

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import io.github.easylog.model.LogEntry
import io.github.easylog.model.SaveLogRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LogSenderClient {

    companion object {

        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(ZonedDateTime::class.java, JsonSerializer<ZonedDateTime> { src, _, _ ->
                JsonPrimitive(src.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
            })
            .create()

        private val client = OkHttpClient()

        fun sendLog(config: EasyLogClientConfig, messages: List<LogEntry>) {
            val url = "http://${EasyLogState.serverHost}/api/log"
            val request = SaveLogRequest()
            request.entries = messages

            val json = gson.toJson(request)
            val body = json.toRequestBody("application/json".toMediaType())

            val req = Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w(config.logTag, "Failed to send logs: ${resp.code}")
                }
            }
        }
    }
}

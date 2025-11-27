package io.github.easylog.client

interface LogCallback {
    fun log(tag: String, log: String)
}
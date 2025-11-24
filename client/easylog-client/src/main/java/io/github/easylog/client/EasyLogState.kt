package io.github.easylog.client

class EasyLogState {

    companion object {
        var serverFound: Boolean = false
        var serverHost: String? = null
        var metadata: Map<String, String>? = null
        var pollEnabled: Boolean = false
        var discoveryStarted: Boolean = false
    }
}
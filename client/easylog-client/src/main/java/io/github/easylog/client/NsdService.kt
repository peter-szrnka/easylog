package io.github.easylog.client

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Handler
import android.os.Looper

class NsdService(context: Context, private val config: EasyLogClientConfig) {

    private val logCallback = config.logCallback
    private val logTag = config.logTag

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    // Instantiate a new DiscoveryListener
    private val discoveryListener = object : NsdManager.DiscoveryListener {

        override fun onDiscoveryStarted(regType: String) {
            logCallback?.log(logTag,"Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            nsdManager.resolveService(service, resolveListener)
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            logCallback?.log(logTag,"service lost: $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            logCallback?.log(logTag,"Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            logCallback?.log(logTag,"Discovery failed: Error code:$errorCode")
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            logCallback?.log(logTag,"Discovery failed: Error code:$errorCode")
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            logCallback?.log(logTag,"Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            logCallback?.log(logTag,"Resolve Succeeded. $serviceInfo")
            EasyLogState.serverHost = "${serviceInfo.host.hostAddress}:${serviceInfo.port}"
            EasyLogState.metadata = serviceInfo.attributes.entries.associate { it.key to it.value.toString(Charsets.UTF_8) }

            serviceInfo.attributes.entries.forEach {
                logCallback?.log(logTag,"Txt record: ${it.key} = ${it.value.toString(Charsets.UTF_8)}")
            }
            logCallback?.log(logTag,"Log server found at ${EasyLogState.serverHost} with txtRecord: ${EasyLogState.metadata}")
            EasyLogState.pollEnabled = true
            QueuePoller.queryStart(config)
            stopDiscover()
            EasyLogState.serverFound = true
        }
    }

    fun discoverServer() {
        Handler(Looper.getMainLooper()).post {
            nsdManager.discoverServices(config.serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            EasyLogState.discoveryStarted = true
        }
    }

    fun stopDiscover() {
        QueuePoller.queryStop()
        if (EasyLogState.discoveryStarted) {
            nsdManager.stopServiceDiscovery(discoveryListener)
        }
        EasyLogState.discoveryStarted = false
    }
}

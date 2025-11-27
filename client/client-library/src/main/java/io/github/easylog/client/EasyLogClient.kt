package io.github.easylog.client

import android.content.Context
import android.net.wifi.WifiManager

class EasyLogClient(context: Context, private val config: EasyLogClientConfig) {

    private val nsdService: NsdService = NsdService(context, config)
    private val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    //private val lock = wifi.createMulticastLock("mdnsLock")

    fun start() {
        if (config.fixAddress != null) {
            EasyLogState.serverHost = config.fixAddress
            EasyLogState.serverFound = true
        } else {
            //lock.setReferenceCounted(true)
            //lock.acquire()
            nsdService.discoverServer()
        }
    }

    fun resume() {
        EasyLogState.pollEnabled = true
    }

    fun pause() {
        EasyLogState.pollEnabled = false
        if (config.fixAddress != null) {
            return
        }
        //lock.release()
        //nsdService.stopDiscover()

    }

    fun shutdown() {
        EasyLogState.pollEnabled = false
        if (config.fixAddress != null) {
            return
        }
        //lock.release()
        nsdService.stopDiscover()
    }
}
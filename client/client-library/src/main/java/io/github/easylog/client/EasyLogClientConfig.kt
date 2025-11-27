package io.github.easylog.client

/**
 * @author Peter Szrnka
 */
data class EasyLogClientConfig(
    val serviceType: String,
    val serviceName: String,
    val fixAddress: String? = null,
    val logTag: String,
    val logCallback: LogCallback? = null
)
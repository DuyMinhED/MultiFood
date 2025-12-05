package com.baonhutminh.multifood.common

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retry
import java.util.concurrent.CancellationException

/**
 * Retry configuration cho exponential backoff
 */
data class RetryConfig(
    val maxRetries: Int = 3,
    val initialDelayMillis: Long = 1000L,
    val maxDelayMillis: Long = 10000L,
    val multiplier: Double = 2.0,
    val retryOn: (Throwable) -> Boolean = { it !is CancellationException }
)

/**
 * Retry một suspend function với exponential backoff
 * 
 * @param config Cấu hình retry
 * @param block Function cần retry
 * @return Kết quả của block hoặc throw exception sau khi hết retry
 */
suspend fun <T> retryWithBackoff(
    config: RetryConfig = RetryConfig(),
    block: suspend () -> T
): T {
    var currentDelay = config.initialDelayMillis
    var lastException: Throwable? = null
    
    repeat(config.maxRetries) { attempt ->
        try {
            return block()
        } catch (e: Throwable) {
            lastException = e
            
            // Không retry nếu là CancellationException hoặc không match retryOn condition
            if (!config.retryOn(e)) {
                throw e
            }
            
            // Nếu đây là lần retry cuối, throw exception
            if (attempt == config.maxRetries - 1) {
                throw e
            }
            
            // Delay với exponential backoff
            delay(currentDelay)
            currentDelay = (currentDelay * config.multiplier).toLong().coerceAtMost(config.maxDelayMillis)
        }
    }
    
    // Không bao giờ đến đây, nhưng để compiler happy
    throw lastException ?: IllegalStateException("Retry failed")
}

/**
 * Retry một Flow với exponential backoff
 * 
 * @param config Cấu hình retry
 * @return Flow với retry logic
 */
fun <T> Flow<T>.retryWithBackoff(
    config: RetryConfig = RetryConfig()
): Flow<T> {
    return this.retry(
        retries = config.maxRetries.toLong(),
        predicate = { exception ->
            config.retryOn(exception)
        }
    )
}

/**
 * Retry configuration mặc định cho network operations
 */
val DEFAULT_NETWORK_RETRY_CONFIG = RetryConfig(
    maxRetries = 3,
    initialDelayMillis = 1000L,
    maxDelayMillis = 10000L,
    multiplier = 2.0,
    retryOn = { exception ->
        // Retry cho network errors, không retry cho CancellationException
        exception !is CancellationException && (
            exception is java.net.UnknownHostException ||
            exception is java.net.SocketTimeoutException ||
            exception is java.io.IOException ||
            exception.message?.contains("network", ignoreCase = true) == true ||
            exception.message?.contains("timeout", ignoreCase = true) == true
        )
    }
)

/**
 * Retry configuration cho upload operations (nhiều retry hơn)
 */
val DEFAULT_UPLOAD_RETRY_CONFIG = RetryConfig(
    maxRetries = 5,
    initialDelayMillis = 2000L,
    maxDelayMillis = 30000L,
    multiplier = 2.0,
    retryOn = { exception ->
        exception !is CancellationException && (
            exception is java.net.UnknownHostException ||
            exception is java.net.SocketTimeoutException ||
            exception is java.io.IOException ||
            exception.message?.contains("network", ignoreCase = true) == true ||
            exception.message?.contains("timeout", ignoreCase = true) == true ||
            exception.message?.contains("upload", ignoreCase = true) == true
        )
    }
)




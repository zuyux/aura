package io.aura.android.data.network

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

sealed class NetworkError(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    class Offline(cause: Throwable? = null) : NetworkError("Network unavailable", cause)
    class Timeout(cause: Throwable? = null) : NetworkError("Network request timed out", cause)
    class Http(val statusCode: Int, cause: Throwable? = null) : NetworkError("HTTP $statusCode", cause)
    class Parsing(cause: Throwable? = null) : NetworkError("Network response could not be parsed", cause)
    class Unknown(cause: Throwable? = null) : NetworkError("Network request failed", cause)
}

suspend inline fun <T> mapNetworkErrors(block: suspend () -> T): T =
    try {
        block()
    } catch (error: Throwable) {
        throw error.toNetworkError()
    }

fun Throwable.toNetworkError(): NetworkError =
    when (this) {
        is NetworkError -> this
        is UnknownHostException -> NetworkError.Offline(this)
        is SocketTimeoutException -> NetworkError.Timeout(this)
        is HttpException -> NetworkError.Http(code(), this)
        is SerializationException -> NetworkError.Parsing(this)
        is IOException -> NetworkError.Offline(this)
        else -> NetworkError.Unknown(this)
    }

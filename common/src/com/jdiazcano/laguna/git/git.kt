package com.jdiazcano.laguna.git

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory

//expect fun createHttpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig>

expect object Git {
    fun clone(url: String, path: String)
}

class GitException(override val message: String): Exception(message)

fun Int.ifError(thrown: () -> Exception): Int {
    if (this != 0) {
        thrown()
    }
    return this
}
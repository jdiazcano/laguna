package com.jdiazcano.laguna

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
    Laguna().main(args)
}

//actual fun createHttpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig> = CIO
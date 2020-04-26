package com.jdiazcano.laguna

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.curl.Curl

fun main(args: Array<String>) {
    Laguna().main(args)
}

//actual fun createHttpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig> = Curl


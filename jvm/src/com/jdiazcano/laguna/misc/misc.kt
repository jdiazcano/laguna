package com.jdiazcano.laguna.misc

actual fun env(name: String): String? = System.getenv(name)
actual fun <T> runBlocking(block: suspend () -> T): T {
    TODO("Not yet implemented")
}

actual fun pwd(): String {
    return java.io.File(".").absolutePath
}
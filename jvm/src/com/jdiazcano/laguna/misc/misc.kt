package com.jdiazcano.laguna.misc

import kotlin.system.exitProcess

actual fun env(name: String): String? = System.getenv(name)
actual fun <T> runBlocking(block: suspend () -> T): T {
    TODO("Not yet implemented")
}

actual fun pwd(): String {
    return java.io.File(".").absolutePath
}

actual fun exit(exitCode: ExitCode) {
    exitProcess(exitCode.code)
}
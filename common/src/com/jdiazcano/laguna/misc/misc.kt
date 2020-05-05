package com.jdiazcano.laguna.misc

expect fun env(name: String): String?

expect fun <T> runBlocking(block: suspend () -> T): T

expect fun pwd(): String
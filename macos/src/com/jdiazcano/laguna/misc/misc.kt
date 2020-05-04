package com.jdiazcano.laguna.misc

import kotlinx.cinterop.toKString
import platform.posix.getenv

actual fun env(name: String) = getenv(name)?.toKString()
actual fun <T> runBlocking(block: suspend () -> T): T = kotlinx.coroutines.runBlocking { block() }
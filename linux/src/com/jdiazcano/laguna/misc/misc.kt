package com.jdiazcano.laguna.misc

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.PATH_MAX
import platform.posix.getcwd
import platform.posix.getenv

actual fun env(name: String) = getenv(name)?.toKString()
actual fun <T> runBlocking(block: suspend () -> T): T = runBlocking { block() }

actual fun pwd(): String = memScoped {
    val realpath = allocArray<ByteVar>(PATH_MAX + 1)
    getcwd(realpath, PATH_MAX)
    return realpath.toKString()
}
package com.jdiazcano.laguna.misc

import kotlinx.cinterop.*
import platform.posix.PATH_MAX
import platform.posix.getcwd
import platform.posix.getenv
import kotlin.system.exitProcess

actual fun env(name: String) = getenv(name)?.toKString()
actual fun <T> runBlocking(block: suspend () -> T): T = kotlinx.coroutines.runBlocking { block() }

actual fun pwd(): String = memScoped {
    val realpath = allocArray<ByteVar>(PATH_MAX + 1)
    getcwd(realpath, PATH_MAX)
    return realpath.toKString()
}

inline fun <reified T : CPointed> NativePlacement.allocValuePointedTo(obj: () -> CPointer<T>): CPointerVar<T> {
    val pointer = allocPointerTo<T>()
    pointer.value = obj()
    return pointer
}

actual fun system(command: String) = platform.posix.system(command)

actual fun exit(exitCode: ExitCode, message: String?) {
    message?.let { println(it) }
    exitProcess(exitCode.code)
}
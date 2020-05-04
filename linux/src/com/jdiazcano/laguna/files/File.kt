package com.jdiazcano.laguna.files

import kotlinx.cinterop.*
import platform.posix.S_IFDIR
import platform.posix.stat
import platform.posix.system

actual class File actual constructor(
        actual val path: String
) {
    actual fun resolve(folder: String): File {
        return File(path.trimEnd('/') + "/" + folder)
    }

    actual fun resolve(file: File): File {
        return File(path.trimEnd('/') + "/" + file.path)
    }

    actual fun mkdirs(): Boolean {
        return system("mkdir -p '$path'") == 0
    }

    actual fun isDirectory(): Boolean = memScoped {
        val sb = alloc<stat>()
        stat(path, sb.ptr) == 0 && sb.st_mode.toInt() and S_IFDIR != 0
    }

    actual fun remove(mode: RemoveMode): Boolean {
        TODO("Not yet implemented")
    }

    actual fun read(): String {
        TODO("Not yet implemented")
    }

    actual fun listFiles(): List<File> {
        TODO("Not yet implemented")
    }

    actual fun exists(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun write(string: String) {
    }
}
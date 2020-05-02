package com.jdiazcano.laguna.files

import kotlinx.cinterop.*
import platform.posix.*

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
        val exitCode = when (mode) {
            RemoveMode.Default -> remove(path)
            RemoveMode.Recursive -> memScoped {
                val unlinkRemove = allocValuePointedTo {
                    staticCFunction { fpath: CPointer<ByteVar>?, _: CPointer<stat>?, _: Int, _: CPointer<FTW>? ->
                        remove(fpath?.toKString())
                    }
                }
                nftw(path, unlinkRemove.value, 64, FTW_DEPTH or FTW_PHYS)
            }
        }

        return exitCode == 0
    }
}

inline fun <reified T : CPointed> NativePlacement.allocValuePointedTo(obj: () -> CPointer<T>): CPointerVar<T> {
    val pointer = allocPointerTo<T>()
    pointer.value = obj()
    return pointer
}
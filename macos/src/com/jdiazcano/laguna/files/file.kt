package com.jdiazcano.laguna.files

import com.jdiazcano.laguna.misc.allocValuePointedTo
import kotlinx.cinterop.*
import platform.posix.*

actual class File actual constructor(
    actual val path: String
) {
    actual companion object {
        actual val pathSeparator = "/"
    }

    actual val absolutePath by lazy {
        memScoped {
            val realpath = allocArray<ByteVar>(PATH_MAX + 1)
            realpath(path, realpath)
            realpath.toKString()
        }
    }

    actual val size by lazy {
        val file = fopen(path, "rb") ?: throw IllegalStateException("cannot open input file $path")
        file.use {
            fseek(file, 0, SEEK_END)
            ftell(file)
        }
    }

    actual fun resolve(name: String): File {
        return if (path.isNotEmpty()) {
            File(path.removeSuffix(pathSeparator) + pathSeparator + name.removePrefix(pathSeparator))
        } else {
            File(name)
        }
    }

    actual fun resolve(file: File): File {
        return if (path.isNotEmpty()) {
            File(path.removeSuffix(pathSeparator) + pathSeparator + file.path.removePrefix(pathSeparator))
        } else {
            File(file.path)
        }
    }

    actual fun mkdirs(): Boolean {
        return system("mkdir -p '$path'") == 0
    }

    actual fun isDirectory(): Boolean = memScoped {
        val sb = alloc<stat>()
        val stat = stat(path, sb.ptr)
        val statIsDir = (sb.st_mode.toInt() and S_IFDIR) != 0
        stat == 0 && statIsDir
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

    actual fun exists(): Boolean = memScoped {
        val sb = alloc<stat>()
        val stat = stat(path, sb.ptr)
        stat == 0
    }

    actual fun files(): List<File> {
        if (!exists()) {
            throw IllegalArgumentException("'$path' does not exist.")
        }

        if (!isDirectory()) {
            throw IllegalArgumentException("'$path' is not a directory.")
        }

        val files = arrayListOf<File>()
        val dir = opendir(path)
        if (dir?.pointed != null) {
            var pathPointer = readdir(dir)
            while (pathPointer?.pointed != null) {
                val pathString = pathPointer.pointed.d_name.toKString()
                if (pathString != "." && pathString != "..") {
                    files.add(resolve(pathString))
                }
                pathPointer = readdir(dir)
            }
        } else {
            throw IllegalArgumentException("Could not open '$path'")
        }

        return files
    }

    actual fun read(): String {
        val file = fopen(path, "r") ?: throw IllegalStateException("cannot open input file $path")

        return buildString {
            file.use {
                memScoped {
                    val bufferLength = 64 * 1024
                    val buffer = allocArray<ByteVar>(bufferLength)

                    while (true) {
                        val nextLine = fgets(buffer, bufferLength, file)?.toKString() ?: break

                        append(nextLine)
                    }
                }
            }
        }
    }

    actual fun readBytes(): ByteArray {
        val file = fopen(path, "rb") ?: throw IllegalStateException("cannot open input file $path")
        val length = size
        val bytes = ByteArray(length.toInt())
        bytes.usePinned {
            fread(it.addressOf(0), length.convert(), 1.convert(), file)
        }
        return bytes
    }

    actual fun write(string: String) {
        val file = fopen(path, "w") ?: throw IllegalStateException("Can't write into '$path'")

        file.use {
            fputs(string, file)
        }
    }

    actual fun write(bytes: ByteArray) {
        val file = fopen(path, "wb") ?: throw IllegalStateException("Can't write into '$path'")

        file.use {
            bytes.usePinned {
                fwrite(it.addressOf(0), bytes.size.convert(), 1.convert(), file)
            }
        }
    }

}

fun <T> CPointer<FILE>.use(block: () -> T) : T{
    try {
        return block()
    } finally {
        fclose(this)
    }
}
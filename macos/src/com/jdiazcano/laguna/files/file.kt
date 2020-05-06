package com.jdiazcano.laguna.files

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
            println("Path: ${realpath.toKString()}")
            realpath.toKString()
        }
    }

    actual fun resolve(folder: String): File {
        return File(path.removeSuffix(pathSeparator) + pathSeparator + folder)
    }

    actual fun resolve(file: File): File {
        return File(path.removeSuffix(pathSeparator) + pathSeparator + file.path)
    }

    actual fun mkdirs(): Boolean {
        return system("mkdir -p '$path'") == 0
    }

    actual fun isDirectory(): Boolean = memScoped {
        val sb = alloc<stat>()
        val stat = stat(path, sb.ptr)
        val statIsDir = sb.st_mode.toInt() and S_IFDIR != 0
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

    actual fun listFiles(): List<File> {
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
                    files.add(File(pathString))
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

    actual fun write(string: String) {
        val file = fopen(path, "w") ?: throw IllegalStateException("Can't write into '$path'")

        file.use {
            fputs(string, file)
        }
    }

}

fun CPointer<FILE>.use(block: () -> Unit) {
    try {
        block()
    } finally {
        fclose(this)
    }
}
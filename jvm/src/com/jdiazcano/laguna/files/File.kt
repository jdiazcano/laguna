package com.jdiazcano.laguna.files

actual class File actual constructor(path: String) {
    actual val path: String
        get() = TODO("Not yet implemented")

    actual fun resolve(folder: String): File {
        TODO("Not yet implemented")
    }

    actual fun resolve(file: File): File {
        TODO("Not yet implemented")
    }

    actual fun mkdirs(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun isDirectory(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun remove(mode: RemoveMode): Boolean {
        TODO("Not yet implemented")
    }

}
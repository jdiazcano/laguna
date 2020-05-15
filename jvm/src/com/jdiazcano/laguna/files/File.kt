package com.jdiazcano.laguna.files

actual class File actual constructor(path: String) {
    actual val path: String
        get() = TODO("Not yet implemented")

    actual val absolutePath by lazy { "" }

    actual fun resolve(name: String): File {
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

    actual companion object {
        actual val pathSeparator: String
            get() = TODO("Not yet implemented")
    }

}
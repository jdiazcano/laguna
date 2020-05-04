package com.jdiazcano.laguna.git

import com.jdiazcano.laguna.files.File
import java.io.Closeable

actual class GitRepository actual constructor(file: File): Closeable {
    actual fun open() {
    }

    actual fun pull(): Int {
        TODO("Not yet implemented")
    }

    actual fun fetch(): Int {
        TODO("Not yet implemented")
    }

    actual fun reset(mode: GitResetMode): Int {
        TODO("Not yet implemented")
    }

    actual fun checkout(identifier: String): Int {
        TODO("Not yet implemented")
    }

    actual override fun close() {
        TODO("Not yet implemented")
    }

    actual fun add(paths: Array<String>): Int {
        TODO("Not yet implemented")
    }

    actual fun clean() {
    }

}

actual object Git {
    actual fun clone(url: String, file: File): GitRepository {
        TODO("Not yet implemented")
    }
}
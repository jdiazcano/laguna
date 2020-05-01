package com.jdiazcano.laguna.git

import java.io.Closeable

actual class GitRepository actual constructor(val path: String): Closeable {
    actual fun open() {
    }

    actual fun clone(url: String): Int {
        TODO("Not yet implemented")
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

}
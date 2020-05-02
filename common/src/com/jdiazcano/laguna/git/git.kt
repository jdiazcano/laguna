package com.jdiazcano.laguna.git

import com.jdiazcano.laguna.files.File
import io.ktor.utils.io.core.Closeable

expect object Git {
    fun clone(url: String, file: File): GitRepository
}

expect class GitRepository(file: File): Closeable {
    fun open()
    override fun close()
    fun pull(): Int
    fun fetch(): Int
    fun reset(mode: GitResetMode): Int
    fun checkout(identifier: String): Int
    fun add(paths: Array<String>): Int
}

enum class GitResetMode(val index: UInt) {
    SOFT(1U),
    MIXED(2U),
    HARD(3U),
    ;
}

class GitException(override val message: String): Exception(message)

fun Int.ifError(thrown: () -> Exception): Int {
    if (this != 0) {
        thrown()
    }
    return this
}
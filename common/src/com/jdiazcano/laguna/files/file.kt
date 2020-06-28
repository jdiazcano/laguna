package com.jdiazcano.laguna.files

import com.jdiazcano.laguna.misc.debug

expect class File(path: String) {
    companion object {
        val pathSeparator: String
    }

    val path: String
    val absolutePath: String
    val size: Long

    fun resolve(name: String): File
    fun resolve(file: File): File
    fun mkdirs(): Boolean
    fun isDirectory(): Boolean
    fun remove(mode: RemoveMode = RemoveMode.Default): Boolean
    fun read(): String
    fun readBytes(): ByteArray
    fun files(): List<File>
    fun exists(): Boolean
    fun write(string: String)
    fun write(bytes: ByteArray)
}

enum class RemoveMode {
    Default,
    Recursive,
    ;
}

suspend fun File.forEachDirectoryRecursive(block: suspend (File) -> Unit) {
    if (isDirectory()) {
        files().forEach {
            if (it.isDirectory()) {
                block(it)
                it.forEachDirectoryRecursive(block)
            }
        }
    }
}

suspend fun <T> File.forEachFileRecursive(filter: (File) -> Boolean = { true }, block: suspend (File) -> T) {
    debug("Foreach recursive: $absolutePath")
    if (isDirectory()) {
        files().filter(filter).forEach {
            debug("Executing function for: ${it.absolutePath}")
            it.forEachFileRecursive(filter, block)
        }
    } else {
        block(this)
    }
}
package com.jdiazcano.laguna.files

expect class File(path: String) {
    companion object {
        val pathSeparator: String
    }

    val path: String
    val absolutePath: String

    fun resolve(name: String): File
    fun resolve(file: File): File
    fun mkdirs(): Boolean
    fun isDirectory(): Boolean
    fun remove(mode: RemoveMode = RemoveMode.Default): Boolean
    fun read(): String
    fun files(): List<File>
    fun exists(): Boolean
    fun write(string: String)
}

enum class RemoveMode {
    Default,
    Recursive,
    ;
}
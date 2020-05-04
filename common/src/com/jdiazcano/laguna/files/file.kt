package com.jdiazcano.laguna.files

expect class File(path: String) {
    val path: String

    fun resolve(folder: String): File
    fun resolve(file: File): File
    fun mkdirs(): Boolean
    fun isDirectory(): Boolean
    fun remove(mode: RemoveMode = RemoveMode.Default): Boolean
    fun read(): String
    fun listFiles(): List<File>
    fun exists(): Boolean
    fun write(string: String)
}

enum class RemoveMode {
    Default,
    Recursive,
    ;
}
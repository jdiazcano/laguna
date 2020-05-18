package com.jdiazcano.laguna.files

class StringOutput(private val content: String): Output {
    override fun write(file: File) {
        file.write(content)
    }
}

class BinaryOutput(private val content: ByteArray): Output {
    override fun write(file: File) {
        file.write(content)
    }
}

interface Output {
    fun write(file: File)
}
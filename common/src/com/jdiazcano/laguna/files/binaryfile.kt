package com.jdiazcano.laguna.files

fun File.isBinary(): Boolean {
    return absolutePath.endsWith(".jar")
}
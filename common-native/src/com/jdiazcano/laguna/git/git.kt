package com.jdiazcano.laguna.git

expect object NativeGit {
    fun clone(url: String, path: String)
}

actual object Git {
    actual fun clone(url: String, path: String) = NativeGit.clone(url, path)
}
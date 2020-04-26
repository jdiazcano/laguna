package com.jdiazcano.laguna.git

import kotlinx.cinterop.*
import libgit2.*

actual object Git {
    actual fun clone(url: String, path: String) {
        memScoped {
            val loc = allocPointerTo<git_repository>()
            val exit = git_clone(loc.ptr, url, path, null).ifError {
                throw GitException(run {
                    giterr_last()!!.pointed.message!!.toKString()
                })
            }
            git_repository_free(loc.value!!)
        }
    }
}
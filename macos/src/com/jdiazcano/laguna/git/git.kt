package com.jdiazcano.laguna.git

import cnames.structs.git_repository
import com.jdiazcano.laguna.Laguna
import com.jdiazcano.laguna.files.File
import io.ktor.utils.io.core.Closeable
import kotlinx.cinterop.*
import libgit2.*
import platform.posix.free
import platform.posix.remove

fun main(args: Array<String>) {
    Laguna().main(args)
}

actual object Git {
    actual fun clone(url: String, file: File): GitRepository {
        memScoped {
            val loc = allocPointerTo<git_repository>()
            git_clone(loc.ptr, url, file.path, null).throwGitErrorIfNeeded()
            git_repository_free(loc.value)
            return GitRepository(file)
        }
    }
}

actual class GitRepository actual constructor(private val file: File): Closeable {
    private lateinit var repository: CPointerVar<git_repository>

    init {
        git_libgit2_init()
        printVersion()
    }

    private fun printVersion() {
        memScoped {
            val major = alloc<IntVar>()
            val minor = alloc<IntVar>()
            val rev = alloc<IntVar>()
            git_libgit2_version(major.ptr, minor.ptr, rev.ptr)
            println("Major: ${major.value}, minor: ${minor.value}, rev: ${rev.value}")
        }
    }

    actual fun open() {
        repository = nativeHeap.allocPointerTo()
        git_repository_open(repository.ptr, file.path).throwGitErrorIfNeeded()
    }

    actual fun pull(): Int {
        TODO("Not yet implemented")
    }

    actual fun fetch(): Int {
        TODO("Not yet implemented")
    }

    actual fun add(paths: Array<String>): Int {
        TODO()
    }

    actual fun reset(mode: GitResetMode): Int {
        TODO()
    }

    actual fun checkout(identifier: String): Int {
        TODO()
    }

    actual override fun close() {
        git_repository_free(repository.value)
        nativeHeap.free(repository)
        git_libgit2_shutdown()
    }
}

private fun Int.throwGitErrorIfNeeded(): Int {
    ifError {
        throw GitException(giterr_last()!!.pointed.message!!.toKString())
    }

    return this
}
package com.jdiazcano.laguna.git

import cnames.structs.git_repository
import com.jdiazcano.laguna.Laguna
import io.ktor.utils.io.core.Closeable
import kotlinx.cinterop.*
import libgit2.*
import platform.posix.free
import platform.posix.remove

fun main(args: Array<String>) {
    Laguna().main(args)
}

actual class GitRepository actual constructor(val path: String): Closeable {
    private lateinit var repository: CPointerVar<git_repository>

    init {
        git_libgit2_init()
    }

    actual fun open() {
        repository = nativeHeap.allocPointerTo()
        git_repository_open(repository.ptr, path).throwGitErrorIfNeeded()
    }

    actual fun clone(url: String): Int {
        memScoped {
            val loc = allocPointerTo<git_repository>()
            val exit = git_clone(loc.ptr, url, path, null)
            open()
            return exit
        }
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

private fun removeUntracked(): CPointer<CFunction<(CPointer<ByteVar>?, UInt, COpaquePointer?) -> Int>> {
    return staticCFunction { statusPath: CPointer<ByteVar>?, flags: UInt, payload: COpaquePointer? ->
        val path = statusPath!!.toKString()
        println("Maybe removing: $path")
        val removing = flags and GIT_STATUS_INDEX_NEW == 1U
        println("Removing? $removing")
        if (removing) {
            val castedPayload = payload!!.reinterpret<ByteVar>()
            remove(castedPayload.toKString() + path)
//            println(payload.rawValue.toLong())
//            println("Casted payload!: $castedPayload")
//            git_index_remove_bypath(castedPayload, statusPath!!.toKString())
        }
        0
    }
}

private fun printer(): CPointer<CFunction<(CPointer<ByteVar>?, CPointer<ByteVar>?, COpaquePointer?) -> Int>> {
    return staticCFunction { statusPath: CPointer<ByteVar>?, _: CPointer<ByteVar>?, payload: COpaquePointer? ->
        println("Adding: ${statusPath?.toKString()}")
        0
    }
}

fun git_reset(repo: CValuesRef<git_repository>, target: CValuesRef<git_object>, resetMode: GitResetMode): Int {
    val opts = cValue<git_checkout_options> {
        version = GIT_CHECKOUT_OPTIONS_VERSION.toUInt()
        checkout_strategy = GIT_CHECKOUT_FORCE
    }
    val exit = git_reset(repo, target, resetMode.index, opts)
    free(opts)
    return exit
}

typealias GitStatusCBFunction = CFunction<(CPointer<ByteVar>?, UInt, COpaquePointer?) -> Int>
typealias AddAllFunction = CFunction<(CPointer<ByteVar>?, CPointer<ByteVar>?, COpaquePointer?) -> Int>
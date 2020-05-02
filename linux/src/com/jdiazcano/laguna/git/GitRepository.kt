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

actual class GitRepository actual constructor(val file: File): Closeable {
    private lateinit var repository: CPointerVar<git_repository>

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
        memScoped {
            val repoIndex = allocPointerTo<git_index>()
            git_repository_index(repoIndex.ptr, repository.value).throwGitErrorIfNeeded()
            println(repoIndex.ptr.toLong())
            println(repoIndex.ptr.toLong().toCPointer<git_index>())
            val files = cValue<git_strarray> {
                count = paths.size.toULong()
                strings = paths.toCStringArray(memScope)
            }

            val callback = allocPointerTo<AddAllFunction>()
            callback.value = printer()

            val exit = git_index_add_all(repoIndex.value, files, GIT_INDEX_ADD_FORCE, callback.value, null).throwGitErrorIfNeeded()
            git_index_free(repoIndex.value)
            return exit
        }
    }

    actual fun reset(mode: GitResetMode): Int {
        memScoped {
            val head = allocPointerTo<git_object>()
            git_revparse_single(head.ptr, repository.value!!, "HEAD")
            git_reset(repository.value!!, head.value!!, mode).throwGitErrorIfNeeded()

            val repo = this@GitRepository
            val pointer = repo.file.path.cstr
            val callback = allocPointerTo<GitStatusCBFunction>()
            callback.value = removeUntracked()

            val exit =  git_status_foreach(repository.value!!, callback.value, pointer)
            free(callback.value)

            return 0
        }
    }

    actual fun checkout(identifier: String): Int {
        return if (identifier == "HEAD") {
            val opts = cValue<git_checkout_opts> {
                version = GIT_CHECKOUT_OPTS_VERSION.toUInt()
                checkout_strategy = GIT_CHECKOUT_FORCE
            }
            git_checkout_head(repository.value!!, opts).throwGitErrorIfNeeded()
        } else {
            TODO()
        }
    }

    actual override fun close() {
        git_repository_free(repository.value)
        nativeHeap.free(repository)
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
    return git_reset(repo, target, resetMode.index)
}

typealias GitStatusCBFunction = CFunction<(CPointer<ByteVar>?, UInt, COpaquePointer?) -> Int>
typealias AddAllFunction = CFunction<(CPointer<ByteVar>?, CPointer<ByteVar>?, COpaquePointer?) -> Int>

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
package com.jdiazcano.laguna.git

import cnames.structs.git_repository
import com.jdiazcano.laguna.Laguna
import com.jdiazcano.laguna.files.File
import com.jdiazcano.laguna.files.allocValuePointedTo
import io.ktor.utils.io.core.Closeable
import kotlinx.cinterop.*
import libgit2.*

fun main(args: Array<String>) {
    Laguna().main(args)
}

actual object Git {
    init {
        git_libgit2_init()
    }

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
        fetch()
        return memScoped {
            val newTarget = allocPointerTo<git_reference>()
            val targetReference = allocPointerTo<git_reference>()
            git_repository_head(targetReference.ptr, repository.value).throwGitErrorIfNeeded()

            val treeish = allocPointerTo<git_object>()
            val options = cValue<git_checkout_options> {
                version = GIT_CHECKOUT_OPTIONS_VERSION.toUInt()
                checkout_strategy = GIT_CHECKOUT_SAFE
            }
            git_revparse_single(treeish.ptr, repository.value, "origin/master").throwGitErrorIfNeeded()
            git_checkout_tree(repository.value, treeish.value, options).throwGitErrorIfNeeded()
            val exit = git_reference_set_target(newTarget.ptr, targetReference.value, git_object_id(treeish.value), null).throwGitErrorIfNeeded()

            git_reference_free(newTarget.value)
            git_reference_free(targetReference.value)
            git_object_free(treeish.value)

            exit
        }
    }

    actual fun fetch(): Int {
        return memScoped {
            val remote = allocPointerTo<git_remote>()
            git_remote_lookup(remote.ptr, repository.value, "origin").throwGitErrorIfNeeded()
            val exit = git_remote_fetch(remote.value, null, null, null).throwGitErrorIfNeeded()
            git_remote_free(remote.value)
            exit
        }
    }

    actual fun add(paths: Array<String>): Int {
        TODO()
    }

    actual fun reset(mode: GitResetMode): Int {
        return memScoped {
            val head = allocPointerTo<git_object>()
            git_revparse_single(head.ptr, repository.value!!, "HEAD")
            git_reset(repository.value!!, head.value!!, mode.index, null).throwGitErrorIfNeeded()
        }
    }

    actual fun checkout(identifier: String): Int {
        return memScoped {
            val treeish = allocPointerTo<git_object>()
            val options = cValue<git_checkout_options> {
                version = GIT_CHECKOUT_OPTIONS_VERSION.toUInt()
                checkout_strategy = GIT_CHECKOUT_SAFE
            }
            git_revparse_single(treeish.ptr, repository.value, identifier).throwGitErrorIfNeeded()
            git_checkout_tree(repository.value, treeish.value, options).throwGitErrorIfNeeded()

            val exit = git_repository_set_head(repository.value, "refs/heads/$identifier").throwGitErrorIfNeeded()
            git_object_free(treeish.value)
            exit
        }
    }

    actual override fun close() {
        git_repository_free(repository.value)
        nativeHeap.free(repository)
        git_libgit2_shutdown()
    }

    actual fun clean() {
        reset(GitResetMode.HARD)

        return memScoped {
            val payload = StableRef.create(file)
            val payloadPtr = payload.asCPointer()
            val removeUnindexed = allocValuePointedTo {
                staticCFunction { path: CPointer<ByteVar>?, _: UInt, payload: COpaquePointer? ->
                    val payloadFile = payload!!.asStableRef<File>().get()
                    payloadFile.resolve(path!!.toKString()).remove()
                    0
                }
            }
            git_status_foreach(repository.value, removeUnindexed.value, payloadPtr).throwGitErrorIfNeeded()
            payload.dispose()
        }
    }
}

private fun Int.throwGitErrorIfNeeded(): Int {
    ifError {
        throw GitException(giterr_last()!!.pointed.message!!.toKString())
    }

    return this
}
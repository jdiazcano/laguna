package com.jdiazcano.laguna.misc

expect fun env(name: String): String?

expect fun <T> runBlocking(block: suspend () -> T): T

expect fun pwd(): String

expect fun exit(exitCode: ExitCode, message: String? = null)

enum class ExitCode(val code: Int) {
    ALL_GOOD(0),
    FOLDER_ALREADY_EXISTS(1),
    GIT_ERROR(2),
    MISSING_VARIABLE_VALUE(3),
    ;
}
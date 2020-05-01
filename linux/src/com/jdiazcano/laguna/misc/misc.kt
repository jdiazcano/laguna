package com.jdiazcano.laguna.misc

import kotlinx.cinterop.toKString
import platform.posix.getenv

actual fun env(name: String) = getenv(name)?.toKString()
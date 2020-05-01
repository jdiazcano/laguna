package com.jdiazcano.laguna.misc

actual fun env(name: String): String? = System.getenv(name)
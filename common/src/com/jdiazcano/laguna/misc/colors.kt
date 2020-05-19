package com.jdiazcano.laguna.misc

internal val String.red   get() = "\u001b[31m$this"
internal val String.grn   get() = "\u001b[32m$this"
internal val String.yel   get() = "\u001b[33m$this"
internal val String.blu   get() = "\u001b[34m$this"
internal val String.mag   get() = "\u001b[35m$this"
internal val String.cyn   get() = "\u001b[36m$this"
internal val String.wht   get() = "\u001b[37m$this"
internal val String.reset get() = "$this\\u001b[0m"

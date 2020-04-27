package com.jdiazcano.laguna

import com.jdiazcano.laguna.git.Git

fun main(args: Array<String>) {
    Laguna().main(args)

    Git.clone("https://github.com/jdiazcano/laguna-templates.git", "randomtestxd")
}
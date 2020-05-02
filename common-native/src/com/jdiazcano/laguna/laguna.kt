package com.jdiazcano.laguna

import com.jdiazcano.laguna.files.File
import com.jdiazcano.laguna.files.RemoveMode
import com.jdiazcano.laguna.git.Git
import com.jdiazcano.laguna.git.GitRepository

fun main(args: Array<String>) {
//    Laguna().main(args)
    val file = File("/tmp/randomdir")
    println("Removed? ${file.remove(RemoveMode.Recursive)}")

    val repo = Git.clone("https://github.com/jdiazcano/laguna-templates.git", File("/tmp/laguna-templates"))

}
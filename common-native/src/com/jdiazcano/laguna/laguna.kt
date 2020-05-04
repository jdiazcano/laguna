package com.jdiazcano.laguna

import com.jdiazcano.laguna.files.File
import com.jdiazcano.laguna.files.RemoveMode
import com.jdiazcano.laguna.git.Git
import com.jdiazcano.laguna.git.GitRepository

fun main(args: Array<String>) {
//    Laguna().main(args)
    val file = File("/tmp/randomdir")
    println("Removed? ${file.remove(RemoveMode.Recursive)}")

    val repoFolder = File("/tmp/laguna-templates")
    println("Is dir? ${repoFolder.isDirectory()}")
    val repo = if (!repoFolder.isDirectory()) {
        Git.clone("https://github.com/jdiazcano/laguna-templates.git", repoFolder)
    } else {
        GitRepository(repoFolder)
    }
    repo.open()
    repo.clean()
    repo.fetch()
    repo.checkout("master")
    repo.pull()
    repo.close()
}
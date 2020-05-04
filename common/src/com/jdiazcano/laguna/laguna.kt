package com.jdiazcano.laguna

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.jdiazcano.laguna.files.File
import com.jdiazcano.laguna.git.Git
import com.jdiazcano.laguna.git.GitRepository
import com.jdiazcano.laguna.misc.runBlocking
import com.soywiz.korte.Templates

class Laguna: CliktCommand() {
    val templateName: String by argument()
    val projectName: String by option("-n", "--name").required()
    val templateArguments: Map<String, String> by argument().multiple().transformAll { items ->
        (items + "name=$projectName").map { it.split("=", limit = 2) }.associate { it[0] to it[1] }
    }

    override fun run() {
        val repoFolder = File("/tmp/laguna-templates/$templateName")
        val outputFolder = File("/tmp/$projectName").apply { mkdirs() }
        val renderer = Templates(GitTemplateProvider(repoFolder))
        runBlocking {
            forEachDirectoryRecursive(repoFolder) {
                File(it.path.replace("/tmp/laguna-templates/$templateName", "/tmp/$projectName")).mkdirs()
            }
            forEachFileRecursive(repoFolder) {
                outputFolder.resolve(it.path.replace("/tmp/laguna-templates/$templateName", "")).write(renderer.render(it.path, templateArguments))
            }
        }
    }
}

suspend fun forEachDirectoryRecursive(file: File, block: suspend (File) -> Unit) {
    if (file.isDirectory()) {
        file.listFiles().forEach {
            if (it.isDirectory()) {
                forEachFileRecursive(it, block)
            }
        }
    }
}

suspend fun forEachFileRecursive(file: File, block: suspend (File) -> Unit) {
    if (file.isDirectory()) {
        file.listFiles().forEach { forEachFileRecursive(it, block) }
    } else {
        block(file)
    }
}

fun main(args: Array<String>) {
    Laguna().main(args)
//    val file = File("/tmp/randomdir")
//    println("Removed? ${file.remove(RemoveMode.Recursive)}")
//
//    val repoFolder = File("/tmp/laguna-templates")
//    prepareRepository(repoFolder)
//
//    val renderer = Templates(GitTemplateProvider(repoFolder))
//    runBlocking {
//        println(renderer.render("test-template/build.gradle.kts"))
//    }
}

private fun prepareRepository(repoFolder: File) {
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
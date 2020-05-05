package com.jdiazcano.laguna

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import com.github.ajalt.clikt.parameters.options.default
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
    val outputFolder: String by option("-o", "--output").default(".", "Current folder")

    override fun run() {
        val templateFolder = File("/tmp/laguna-templates/$templateName")
        val outputFolder = File(outputFolder).resolve(projectName).apply { mkdirs() }
        val renderer = Templates(GitTemplateProvider(templateFolder))
        runBlocking {
            forEachDirectoryRecursive(templateFolder) {
                File(it.path.replace(templateFolder.path, projectName)).mkdirs()
            }
            forEachFileRecursive(templateFolder) {
                // The file is relative to the repository folder
                val relativeFile = it.path.replace(templateFolder.path, "")
                outputFolder.resolve(relativeFile).write(renderer.render(it.path, templateArguments))
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
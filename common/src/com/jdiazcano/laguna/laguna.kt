package com.jdiazcano.laguna

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.jdiazcano.laguna.files.File
import com.jdiazcano.laguna.git.Git
import com.jdiazcano.laguna.git.GitRepository
import com.jdiazcano.laguna.misc.runBlocking
import com.soywiz.korte.Templates

private var VERBOSE = false

class Laguna: CliktCommand() {
    val templateName: String by argument()
    val projectName: String by option("-n", "--name").required()
    val templateArguments: Map<String, String> by argument().multiple().transformAll { items ->
        (items + "name=$projectName").map { it.split("=", limit = 2) }.associate { it[0] to it[1] }
    }
    val outputFolder: String by option("-o", "--output").default(".", "Current folder")
    val verbose: Boolean by option("-v", "--verbose").flag(default = false)

    override fun run() {
        VERBOSE = verbose

        val repository = File("/tmp/laguna-templates")
        prepareRepository(repository)

        val templateFolder = repository.resolve(templateName)
        val outputFolder = File(outputFolder).resolve(projectName).apply { mkdirs() }
        debug("Created output folder: ${outputFolder.path}")
        val renderer = Templates(GitTemplateProvider(templateFolder))
        runBlocking {
            debug("Creating all directories")
            forEachDirectoryRecursive(templateFolder) {
                val file = File(it.path.replace(templateFolder.path, projectName))
                debug("Creating directory: ${file.path}")
                file.mkdirs()
            }
            forEachFileRecursive(templateFolder) {
                // The file is relative to the repository folder
                val relativeFile = it.path.replace(templateFolder.path, "")
                debug("Rendering file: $relativeFile")
                val renderedTemplate = renderer.render(it.path, templateArguments)
                outputFolder.resolve(relativeFile).write(renderedTemplate)
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
    debug("Is dir? ${repoFolder.isDirectory()}")
    val repo = if (!repoFolder.isDirectory()) {
        Git.clone("https://github.com/jdiazcano/laguna-templates.git", repoFolder)
    } else {
        GitRepository(repoFolder)
    }
    debug("Opening repository")
    repo.open()
    debug("Cleaning repository")
    repo.clean()
    debug("Fetching repository")
    repo.fetch()
    debug("Checking out master")
    repo.checkout("master")
    debug("Pulling repository")
    repo.pull()
    debug("Closing repository")
    repo.close()
}

fun debug(message: String) {
    if (VERBOSE) {
        println(message)
    }
}
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
import com.jdiazcano.laguna.git.GitException
import com.jdiazcano.laguna.git.GitRepository
import com.jdiazcano.laguna.misc.ExitCode
import com.jdiazcano.laguna.misc.exit
import com.jdiazcano.laguna.misc.runBlocking
import com.soywiz.korte.Templates

class Laguna: CliktCommand() {
    val templateName: String by argument()
    val projectName: String by option("-n", "--name").required()
    val templateArguments: Map<String, String> by argument().multiple().transformAll { items ->
        (items + "name=$projectName").map { it.split("=", limit = 2) }.associate { it[0] to it[1] }
    }
    val outputFolder: String by option("-o", "--output").default(".", "Current folder")
    val verbose: Boolean by option("-v", "--verbose").flag(default = false)
    val repositoryPath: String? by option("-r", "--repository")
    val noClean: Boolean by option("-C", "--no-clean").flag(default = false)

    override fun run() {
        val repository = `initialize and clean repository`()

        val templateFolder = repository.resolve(templateName)
        val outputFolder = File(outputFolder).resolve(projectName).apply {
            if (exists()) {
                println("Output folder already exists. Select a folder that does not exist.")
                exit(ExitCode.FOLDER_ALREADY_EXISTS)
            }
            mkdirs()
        }
        debug("Created output folder: ${outputFolder.path}")
        val renderer = Templates(GitTemplateProvider(templateFolder))
        renderer.render(templateFolder, outputFolder)

        exit(ExitCode.ALL_GOOD)
    }

    /**
     * Renders a whole folder
     */
    private fun Templates.render(templateFolder: File, outputFolder: File) = runBlocking {
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
            val renderedTemplate = render(it.path, templateArguments)
            outputFolder.resolve(relativeFile).write(renderedTemplate)
        }
    }

    private fun `initialize and clean repository`(): File {
        val repository = File(repositoryPath ?: "/tmp/laguna-templates")
        if (!noClean) {
            try {
                debug("Preparing repository...")
                prepareRepository(repository)
            } catch (e: GitException) {
                println("Could not clean the repository in: ${repository.absolutePath}")
                println("Reason: ${e.message}")
                println()
                println("If you don't want to clean up the repository (reset, pull), use --no-clean.")
                exit(ExitCode.GIT_ERROR)
            }
        }
        return repository
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

val laguna = Laguna()
fun main(args: Array<String>) {
    laguna.main(args)
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
    if (laguna.verbose) {
        println(message)
    }
}
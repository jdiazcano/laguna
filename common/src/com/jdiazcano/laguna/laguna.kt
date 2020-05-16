package com.jdiazcano.laguna

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import com.github.ajalt.clikt.parameters.options.*
import com.jdiazcano.laguna.files.File
import com.jdiazcano.laguna.git.Git
import com.jdiazcano.laguna.git.GitException
import com.jdiazcano.laguna.git.GitRepository
import com.jdiazcano.laguna.misc.ExitCode
import com.jdiazcano.laguna.misc.exit
import com.jdiazcano.laguna.misc.runBlocking
import com.soywiz.korte.Template
import com.soywiz.korte.TemplateConfig
import com.soywiz.korte.Templates

private const val DEFAULT_REPOSITORY_FOLDER = "/tmp/laguna-templates"

class Laguna: CliktCommand(printHelpOnEmptyArgs = true) {
    val repositoryPath: String by option("-r", "--repository", help = "Repository (or folder) where templates are located.").default(DEFAULT_REPOSITORY_FOLDER)
    val templateName: String by argument(help = "Name of the template.")
    val projectName: String by option("-n", "--name", help = "Project name (and name of the created folder)").required()
    val templateArguments: Map<String, String> by argument().multiple().transformAll { items ->
        (items + "name=$projectName").map { it.split("=", limit = 2) }.associate { it[0] to it[1] }
    }
    val outputFolder: String by option("-o", "--output", help = "Folder where the project will be created (Defaults to current folder)").default("", "Current folder")
    val verbose: Boolean by option("-v", "--verbose").flag(default = false)
    val noClean: Boolean by option("-C", "--no-clean", help = "Git repository will not be updated or cleaned up.").flag(default = false)
    val forceClean: Boolean by option("-c", "--clean", help = "Force clean up of repository.").flag(default = false)

    override fun run() {
        val repository = `initialize and clean repository`()

        val templateFolder = repository.resolve(templateName)
        val outputFolder = File(outputFolder).resolve(projectName).apply {
            if (exists()) {
                println("Output folder already exists. Select a folder that does not exist.")
                exit(ExitCode.FOLDER_ALREADY_EXISTS)
            }
        }
        val config = TemplateConfig().apply {
            replaceVariablePocessor { name, previous ->
                val value = previous(name) ?: exit(ExitCode.MISSING_VARIABLE_VALUE, "Variable '$name' is missing.")
                value
            }
        }
        val renderer = Templates(GitTemplateProvider(templateFolder), config = config)
        renderer.render(templateFolder, outputFolder)

        exit(ExitCode.ALL_GOOD)
    }

    /**
     * Renders a whole folder
     */
    private fun Templates.render(templateFolder: File, outputFolder: File) = runBlocking {
        val renderedTemplates = hashMapOf<File, String>()
        debug("Rendering folder: ${templateFolder.absolutePath}")
        forEachFileRecursive(templateFolder) {
            // The file is relative to the repository folder
            val relativeFile = it.path.replace(templateFolder.path, "")
            debug("Rendering file: $relativeFile")
            val renderedTemplate = render(relativeFile, templateArguments)
            val outputFile = outputFolder.resolve(relativeFile)
            val nameTemplate = Template(outputFile.path, config)
            val renderedOutputFile = File(nameTemplate(templateArguments))
            debug("Will render ${renderedOutputFile.path}")
            renderedTemplates[renderedOutputFile] = renderedTemplate
        }
        debug("Creating all directories")
        forEachDirectoryRecursive(templateFolder) {
            val directoryName = it.path.replace(templateFolder.path, projectName)
            val renderedDirectoryName = Template(directoryName, config)(templateArguments)
            val file = File(renderedDirectoryName)
            debug("Creating directory: ${file.path}")
            file.mkdirs()
        }
        debug("Writing templates into files")
        renderedTemplates.forEach {
            it.key.write(it.value)
        }
    }

    private fun `initialize and clean repository`(): File {
        val repository = File(repositoryPath)
        val isGitRepo = repository.resolve(".git").exists()
        val forceCleanup = forceClean
        val clean = (!noClean || forceCleanup) && isGitRepo
        if (clean) {
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
        file.files().forEach {
            if (it.isDirectory()) {
                block(it)
                forEachDirectoryRecursive(it, block)
            }
        }
    }
}

suspend fun <T> forEachFileRecursive(file: File, block: suspend (File) -> T) {
    debug("Foreach recursive: ${file.absolutePath}")
    if (file.isDirectory()) {
        file.files().forEach {
            println("Executing function for: ${it.absolutePath}")
            forEachFileRecursive(it, block)
        }
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
package com.jdiazcano.laguna

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import com.github.ajalt.clikt.parameters.options.*
import com.jdiazcano.laguna.files.*
import com.jdiazcano.laguna.git.Git
import com.jdiazcano.laguna.git.GitException
import com.jdiazcano.laguna.git.GitRepository
import com.jdiazcano.laguna.misc.*
import com.soywiz.korte.Template
import com.soywiz.korte.TemplateConfig
import com.soywiz.korte.Templates

private const val DEFAULT_REPOSITORY_FOLDER = "/tmp/laguna-templates"

class Laguna: CliktCommand(printHelpOnEmptyArgs = true) {
    val repositoryPath: String by option("-r", "--repository", help = "Repository (or folder) where templates are located.").default(DEFAULT_REPOSITORY_FOLDER)
    val templateName: String by argument(help = "Name of the template.")
    val projectName: String by option("-n", "--name", help = "Project name (and name of the created folder)").required()
    val templateArguments: Map<String, String> by argument(help = "Key=value arguments (can be multiple)").multiple().transformAll { items ->
        (items + "name=$projectName").map { it.split("=", limit = 2) }.associate { it[0] to it[1] }
    }
    val outputFolder: String by option("-o", "--output", help = "Folder where the project will be created (Defaults to current folder)").default("", "Current folder")
    val verbose: Boolean by option("-v", "--verbose", help = "Enable debug messages").flag(default = false)
    val noClean: Boolean by option("-C", "--no-clean", help = "Git repository will not be updated or cleaned up.").flag(default = false)
    val forceClean: Boolean by option("-c", "--clean", help = "Force clean up of repository.").flag(default = false)

    override fun run() {
        val repository = `initialize and clean repository`()

        val templateFolder = repository.resolve(templateName)
        val outputFolder = File(outputFolder).resolve(projectName).apply {
            if (exists()) {
                exit(ExitCode.FOLDER_ALREADY_EXISTS, "Output folder already exists. Select a folder that does not exist.".red.reset)
            }
        }
        val config = LagunaTemplateConfiguration()
        val renderer = Templates(GitTemplateProvider(templateFolder), config = config)
        renderer.render(templateFolder, outputFolder)

        exit(ExitCode.ALL_GOOD, "Template created!".grn.reset)
    }

    /**
     * Renders a whole folder
     */
    private fun Templates.render(templateFolder: File, outputFolder: File) = runBlocking {
        val renderedTemplates = hashMapOf<File, Output>()
        debug("Rendering folder: ${templateFolder.absolutePath}")
        templateFolder.forEachFileRecursive {
            // The file is relative to the repository folder
            val relativeFile = it.path.replace(templateFolder.path, "")
            debug("Rendering file: $relativeFile")
            val renderedTemplate = if (it.isBinary()) {
                BinaryOutput(File(it.absolutePath).readBytes())
            } else {
                StringOutput(render(relativeFile, templateArguments))
            }
            val outputFile = outputFolder.resolve(relativeFile)
            val nameTemplate = Template(outputFile.path, config)
            val renderedOutputFile = File(nameTemplate(templateArguments))
            debug("Will render ${renderedOutputFile.path}")
            renderedTemplates[renderedOutputFile] = renderedTemplate
        }
        debug("Creating all directories")
        templateFolder.forEachDirectoryRecursive {
            val directoryName = it.path.replace(templateFolder.path, "")
            val renderedDirectoryName = Template(directoryName, config)(templateArguments)
            val outputDirectory = outputFolder.resolve(renderedDirectoryName)
            debug("Creating directory: ${outputDirectory.path}")
            outputDirectory.mkdirs()
        }
        debug("Writing templates into files")
        renderedTemplates.forEach { (file, output) ->
            output.write(file)
        }
    }

    private fun `initialize and clean repository`(): File {
        val repository = File(repositoryPath)
        val isGitRepo = repository.resolve(".git").exists()
        val forceCleanup = forceClean
        val clean = (!noClean || forceCleanup) && (isGitRepo || !repository.exists())
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
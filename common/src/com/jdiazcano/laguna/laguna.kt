package com.jdiazcano.laguna

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import com.github.ajalt.clikt.parameters.options.*
import com.jdiazcano.laguna.files.*
import com.jdiazcano.laguna.git.Git
import com.jdiazcano.laguna.git.GitException
import com.jdiazcano.laguna.misc.*
import com.soywiz.korte.Template
import com.soywiz.korte.Templates

private const val DEFAULT_REPOSITORY = "https://github.com/jdiazcano/laguna-templates.git"
private const val CONFIG_FILE_NAME = ".laguna"
private val nonCoreFilter = { file: File ->
    !file.path.endsWith(CONFIG_FILE_NAME)
}

class Laguna: CliktCommand(printHelpOnEmptyArgs = true) {
    val repository: String by option("-r", "--repository", help = "Repository (or folder) where templates are located.").default(DEFAULT_REPOSITORY)
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
        val jsonConfigFile = templateFolder.resolve(CONFIG_FILE_NAME)
        val templateJsonConfiguration = if (jsonConfigFile.exists()) {
            jsonConfigFile.readAsJson(LagunaTemplateConfiguration.serializer())
        } else {
            null
        }
        val outputFolder = File(outputFolder).resolve(projectName).apply {
            if (exists()) {
                exit(ExitCode.FOLDER_ALREADY_EXISTS, "Output folder already exists. Select a folder that does not exist.".red)
            }
        }
        val config = LagunaKorteConfiguration()
        val renderer = Templates(GitTemplateProvider(templateFolder), config = config)
        templateJsonConfiguration.executeBefore(config, templateArguments)
        renderer.render(templateFolder, outputFolder)
        templateJsonConfiguration.executeAfter(config, templateArguments)

        exit(ExitCode.ALL_GOOD, "Template created!".grn)
    }

    /**
     * Renders a whole folder
     */
    private fun Templates.render(templateFolder: File, outputFolder: File) = runBlocking {
        val renderedTemplates = hashMapOf<File, Output>()
        debug("Rendering folder: ${templateFolder.absolutePath}")
        templateFolder.forEachFileRecursive(nonCoreFilter) {
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
        val isUrl = repository.let {
            // https://github.com/jdiazcano/laguna.git
            // git@github.com:jdiazcano/laguna.git
            it.startsWith("http") or it.startsWith("ssh")
        }
        val repositoryFolder = if (isUrl) {
            val repositoryFolderName = repository.substringAfterLast('/').removeSuffix(".git")
            File("/tmp/${repositoryFolderName}")
        } else {
            File(repository)
        }
        cloneRepoIfNeeded(repositoryFolder, repository)
        val isGitRepo = repositoryFolder.resolve(".git").exists()
        val forceCleanup = forceClean
        val clean = (!noClean || forceCleanup) && (isGitRepo || !repositoryFolder.exists())
        if (clean) {
            try {
                debug("Preparing repository...")
                prepareRepository(repositoryFolder)
            } catch (e: GitException) {
                println("Could not clean the repository in: ${repositoryFolder.absolutePath}")
                println("Reason: ${e.message}")
                println()
                println("If you don't want to clean up the repository (reset, pull), use --no-clean.")
                exit(ExitCode.GIT_ERROR)
            }
        }
        return repositoryFolder
    }

    private fun cloneRepoIfNeeded(repositoryFolder: File, repository: String) {
        debug("Is dir? ${repositoryFolder.isDirectory()}")
        if (!repositoryFolder.exists()) {
            debug("Cloning repository: $repository")
            Git.clone(repository, repositoryFolder)
        }
    }
}

val laguna = Laguna()
fun main(args: Array<String>) {
    laguna.main(args)
}
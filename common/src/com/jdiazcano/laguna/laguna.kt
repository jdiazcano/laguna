package com.jdiazcano.laguna

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.jdiazcano.laguna.git.GitRepository
import com.jdiazcano.laguna.git.GitResetMode
import io.ktor.utils.io.core.use

class Laguna: CliktCommand() {
    val templateName: String by argument()
    val projectName: String by option("-n", "--name").required()
    val templateArguments: Map<String, String> by argument().multiple().transformAll { items ->
        items.map {it.split("=", limit = 2) }.associate { it[0] to it[1] }
    }

    override fun run() {
        echo(templateName)
        echo(projectName)
        echo(templateArguments)

        val repository = GitRepository("laguna-templates")
        when (projectName) {
            "clone" -> {
                println("Cloning: ${repository.clone("https://github.com/jdiazcano/laguna-templates.git")}")
            }
            "reset" -> {
                repository.use {
                    println("Opening: ${repository.open()}")
                    println("Resetting: ${repository.reset(GitResetMode.HARD)}")
                    println("Checking out: ${repository.checkout("HEAD")}")
//                    println("Closing: ${repository.close()}")
                }
            }
            "add" -> {
                val folders = templateArguments.values.toTypedArray()
                println(folders.toList())
                repository.use {
                    println("Opening: ${repository.open()}")
                    println("Adding: ${repository.add(folders)}")
//                    println("Closing: ${repository.close()}")
                }
            }
            else -> TODO()
        }
    }
}
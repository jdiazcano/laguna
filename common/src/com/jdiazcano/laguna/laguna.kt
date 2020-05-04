package com.jdiazcano.laguna

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.transformAll
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required

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
    }
}
package com.jdiazcano.laguna

import com.jdiazcano.laguna.files.File
import com.jdiazcano.laguna.misc.runBlocking
import com.jdiazcano.laguna.misc.system
import com.soywiz.korte.Template
import com.soywiz.korte.TemplateProvider
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class GitTemplateProvider(val templateFolder: File): TemplateProvider {
    override suspend fun get(template: String): String {
        return this.templateFolder.resolve(template).read()
    }
}

@Serializable
data class LagunaTemplateConfiguration(
        val commands: LagunaCommands
)

@Serializable
data class LagunaCommands(
        val before: List<String>,
        val after: List<String>
)

fun <T> File.readAsJson(strategy: DeserializationStrategy<T>): T {
    val json = Json(JsonConfiguration.Stable)
    return json.parse(strategy, read())
}

fun LagunaTemplateConfiguration?.executeBefore(
        config: LagunaKorteConfiguration,
        templateArguments: Map<String, String>
) = executeRenderedCommands(this?.commands?.before, config, templateArguments)

fun LagunaTemplateConfiguration?.executeAfter(
        config: LagunaKorteConfiguration,
        templateArguments: Map<String, String>
) = executeRenderedCommands(this?.commands?.after, config, templateArguments)

private fun executeRenderedCommands(
        commands: List<String>?,
        config: LagunaKorteConfiguration,
        templateArguments: Map<String, String>
) = runBlocking {
    commands?.forEach {
        val renderedCommand = Template(it, config)(templateArguments)
        system(renderedCommand)
    }
}
package com.jdiazcano.laguna

import com.jdiazcano.laguna.files.File
import com.jdiazcano.laguna.misc.system
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

fun LagunaTemplateConfiguration?.executeBefore() = this?.commands?.before?.forEach { system(it) }
fun LagunaTemplateConfiguration?.executeAfter() = this?.commands?.after?.forEach { system(it) }
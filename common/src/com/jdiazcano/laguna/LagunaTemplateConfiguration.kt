package com.jdiazcano.laguna

import com.jdiazcano.laguna.misc.ExitCode
import com.jdiazcano.laguna.misc.exit
import com.jdiazcano.laguna.misc.red
import com.jdiazcano.laguna.misc.reset
import com.soywiz.korte.Filter
import com.soywiz.korte.TemplateConfig

class LagunaTemplateConfiguration: TemplateConfig() {
    init {
        replaceVariablePocessor { name, previous ->
            val value = previous(name) ?: exit(ExitCode.MISSING_VARIABLE_VALUE, "Variable '${name.red.reset}' is missing.")
            value
        }

        register(Filter("classname") {
            val name = subject.toDynamicString()
            val cleanName = name.capitalizeWords()
            cleanName
        })

        register(Filter("functionname") {
            val name = subject.toDynamicString()
            val cleanName = name.toKotlinFunction()
            cleanName
        })
    }
}

fun String.capitalizeWords(): String = split("[\\d.-]".toRegex()).joinToString("") { it.capitalize() }
fun String.toKotlinFunction(): String = replace("[\\d.-]".toRegex(), "").decapitalize()
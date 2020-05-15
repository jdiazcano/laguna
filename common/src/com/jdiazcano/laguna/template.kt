package com.jdiazcano.laguna

import com.jdiazcano.laguna.files.File
import com.soywiz.korte.TemplateProvider

class GitTemplateProvider(val templateFolder: File): TemplateProvider {
    override suspend fun get(template: String): String {
        return this.templateFolder.resolve(template).read()
    }
}
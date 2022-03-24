package com.programistich.twitter.template

import com.programistich.twitter.language.Language
import org.springframework.stereotype.Component
import org.tomlj.TomlParseResult

@Component
class TemplateReader(
    private val tomlParseResult: TomlParseResult,
) {
    fun getTemplate(template: Template, language: Language = Language.RUSSIAN, vararg values: String): String {
        val tableOfTemplate = tomlParseResult.getTable(template.name)
            ?: throw RuntimeException("Toml table by name ${template.name} is missing")
        var templateByLang: String = tableOfTemplate.get(language.tag) as String?
            ?: throw RuntimeException("Lang ${language.tag} in toml table is missing")
        values.forEach {
            templateByLang = templateByLang.replaceFirst("{}", it)
        }
        return templateByLang.trimIndent()
    }
}

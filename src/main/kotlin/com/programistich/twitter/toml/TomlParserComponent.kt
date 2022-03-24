package com.programistich.twitter.toml

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.tomlj.Toml
import org.tomlj.TomlParseResult

@Component
class TomlParserComponent {

    @Bean
    fun tomlParser(): TomlParseResult {
        val resourceAsStream = this::class.java.classLoader.getResourceAsStream(templatesFile)
            ?: throw RuntimeException("Toml file $templatesFile is missing")
        return Toml.parse(resourceAsStream)
    }

    companion object {
        const val templatesFile = "templates.toml"
    }
}

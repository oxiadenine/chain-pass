package io.github.oxiadenine.chainpass

import com.typesafe.config.ConfigFactory

class Intl(locale: String = DEFAULT_LANGUAGE) {
    companion object {
        const val DEFAULT_LANGUAGE = "en"

        val languages = listOf(DEFAULT_LANGUAGE, "es")
    }

    private val messages = ConfigFactory.load("locales/$locale").entrySet().associate { entry ->
        entry.key to entry.value.unwrapped().toString()
    }

    fun translate(id: String) = messages[id] ?: ""

    fun translate(id: String, value: Pair<String, String>) = messages[id]?.let { message ->
        if (message.contains(value.first)) {
            message.replace("{${value.first}}", value.second)
        } else message
    } ?: ""

    fun translate(id: String, values: Map<String, String>): String {
        var message = messages[id] ?: ""

        if (message.isNotEmpty()) {
            values.forEach { entry ->
                if (message.contains(entry.key)) {
                    message = message.replace("{${entry.key}}", entry.value)
                }
            }
        }

        return message
    }
}